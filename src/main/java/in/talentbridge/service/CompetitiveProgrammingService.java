package in.talentbridge.service;

import in.talentbridge.entity.Student;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CompetitiveProgrammingService {

    private final StudentRepository studentRepository;
    private final RestTemplate      restTemplate;

    // ─── Platform API base URLs ───────────────────────────────────────────────
    private static final String LEETCODE_GRAPHQL   = "https://leetcode.com/graphql";
    private static final String CODEFORCES_API     = "https://codeforces.com/api/user.info?handles=";
    private static final String HACKERRANK_API     = "https://www.hackerrank.com/rest/hackers/";

    // ─── Entry point ─────────────────────────────────────────────────────────

    public Map<String, Object> fetchAndScore(String studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        String profileUrl = student.getCompetitiveProgrammingProfile();
        if (profileUrl == null || profileUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "No competitive programming profile linked to this student. " +
                            "Add a HackerRank, LeetCode, or Codeforces profile URL first."
            );
        }

        Platform platform = detectPlatform(profileUrl);
        String   username = extractUsername(profileUrl, platform);

        log.info("[CP] Scoring student: {} | Platform: {} | Username: {}", studentId, platform, username);

        int platformScore = switch (platform) {
            case LEETCODE    -> scoreLeetCode(username);
            case CODEFORCES  -> scoreCodeforces(username);
            case HACKERRANK  -> scoreHackerRank(username);
        };

        log.info("[CP] Platform score for {}: {}", username, platformScore);

        // ── Overall score — same formula as GitHubService ─────────────────
        int githubScore  = student.getGithubScore();
        int overallScore = githubScore == 0
                ? platformScore / 2
                : (platformScore + githubScore) / 2;

        // ── Persist ───────────────────────────────────────────────────────
        studentRepository.updatePlatformDetails(studentId, platformScore, overallScore);

        // ── Response ──────────────────────────────────────────────────────
        Map<String, Object> result = new HashMap<>();
        result.put("platform",      platform.name().toLowerCase());
        result.put("username",      username);
        result.put("platformScore", platformScore);
        result.put("overallScore",  overallScore);
        result.put("message",       "Platform score calculated successfully");

        log.info("[CP] Done — platformScore={} overallScore={}", platformScore, overallScore);
        return result;
    }

    // ─── Platform detection ───────────────────────────────────────────────────

    private Platform detectPlatform(String url) {
        String lower = url.toLowerCase();
        if (lower.contains("leetcode.com"))    return Platform.LEETCODE;
        if (lower.contains("codeforces.com"))  return Platform.CODEFORCES;
        if (lower.contains("hackerrank.com"))  return Platform.HACKERRANK;
        throw new IllegalArgumentException(
                "Unsupported platform URL. Please use a HackerRank, LeetCode, or Codeforces profile URL."
        );
    }

    private String extractUsername(String url, Platform platform) {
        if (url == null || url.isBlank()) return "";
        String trimmed = url.trim();
        if (trimmed.endsWith("/")) trimmed = trimmed.substring(0, trimmed.length() - 1);

        return switch (platform) {
            // https://leetcode.com/username
            case LEETCODE   -> trimmed.substring(trimmed.lastIndexOf("/") + 1);
            // https://codeforces.com/profile/username
            case CODEFORCES -> trimmed.substring(trimmed.lastIndexOf("/") + 1);
            // https://hackerrank.com/username
            case HACKERRANK -> trimmed.substring(trimmed.lastIndexOf("/") + 1);
        };
    }

    // ─── LeetCode scorer ─────────────────────────────────────────────────────
    // Uses LeetCode's public GraphQL endpoint — no auth required

    private int scoreLeetCode(String username) {
        String query = """
            {
              "query": "{ matchedUser(username: \\"%s\\") { submitStats { acSubmissionNum { difficulty count } } profile { ranking reputation } } }",
              "variables": {}
            }
            """.formatted(username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "TalentBridge-App");
        headers.set("Referer", "https://leetcode.com");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    LEETCODE_GRAPHQL, HttpMethod.POST,
                    new HttpEntity<>(query, headers), Map.class
            );

            Map<?, ?> body = response.getBody();
            if (body == null) return 0;

            //noinspection unchecked
            Map<?, ?> data = (Map<?, ?>) body.get("data");
            if (data == null) return 0;

            //noinspection unchecked
            Map<?, ?> user = (Map<?, ?>) data.get("matchedUser");
            if (user == null) {
                log.warn("[CP][LEETCODE] User '{}' not found", username);
                throw new IllegalArgumentException("LeetCode user '" + username + "' not found.");
            }

            //noinspection unchecked
            Map<?, ?> stats   = (Map<?, ?>) user.get("submitStats");
            //noinspection unchecked
            Map<?, ?> profile = (Map<?, ?>) user.get("profile");

            int easy   = 0, medium = 0, hard = 0;

            if (stats != null) {
                //noinspection unchecked
                List<Map<?, ?>> counts = (List<Map<?, ?>>) stats.get("acSubmissionNum");
                if (counts != null) {
                    for (Map<?, ?> entry : counts) {
                        String diff  = String.valueOf(entry.get("difficulty"));
                        int    count = toInt(entry.get("count"));
                        switch (diff.toUpperCase()) {
                            case "EASY"   -> easy   = count;
                            case "MEDIUM" -> medium = count;
                            case "HARD"   -> hard   = count;
                        }
                    }
                }
            }

            int ranking    = profile != null ? toInt(profile.get("ranking"))    : 999_999;
            int reputation = profile != null ? toInt(profile.get("reputation")) : 0;

            return calculateLeetCodeScore(easy, medium, hard, ranking, reputation);

        } catch (IllegalArgumentException e) {
            throw e; // re-throw user-not-found so controller returns 400
        } catch (Exception e) {
            log.error("[CP][LEETCODE] Error fetching {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to fetch LeetCode profile. Check the username.");
        }
    }

    private int calculateLeetCodeScore(int easy, int medium, int hard, int ranking, int reputation) {
        int score = 0;

        // Problem solving — weighted by difficulty
        score += Math.min(easy   * 1, 15);   // easy:   1 pt each, max 15
        score += Math.min(medium * 2, 35);   // medium: 2 pts each, max 35
        score += Math.min(hard   * 4, 30);   // hard:   4 pts each, max 30

        // Global ranking bonus (lower rank = better)
        if      (ranking <= 1_000)   score += 15;
        else if (ranking <= 10_000)  score += 10;
        else if (ranking <= 50_000)  score += 5;
        else if (ranking <= 200_000) score += 2;

        // Reputation (community contributions)
        score += Math.min(reputation / 10, 5);

        return Math.min(score, 100);
    }

    // ─── Codeforces scorer ────────────────────────────────────────────────────
    // Codeforces has the cleanest public REST API — no auth needed

    private int scoreCodeforces(String username) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    CODEFORCES_API + username, HttpMethod.GET,
                    new HttpEntity<>(buildBasicHeaders()), Map.class
            );

            Map<?, ?> body = response.getBody();
            if (body == null || !"OK".equals(body.get("status"))) {
                log.warn("[CP][CODEFORCES] Bad response for '{}': {}", username, body);
                throw new IllegalArgumentException("Codeforces user '" + username + "' not found.");
            }

            //noinspection unchecked
            List<Map<?, ?>> resultList = (List<Map<?, ?>>) body.get("result");
            if (resultList == null || resultList.isEmpty()) {
                throw new IllegalArgumentException("Codeforces user '" + username + "' not found.");
            }

            Map<?, ?> user = resultList.get(0);
            return calculateCodeforcesScore(user);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Codeforces user '" + username + "' not found.");
        } catch (Exception e) {
            log.error("[CP][CODEFORCES] Error fetching {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to fetch Codeforces profile. Check the username.");
        }
    }

    private int calculateCodeforcesScore(Map<?, ?> user) {
        int score = 0;

        int rating    = toInt(user.get("rating"));
        int maxRating = toInt(user.get("maxRating"));
        // contribution is how much they've helped the community (problem proposals etc)
        int contribution = toInt(user.get("contribution"));

        // Current rating — Codeforces rating goes from ~800 (newbie) to 3500+ (legendary)
        if      (rating >= 2400) score += 50; // Grandmaster+
        else if (rating >= 2100) score += 42; // Master
        else if (rating >= 1900) score += 35; // Candidate Master
        else if (rating >= 1600) score += 27; // Expert
        else if (rating >= 1400) score += 20; // Specialist
        else if (rating >= 1200) score += 13; // Pupil
        else if (rating >= 800)  score += 7;  // Newbie
        // 0 = unrated

        // Max rating bonus (shows peak performance, max 20 pts)
        if      (maxRating >= 2400) score += 20;
        else if (maxRating >= 2100) score += 16;
        else if (maxRating >= 1900) score += 12;
        else if (maxRating >= 1600) score += 8;
        else if (maxRating >= 1400) score += 5;
        else if (maxRating >= 1200) score += 3;

        // Contribution to community (max 10 pts)
        score += Math.min(Math.max(contribution, 0) / 5, 10);

        // Rank title bonus (separate from numeric rating)
        String rank = user.get("rank") != null ? user.get("rank").toString().toLowerCase() : "";
        score += switch (rank) {
            case "legendary grandmaster"        -> 20;
            case "international grandmaster"    -> 18;
            case "grandmaster"                  -> 16;
            case "international master"         -> 13;
            case "master"                       -> 10;
            case "candidate master"             -> 7;
            case "expert"                       -> 4;
            default                             -> 0;
        };

        return Math.min(score, 100);
    }

    // ─── HackerRank scorer ────────────────────────────────────────────────────
    // Uses HackerRank's public profile endpoint

    private int scoreHackerRank(String username) {
        HttpHeaders headers = buildBasicHeaders();
        headers.set("Accept", "application/json");

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    HACKERRANK_API + username + "/badges", HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class
            );

            Map<?, ?> body = response.getBody();
            return calculateHackerRankScore(body, username);

        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("HackerRank user '" + username + "' not found.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CP][HACKERRANK] Error fetching {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to fetch HackerRank profile. Check the username.");
        }
    }

    private int calculateHackerRankScore(Map<?, ?> body, String username) {
        int score = 0;
        if (body == null) return 0;

        //noinspection unchecked
        List<Map<?, ?>> badges = (List<Map<?, ?>>) body.get("models");
        if (badges == null || badges.isEmpty()) {
            log.info("[CP][HACKERRANK] No badges found for '{}'", username);
            return 5; // account exists but no badges yet
        }

        for (Map<?, ?> badge : badges) {
            int stars = toInt(badge.get("stars"));
            // Each badge worth up to 5 pts based on star level (1–5 stars)
            // Cap per badge at 5 so one super-badge doesn't dominate
            score += Math.min(stars * 2, 5);
        }

        // Badge count bonus (breadth of skill coverage)
        int badgeCount = badges.size();
        score += Math.min(badgeCount * 3, 30); // 3 pts per badge, max 30

        // Top-starred badge bonus
        int maxStars = badges.stream()
                .mapToInt(b -> toInt(b.get("stars")))
                .max()
                .orElse(0);
        if      (maxStars == 5) score += 20;
        else if (maxStars == 4) score += 12;
        else if (maxStars == 3) score += 6;
        else if (maxStars >= 1) score += 2;

        return Math.min(score, 100);
    }

    // ─── Shared helpers ───────────────────────────────────────────────────────

    private HttpHeaders buildBasicHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "TalentBridge-App");
        headers.set("Accept", "application/json");
        return headers;
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return 0; }
    }

    // ─── Platform enum ────────────────────────────────────────────────────────

    public enum Platform {
        LEETCODE, CODEFORCES, HACKERRANK
    }
}