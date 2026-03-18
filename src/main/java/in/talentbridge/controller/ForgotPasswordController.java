package in.talentbridge.controller;

import in.talentbridge.dto.ForgotPasswordRequest;
import in.talentbridge.dto.ResetPasswordRequest;
import in.talentbridge.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    /**
     * POST /api/auth/forgot-password
     * Always returns 200 — never leaks whether the email exists.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        try {
            forgotPasswordService.processForgotPassword(request.getEmail());
        } catch (Exception ignored) {
            // Intentional swallow
        }
        return ResponseEntity.ok(Map.of(
                "message", "If that email is registered, a reset link has been sent."
        ));
    }

    /**
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        forgotPasswordService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }
}