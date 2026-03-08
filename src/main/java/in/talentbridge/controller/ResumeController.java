package in.talentbridge.controller;

import in.talentbridge.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload/{studentId}")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @PathVariable String studentId,
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = resumeService.uploadAndParseResume(studentId, file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Failed to upload resume: " + e.getMessage()));
        }
    }
}