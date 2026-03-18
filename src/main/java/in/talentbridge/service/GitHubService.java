package in.talentbridge.service;

import in.talentbridge.entity.Student;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubService {

    private final StudentRepository studentRepository;
    private final RestTemplate      restTemplate;

    private static final String GITHUB_API = "https://api.github.com/users/";

    // Optional — set GITHUB_TOKEN in application-local.properties to raise
    // rate limit from 60 → 5000 requests/hour.
    // Leave blank to use unauthenticated (fine for dev/demo).
    @Value("${github.token:}")
    private String githubToken;

    // ─────────────────────────────────────────────────────────────────────────

    public Map<String, Object> fetchAndScoreGitHub(String studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        if (student.getGithubProfile() == null || student.getGithubProfile().isBlank()) {
            throw new IllegalArgumentException("No GitHub profile URL linked to this student");
        }

        String username = extractUsername(student.getGithubProfile());
        log.info("[GITHUB] Fetching score for student: {} | GitHub username: {}", studentId, username);

        // Build headers — User-Agent is required by GitHub API
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // ── Fetch user profile ────────────────────────────────────────────
        Map<?, ?> userData;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    GITHUB_API + username, HttpMethod.GET, entity, Map.class);
            userData = response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[GITHUB] User '{}' not found on GitHub", username);
            throw new IllegalArgumentException("GitHub user '" + username + "' not found. Check the profile URL.");
        } catch (HttpClientErrorException e) {
            log.error("[GITHUB] GitHub API error for '{}': {} {}", username, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("GitHub API error: " + e.getStatusCode() + " — " + e.getMessage());
        }

        // ── Fetch repos ───────────────────────────────────────────────────
        List<Map<?, ?>> repos;
        try {
            ResponseEntity<List> repoResponse = restTemplate.exchange(
                    GITHUB_API + username + "/repos?per_page=100&sort=updated",
                    HttpMethod.GET, entity, List.class);
            //noinspection unchecked
            repos = (List<Map<?, ?>>) repoResponse.getBody();
        } catch (Exception e) {
            log.warn("[GITHUB] Failed to fetch repos for '{}', scoring without them: {}", username, e.getMessage());
            repos = List.of(); // score without repo data rather than failing completely
        }

        // ── Calculate score ───────────────────────────────────────────────
        int githubScore = calculateScore(userData, repos);
        log.info("[GITHUB] Score for {}: {}", username, githubScore);

        // ── Overall score — match your JPQL logic ─────────────────────────
        // Rule from handoff: if githubScore == 0 → overallScore = platformScore
        //                    else → (platformScore + githubScore) / 2
        int platformScore = student.getPlatformScore();
        int overallScore  = githubScore == 0
                ? platformScore/2
                : (platformScore + githubScore) / 2;

        // ── Persist ───────────────────────────────────────────────────────
        studentRepository.updateGitHubDetails(studentId, githubScore, overallScore);

        // ── Build response ────────────────────────────────────────────────
        Map<String, Object> result = new HashMap<>();
        result.put("username",    username);
        result.put("githubScore", githubScore);
        result.put("overallScore", overallScore);
        result.put("publicRepos", userData != null ? userData.get("public_repos") : 0);
        result.put("followers",   userData != null ? userData.get("followers")    : 0);
        result.put("message",     "GitHub score calculated successfully");

        log.info("[GITHUB] Done — githubScore={} overallScore={}", githubScore, overallScore);
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        // GitHub API requires a User-Agent header — requests without one get blocked
        headers.set("User-Agent", "TalentBridge-App");
        headers.set("Accept", "application/vnd.github.v3+json");

        // If a token is configured, add it to raise rate limit 60 → 5000 req/hr
        if (githubToken != null && !githubToken.isBlank()) {
            headers.set("Authorization", "Bearer " + githubToken);
            log.debug("[GITHUB] Using authenticated requests (token configured)");
        } else {
            log.debug("[GITHUB] Using unauthenticated requests (60 req/hr limit)");
        }
        return headers;
    }

    private String extractUsername(String githubUrl) {
        if (githubUrl == null || githubUrl.isBlank()) return "";
        String url = githubUrl.trim();
        // Remove trailing slash if present
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        // Extract from full URL: https://github.com/username
        if (url.contains("github.com/")) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        // Already just a username
        return url;
    }

    private int calculateScore(Map<?, ?> userData, List<Map<?, ?>> repos) {
        int score = 0;

        if (userData == null) return 0;

        // ── Profile completeness ──────────────────────────────────────────

        // Public repos (2 pts each, max 20)
        int publicRepos = toInt(userData.get("public_repos"));
        score += Math.min(publicRepos * 2, 20);

        // Followers (2 pts each, max 20)
        int followers = toInt(userData.get("followers"));
        score += Math.min(followers * 2, 20);

        // Has bio (5 pts)
        if (userData.get("bio") != null && !userData.get("bio").toString().isBlank()) {
            score += 5;
        }

        // Has blog/portfolio link (5 pts)
        Object blog = userData.get("blog");
        if (blog != null && !blog.toString().isBlank()) {
            score += 5;
        }

        // ── Repository quality ────────────────────────────────────────────

        if (repos != null && !repos.isEmpty()) {

            // Total stars across all repos (2 pts each, max 25)
            int totalStars = repos.stream()
                    .mapToInt(r -> toInt(r.get("stargazers_count")))
                    .sum();
            score += Math.min(totalStars * 2, 25);

            // Repos with a description (1 pt each, max 10)
            long reposWithDesc = repos.stream()
                    .filter(r -> r.get("description") != null
                            && !r.get("description").toString().isBlank())
                    .count();
            score += Math.min((int) reposWithDesc, 10);

            // Language diversity (3 pts per language, max 15)
            long distinctLanguages = repos.stream()
                    .filter(r -> r.get("language") != null)
                    .map(r -> r.get("language").toString())
                    .distinct()
                    .count();
            score += Math.min((int) distinctLanguages * 3, 15);
        }

        return Math.min(score, 100);
    }

    /** Safely converts Object → int (handles Integer and other Number types from JSON) */
    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return 0; }
    }
}