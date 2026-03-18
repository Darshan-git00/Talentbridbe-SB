package in.talentbridge.controller;

import in.talentbridge.service.AIInterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/interview")
@RequiredArgsConstructor
public class AIInterviewController {

    private final AIInterviewService aiInterviewService;

    /**
     * POST /api/ai/interview/start/{studentId}
     * Returns the first interview question based on student's profile.
     */
    @PostMapping("/start/{studentId}")
    public ResponseEntity<Map<String, String>> startInterview(
            @PathVariable String studentId) {
        String question = aiInterviewService.startInterview(studentId);
        return ResponseEntity.ok(Map.of(
                "message", question,
                "role",    "assistant"
        ));
    }

    /**
     * POST /api/ai/interview/message/{studentId}
     * Sends the full conversation history + student's latest answer.
     * Returns the AI's next question or feedback.
     *
     * Request body:
     * {
     *   "messages": [
     *     { "role": "assistant", "content": "Tell me about yourself." },
     *     { "role": "user",      "content": "I am a final year CS student..." },
     *     ...
     *   ]
     * }
     */
    @PostMapping("/message/{studentId}")
    public ResponseEntity<Map<String, String>> sendMessage(
            @PathVariable String studentId,
            @RequestBody Map<String, List<Map<String, String>>> body) {

        List<Map<String, String>> messages = body.get("messages");
        String reply = aiInterviewService.continueInterview(studentId, messages);
        return ResponseEntity.ok(Map.of(
                "message", reply,
                "role",    "assistant"
        ));
    }

    /**
     * POST /api/ai/interview/end/{studentId}
     * Sends the full conversation, returns a score + detailed feedback.
     * Also saves the aiInterviewScore to the student record.
     *
     * Returns:
     * {
     *   "score": 72,
     *   "feedback": "Overall strong communication...",
     *   "strengths": "...",
     *   "improvements": "..."
     * }
     */
    @PostMapping("/end/{studentId}")
    public ResponseEntity<Map<String, Object>> endInterview(
            @PathVariable String studentId,
            @RequestBody Map<String, List<Map<String, String>>> body) {

        List<Map<String, String>> messages = body.get("messages");
        Map<String, Object> result = aiInterviewService.endInterview(studentId, messages);
        return ResponseEntity.ok(result);
    }
}