package in.talentbridge.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String status; // APPLIED, SHORTLISTED, REJECTED, HIRED

    private LocalDateTime appliedDate;
    private LocalDateTime updatedAt;

    // Recruiter feedback
    private String feedback;

    // Interview details
    private LocalDateTime interviewDate;
    private String interviewMode; // ONLINE, OFFLINE

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "drive_id")
    private Drive drive;

    @PrePersist
    protected void onCreate() {
        appliedDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = "APPLIED";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}