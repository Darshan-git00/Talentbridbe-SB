package in.talentbridge.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

    @Entity
    @Table(name = "recruiters")
    @Data
    public class Recruiter {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private String id;

        @Column(nullable = false)
        private String name;

        @Column(unique = true, nullable = false)
        private String email;

        private String passwordHash;
        private String company;
        private String designation;
        private String phone;

        // Company details
        private String companyWebsite;
        private String companyLocation;
        private String industry;

        // Profile
        private String linkedinProfile;
        private String profilePicture;

        private String status; // ACTIVE, INACTIVE

        private LocalDateTime createdAt;

        @ManyToOne
        @JoinColumn(name = "college_id")
        private College college;

        @JsonIgnore
        @OneToMany(mappedBy = "recruiter")
        private List<Drive> drives;

        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
        }
    }

