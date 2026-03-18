package in.talentbridge.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * Plain-text email — used by ForgotPasswordService.
     */
    public void sendEmail(String to, String subject, String body) {
        log.info("[EMAIL] Sending to: {} | Subject: {}", to, subject);
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("[EMAIL] Sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("[EMAIL] FAILED to send to: {} | Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    /**
     * HTML email — nicer looking reset emails.
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        log.info("[EMAIL] Sending HTML email to: {} | Subject: {}", to, subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("[EMAIL] HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("[EMAIL] FAILED HTML email to: {} | Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }
}