package in.talentbridge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;
    private String course;
    private String branch;
    private String year;
    private String status;
    private double cgpa;

    @Column(columnDefinition = "text[]")
    private String[] skills;

    @Column(columnDefinition = "text[]")
    private String[] certifications;

    private int aiInterviewScore;
    private int skillMatchPercentage;
    private int projectExperience;

    // External profiles
    private String githubProfile;
    private String linkedinProfile;
    private String portfolioUrl;

    @Column(name = "competitive_programming_profile")
    private String competitiveProgrammingProfile;

    // Scores
    private int githubScore;

    // ── FIXED: was 'PlatformScore' (capital P) — renamed to camelCase ──────
    // Maps to the existing platform_score column (Hibernate auto-maps camelCase → snake_case)
    private int platformScore;

    // ── NEW: maps to competitive_programming_score column you just created ──
    @Column(name = "competitive_programming_score")
    private Integer  competitiveProgrammingScore;

    private int overallScore;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "college_id")
    private College college;

    @Column(name = "resume_url")
    private String resumeUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "student")
    private List<Application> applications;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;
}