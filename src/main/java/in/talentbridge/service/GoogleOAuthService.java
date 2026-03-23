package in.talentbridge.service;

import in.talentbridge.dto.AuthResponse;
import in.talentbridge.entity.College;
import in.talentbridge.entity.Recruiter;
import in.talentbridge.entity.Student;
import in.talentbridge.repository.CollegeRepository;
import in.talentbridge.repository.RecruiterRepository;
import in.talentbridge.repository.StudentRepository;
import in.talentbridge.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private final StudentRepository   studentRepository;
    private final RecruiterRepository recruiterRepository;
    private final CollegeRepository   collegeRepository;
    private final JwtUtil             jwtUtil;
    private final RestTemplate        restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private static final String TOKEN_URL    = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    // ── Build Google OAuth URL ────────────────────────────────────────────
    public String buildAuthUrl(String role) {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id="      + clientId
                + "&redirect_uri="   + encode(redirectUri)
                + "&response_type=code"
                + "&scope=email%20profile"
                + "&state="          + role.toUpperCase()
                + "&access_type=offline"
                + "&prompt=select_account";
    }

    // ── Handle callback — two paths: existing user OR new user ────────────
    public String buildRedirectUrl(String code, String role) {
        // 1. Get Google user info
        String accessToken = exchangeCodeForToken(code);
        Map<String, Object> rawUserInfo = fetchUserInfo(accessToken);

        // 2. Normalize email — always lowercase + trimmed
        String email = ((String) rawUserInfo.get("email")).trim().toLowerCase();
        String name  = (String) rawUserInfo.getOrDefault("name", "");

        log.info("[GOOGLE OAUTH] Callback for role={} email={}", role, email);

        // 3. Route to correct handler
        return switch (role.toUpperCase()) {
            case "STUDENT"   -> handleStudentCallback(email, name);
            case "RECRUITER" -> handleRecruiterCallback(email, name);
            case "COLLEGE"   -> handleCollegeCallback(email, name);
            default -> frontendUrl + "/?error=invalid_role";
        };
    }

    // ── STUDENT ───────────────────────────────────────────────────────────
    private String handleStudentCallback(String email, String name) {
        Optional<Student> existing = studentRepository.findByEmail(email);

        if (existing.isPresent()) {
            // Existing user — issue JWT and go straight to dashboard
            Student student = existing.get();
            String token = jwtUtil.generateToken(student.getEmail(), "STUDENT", student.getId());
            log.info("[GOOGLE OAUTH] Existing student login: {}", email);
            return frontendUrl + "/student/dashboard"
                    + "?token="  + token
                    + "&userId=" + student.getId()
                    + "&name="   + encode(student.getName())
                    + "&email="  + encode(student.getEmail())
                    + "&role=STUDENT";
        }

        // New user — redirect to complete-profile page with Google info pre-filled
        log.info("[GOOGLE OAUTH] New student — redirecting to complete profile: {}", email);
        return frontendUrl + "/auth/student/complete"
                + "?name="  + encode(name)
                + "&email=" + encode(email)
                + "&role=STUDENT";
    }

    // ── RECRUITER ─────────────────────────────────────────────────────────
    private String handleRecruiterCallback(String email, String name) {
        Optional<Student> existing = studentRepository.findByEmail(email.toLowerCase());

        if (existing.isPresent()) {
            Student recruiter = existing.get();
            String token = jwtUtil.generateToken(recruiter.getEmail(), "RECRUITER", recruiter.getId());
            log.info("[GOOGLE OAUTH] Existing recruiter login: {}", email);
            return frontendUrl + "/recruiter/dashboard"
                    + "?token="  + token
                    + "&userId=" + recruiter.getId()
                    + "&name="   + encode(recruiter.getName())
                    + "&email="  + encode(recruiter.getEmail())
                    + "&role=RECRUITER";
        }

        log.info("[GOOGLE OAUTH] New recruiter — redirecting to complete profile: {}", email);
        return frontendUrl + "/auth/recruiter/complete"
                + "?name="  + encode(name)
                + "&email=" + encode(email)
                + "&role=RECRUITER";
    }

    // ── COLLEGE ───────────────────────────────────────────────────────────
    private String handleCollegeCallback(String email, String name) {
        Optional<College> existing = collegeRepository.findByEmail(email);

        if (existing.isPresent()) {
            College college = existing.get();
            String token = jwtUtil.generateToken(college.getEmail(), "COLLEGE", college.getId());
            log.info("[GOOGLE OAUTH] Existing college login: {}", email);
            return frontendUrl + "/college/dashboard"
                    + "?token="  + token
                    + "&userId=" + college.getId()
                    + "&name="   + encode(college.getName())
                    + "&email="  + encode(college.getEmail())
                    + "&role=COLLEGE";
        }

        log.info("[GOOGLE OAUTH] New college — redirecting to complete profile: {}", email);
        return frontendUrl + "/auth/college/complete"
                + "?name="  + encode(name)
                + "&email=" + encode(email)
                + "&role=COLLEGE";
    }

    // ── Google API helpers ────────────────────────────────────────────────

    private String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code",          code);
        body.add("client_id",     clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri",  redirectUri);
        body.add("grant_type",    "authorization_code");

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    TOKEN_URL, new HttpEntity<>(body, headers), Map.class);
            Map<?, ?> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("access_token")) {
                throw new RuntimeException("No access token in Google response");
            }
            return (String) responseBody.get("access_token");
        } catch (Exception e) {
            log.error("[GOOGLE OAUTH] Token exchange failed: {}", e.getMessage());
            throw new RuntimeException("Failed to exchange Google authorization code");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    USERINFO_URL, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map<String, Object> userInfo = response.getBody();
            if (userInfo == null || !userInfo.containsKey("email")) {
                throw new RuntimeException("Could not fetch user info from Google");
            }
            return userInfo;
        } catch (Exception e) {
            log.error("[GOOGLE OAUTH] UserInfo fetch failed: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch Google user info");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}