package in.talentbridge.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
        private String year;   // String, not int (as per Prisma schema)
        private String status;
        private double cgpa;

        // PostgreSQL array stored as text array
        @Column(columnDefinition = "text[]")
        private String[] skills;

        @Column(columnDefinition = "text[]")
        private String[] certifications;

        private int aiInterviewScore;
        private int skillMatchPercentage;
        private int projectExperience;
        // External profiles
        private String githubProfile;
        private String hackerrankProfile;
        private String linkedinProfile;
        private String portfolioUrl;

        public int getPlatformScore() {
            return PlatformScore;
        }

        public void setPlatformScore(int platformScore) {
            PlatformScore = platformScore;
        }

        private int PlatformScore;

        // Scores derived from those profiles
        private int githubScore;
        private int hackerrankScore;



        // Overall computed score combining everything
        private int overallScore;
        private LocalDateTime createdAt;

        @ManyToOne
        @JoinColumn(name = "college_id")
        private College college;

    @Column(name = "resume_url")
    private String resumeUrl;

        public void setresumeUrl(String resumeUrl) {
            this.resumeUrl = resumeUrl;
        }

        @JsonIgnore
        @OneToMany(mappedBy = "student")
        private List<Application> applications;

        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
        }

        public void setResumeUrl(String resumeUrl) {
        }
    }
