package in.talentbridge.controller;

import in.talentbridge.service.CompetitiveProgrammingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
@Slf4j
public class CompetitiveProgrammingController {

    private final CompetitiveProgrammingService cpService;



    /**
     * GET /api/platform/score/{studentId}
     *
     * Auto-detects platform from the student's competitiveProgrammingProfile URL,
     * fetches live data, calculates score, persists it, and returns the result.
     *
     * Frontend calls this from the "Refresh" button in ProfileTab.
     */
    @GetMapping("/score/{studentId}")
    public ResponseEntity<?> getPlatformScore(@PathVariable String studentId) {
        log.info("[CP CONTROLLER] Score request for studentId: {}", studentId);
        try {
            Map<String, Object> result = cpService.fetchAndScore(studentId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // User-facing errors: profile not set, username not found, unsupported platform
            log.warn("[CP CONTROLLER] Bad request for {}: {}", studentId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Unexpected errors: API down, network issues etc.
            log.error("[CP CONTROLLER] Unexpected error for {}: {}", studentId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Failed to calculate platform score. Please try again."));
        }
    }
}