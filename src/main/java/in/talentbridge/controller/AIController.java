package in.talentbridge.controller;

import in.talentbridge.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    // Match student to a drive
    @GetMapping("/match/{studentId}/{driveId}")
    public ResponseEntity<Map<String, Object>> matchStudentToDrive(
            @PathVariable String studentId,
            @PathVariable String driveId) {
        return ResponseEntity.ok(aiService.matchStudentToDrive(studentId, driveId));
    }

    // Get AI profile feedback for a student
    @GetMapping("/feedback/{studentId}")
    public ResponseEntity<Map<String, Object>> getProfileFeedback(
            @PathVariable String studentId) {
        return ResponseEntity.ok(aiService.getProfileFeedback(studentId));
    }
}