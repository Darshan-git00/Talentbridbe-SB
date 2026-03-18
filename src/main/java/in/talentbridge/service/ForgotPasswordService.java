package in.talentbridge.service;

import in.talentbridge.entity.College;
import in.talentbridge.entity.Recruiter;
import in.talentbridge.entity.Student;
import in.talentbridge.repository.CollegeRepository;
import in.talentbridge.repository.RecruiterRepository;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordService {

    private final StudentRepository   studentRepository;
    private final RecruiterRepository recruiterRepository;
    private final CollegeRepository   collegeRepository;
    private final EmailService        emailService;
    private final PasswordEncoder     passwordEncoder;

    private static final int TOKEN_EXPIRY_MINUTES = 15;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ── Step 1 ────────────────────────────────────────────────────────────────

    @Transactional
    public void processForgotPassword(String email) {
        if (email == null || email.isBlank()) {
            log.warn("[FORGOT-PWD] Empty email received");
            return;
        }

        String normalised = email.trim().toLowerCase();
        log.info("[FORGOT-PWD] Processing for: {}", normalised);

        Optional<Student> student = studentRepository.findByEmail(normalised);
        if (student.isPresent()) {
            log.info("[FORGOT-PWD] Matched student: {}", normalised);
            Student s = student.get();
            String token = generateToken();
            s.setPasswordResetToken(token);
            s.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
            studentRepository.save(s);
            sendResetEmail(s.getEmail(), s.getName(), token);
            return;
        }

        Optional<Recruiter> recruiter = recruiterRepository.findByEmail(normalised);
        if (recruiter.isPresent()) {
            log.info("[FORGOT-PWD] Matched recruiter: {}", normalised);
            Recruiter r = recruiter.get();
            String token = generateToken();
            r.setPasswordResetToken(token);
            r.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
            recruiterRepository.save(r);
            sendResetEmail(r.getEmail(), r.getName(), token);
            return;
        }

        Optional<College> college = collegeRepository.findByEmail(normalised);
        if (college.isPresent()) {
            log.info("[FORGOT-PWD] Matched college: {}", normalised);
            College c = college.get();
            String token = generateToken();
            c.setPasswordResetToken(token);
            c.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
            collegeRepository.save(c);
            sendResetEmail(c.getEmail(), c.getName(), token);
            return;
        }

        log.warn("[FORGOT-PWD] No account found for: {} — silently ignoring", normalised);
    }

    // ── Step 2 ────────────────────────────────────────────────────────────────

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank())
            throw new RuntimeException("Reset token is required");
        if (newPassword == null || newPassword.length() < 8)
            throw new RuntimeException("Password must be at least 8 characters");

        log.info("[FORGOT-PWD] Reset attempt with token prefix: {}",
                token.length() > 8 ? token.substring(0, 8) + "..." : token);

        String encoded = passwordEncoder.encode(newPassword);

        Optional<Student> student = studentRepository.findByPasswordResetToken(token);
        if (student.isPresent()) {
            Student s = student.get();
            assertNotExpired(s.getPasswordResetTokenExpiry());
            s.setPasswordHash(encoded);
            s.setPasswordResetToken(null);
            s.setPasswordResetTokenExpiry(null);
            studentRepository.save(s);
            log.info("[FORGOT-PWD] Reset successful for student: {}", s.getEmail());
            return;
        }

        Optional<Recruiter> recruiter = recruiterRepository.findByPasswordResetToken(token);
        if (recruiter.isPresent()) {
            Recruiter r = recruiter.get();
            assertNotExpired(r.getPasswordResetTokenExpiry());
            r.setPasswordHash(encoded);
            r.setPasswordResetToken(null);
            r.setPasswordResetTokenExpiry(null);
            recruiterRepository.save(r);
            log.info("[FORGOT-PWD] Reset successful for recruiter: {}", r.getEmail());
            return;
        }

        Optional<College> college = collegeRepository.findByPasswordResetToken(token);
        if (college.isPresent()) {
            College c = college.get();
            assertNotExpired(c.getPasswordResetTokenExpiry());
            c.setPasswordHash(encoded);
            c.setPasswordResetToken(null);
            c.setPasswordResetTokenExpiry(null);
            collegeRepository.save(c);
            log.info("[FORGOT-PWD] Reset successful for college: {}", c.getEmail());
            return;
        }

        log.warn("[FORGOT-PWD] Token not found in any repo");
        throw new RuntimeException("Invalid or expired reset token");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void assertNotExpired(LocalDateTime expiry) {
        if (expiry == null || LocalDateTime.now().isAfter(expiry)) {
            log.warn("[FORGOT-PWD] Token expired at: {}", expiry);
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }
    }

    private void sendResetEmail(String to, String name, String token) {
        String resetLink = frontendUrl + "/forgot-password?token=" + token;
        String subject   = "Reset your TalentBridge password";

        // ── Plain text fallback ───────────────────────────────────────────
        String plainText = String.format("""
                Hi %s,

                We received a request to reset your TalentBridge password.

                Reset link (valid for %d minutes):
                %s

                If you didn't request this, ignore this email — your password won't change.

                — The TalentBridge Team
                """, name, TOKEN_EXPIRY_MINUTES, resetLink);

        // ── HTML email ────────────────────────────────────────────────────
        String html = String.format("""
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f4f4f5;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f5;padding:32px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:16px;overflow:hidden;border:1px solid #e4e4e7;">

                        <!-- Header -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:28px 32px;text-align:center;">
                            <h1 style="color:#fff;margin:0;font-size:22px;font-weight:700;letter-spacing:-0.02em;">TalentBridge</h1>
                            <p style="color:rgba(255,255,255,0.75);margin:6px 0 0;font-size:13px;">Password Reset Request</p>
                          </td>
                        </tr>

                        <!-- Body -->
                        <tr>
                          <td style="padding:32px;">
                            <p style="color:#0f172a;font-size:15px;margin:0 0 8px;">Hi <strong>%s</strong>,</p>
                            <p style="color:#52525b;font-size:14px;line-height:1.65;margin:0 0 28px;">
                              Someone requested a password reset for your TalentBridge account.
                              Click the button below to set a new password.
                              This link expires in <strong>%d minutes</strong>.
                            </p>

                            <!-- CTA Button -->
                            <table cellpadding="0" cellspacing="0" style="margin:0 auto 28px;">
                              <tr>
                                <td style="background:linear-gradient(135deg,#6366f1,#8b5cf6);border-radius:12px;">
                                  <a href="%s"
                                     style="display:inline-block;color:#fff;text-decoration:none;
                                            padding:14px 36px;font-size:15px;font-weight:600;">
                                    Reset My Password
                                  </a>
                                </td>
                              </tr>
                            </table>

                            <!-- Fallback link -->
                            <p style="color:#94a3b8;font-size:12px;margin:0 0 6px;">
                              Button not working? Copy and paste this link:
                            </p>
                            <p style="color:#6366f1;font-size:12px;word-break:break-all;margin:0 0 28px;">%s</p>

                            <hr style="border:none;border-top:1px solid #f1f5f9;margin:0 0 20px;">
                            <p style="color:#94a3b8;font-size:12px;margin:0;line-height:1.6;">
                              If you didn't request this, you can safely ignore this email.
                              Your password won't change unless you click the link above.
                            </p>
                          </td>
                        </tr>

                        <!-- Footer -->
                        <tr>
                          <td style="background:#f8fafc;padding:16px 32px;text-align:center;border-top:1px solid #f1f5f9;">
                            <p style="color:#a1a1aa;font-size:11px;margin:0;">
                              © 2026 TalentBridge · All rights reserved
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """, name, TOKEN_EXPIRY_MINUTES, resetLink, resetLink);

        try {
            emailService.sendHtmlEmail(to, subject, html);
        } catch (Exception e) {
            log.warn("[FORGOT-PWD] HTML send failed ({}), trying plain text", e.getMessage());
            emailService.sendEmail(to, subject, plainText);
        }
    }
}