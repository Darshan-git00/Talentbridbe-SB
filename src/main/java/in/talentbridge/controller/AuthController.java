package in.talentbridge.controller;

import in.talentbridge.dto.*;
import in.talentbridge.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}