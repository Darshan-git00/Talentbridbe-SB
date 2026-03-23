package in.talentbridge.controller;

import in.talentbridge.dto.*;
import in.talentbridge.service.AuthService;
import in.talentbridge.service.ForgotPasswordService;
import in.talentbridge.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService        authService;
    private final GoogleOAuthService googleOAuthService;
    private final ForgotPasswordService forgotPasswordService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ── Existing endpoints — untouched ────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup/student")
    public ResponseEntity<AuthResponse> signupStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(authService.signupStudent(request));
    }

    @PostMapping("/signup/recruiter")
    public ResponseEntity<AuthResponse> signupRecruiter(@Valid @RequestBody RecruiterRequest request) {
        return ResponseEntity.ok(authService.signupRecruiter(request));
    }

    @PostMapping("/signup/college")
    public ResponseEntity<AuthResponse> signupCollege(@Valid @RequestBody CollegeRequest request) {
        return ResponseEntity.ok(authService.signupCollege(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("No token provided");
        }
        String token = authHeader.substring(7);
        return ResponseEntity.ok(authService.getMe(token));
    }

//    @PostMapping("/forgot-password")
//    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
//        forgotPasswordService.processForgotPassword(body.get("email"));
//        return ResponseEntity.ok(Map.of("message", "If that email exists, we've sent a reset link."));
//    }
//
//    @PostMapping("/reset-password")
//    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
//        forgotPasswordService.resetPassword(body.get("token"), body.get("newPassword"));
//        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
//    }

    // ── NEW: Google OAuth ─────────────────────────────────────────────────

    /**
     * GET /api/auth/oauth2/google?role=STUDENT
     * Returns the Google OAuth URL — frontend redirects the browser to it.
     */
    @GetMapping("/oauth2/google")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl(@RequestParam String role) {
        String url = googleOAuthService.buildAuthUrl(role.toUpperCase());
        log.info("[AUTH] Google OAuth URL requested for role: {}", role);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * GET /api/auth/oauth2/callback?code=xxx&state=STUDENT
     *
     * Google redirects here after authentication.
     *
     * Two outcomes:
     *  A) Existing user → redirect to dashboard with JWT in params
     *  B) New user      → redirect to /auth/{role}/complete with name+email pre-filled
     */
    @GetMapping("/oauth2/callback")
    public void handleGoogleCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response
    ) throws IOException {
        log.info("[AUTH] Google OAuth callback | role: {}", state);
        try {
            String redirectUrl = googleOAuthService.buildRedirectUrl(code, state);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("[AUTH] Google OAuth callback failed: {}", e.getMessage());
            String role = state.toLowerCase();
            response.sendRedirect(frontendUrl + "/auth/" + role + "?error=oauth_failed");
        }
    }
}