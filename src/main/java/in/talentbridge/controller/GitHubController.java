package in.talentbridge.controller;

import in.talentbridge.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;

    @GetMapping("/score/{studentId}")
    public ResponseEntity<Map<String, Object>> getGitHubScore(@PathVariable String studentId) {
        return ResponseEntity.ok(gitHubService.fetchAndScoreGitHub(studentId));
    }
}