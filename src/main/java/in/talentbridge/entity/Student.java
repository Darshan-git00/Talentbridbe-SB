package in.talentbridge.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

    @Entity
    @Table(name = "students")
    @Data
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

        // Scores derived from those profiles
        private int githubScore;
        private int hackerrankScore;


        // Overall computed score combining everything
        private int overallScore;
        private LocalDateTime createdAt;

        @ManyToOne
        @JoinColumn(name = "college_id")
        private College college;

        @OneToMany(mappedBy = "student")
        private List<Application> applications;

        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
        }

}
