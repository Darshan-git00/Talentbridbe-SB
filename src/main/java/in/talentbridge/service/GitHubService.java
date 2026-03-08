package in.talentbridge.service;

import in.talentbridge.entity.Student;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;

    private static final String GITHUB_API = "https://api.github.com/users/";

    public Map<String, Object> fetchAndScoreGitHub(String studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (student.getGithubProfile() == null || student.getGithubProfile().isEmpty()) {
            throw new IllegalArgumentException("Student has no GitHub profile linked");
        }

        // Extract username from URL
        String username = extractUsername(student.getGithubProfile());
        //System.out.println("GitHub username: " + username);

        // Fetch GitHub user data
        Map userdata = restTemplate.getForObject(GITHUB_API + username, Map.class);
        //System.out.println("GitHub user data: " + userdata);

        // Fetch GitHub repos
        List<Map> repos = restTemplate.getForObject(GITHUB_API + username + "/repos?per_page=100", List.class);
        //System.out.println("GitHub repos count: " + (repos != null ? repos.size() : 0));

        // Calculate score
        int githubScore = calculateGitHubScore(userdata, repos);
        //System.out.println("GitHub score: " + githubScore);

        // Calculate overall score
        int overallScore = (student.getPlatformScore() + githubScore) / 2;

        // Update student in DB
        studentRepository.updateGitHubDetails(studentId, githubScore, overallScore);

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("githubScore", githubScore);
        result.put("overallScore", overallScore);
        result.put("publicRepos", userdata != null ? userdata.get("public_repos") : 0);
        result.put("followers", userdata != null ? userdata.get("followers") : 0);
        result.put("message", "GitHub score calculated successfully");
        return result;
    }

    private String extractUsername(String githubUrl) {
        // Handle both https://github.com/username and just username
        if (githubUrl.contains("github.com/")) {
            return githubUrl.substring(githubUrl.lastIndexOf("/") + 1);
        }
        return githubUrl;
    }

    private int calculateGitHubScore(Map userData, List<Map> repos) {
        int score = 0;

        if (userData == null) return 0;

        // Public repos — max 20 points
        int publicRepos = userData.get("public_repos") != null ?
                (Integer) userData.get("public_repos") : 0;
        score += Math.min(publicRepos * 2, 20);

        // Followers — max 20 points
        int followers = userData.get("followers") != null ?
                (Integer) userData.get("followers") : 0;
        score += Math.min(followers * 2, 20);

        // Has bio — 5 points
        if (userData.get("bio") != null) score += 5;

        // Has blog/portfolio — 5 points
        if (userData.get("blog") != null && !userData.get("blog").toString().isEmpty()) score += 5;

        if (repos != null) {
            // Total stars across all repos — max 25 points
            int totalStars = repos.stream()
                    .mapToInt(repo -> repo.get("stargazers_count") != null ?
                            (Integer) repo.get("stargazers_count") : 0)
                    .sum();
            score += Math.min(totalStars * 2, 25);

            // Has README — check if description exists — max 10 points
            long reposWithDescription = repos.stream()
                    .filter(repo -> repo.get("description") != null)
                    .count();
            score += Math.min((int) reposWithDescription, 10);

            // Language diversity — max 15 points
            long distinctLanguages = repos.stream()
                    .filter(repo -> repo.get("language") != null)
                    .map(repo -> repo.get("language").toString())
                    .distinct()
                    .count();
            score += Math.min((int) distinctLanguages * 3, 15);
        }

        return Math.min(score, 100);
    }
}