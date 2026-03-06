package in.talentbridge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

    @Entity
    @Table(name = "drives")
    @Data
    public class Drive {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private String id;

        @Column(nullable = false)
        private String position;

        private String description;
        private int openings;
        private String salary;
        private String location;
        private String driveType; // ON_CAMPUS, OFF_CAMPUS, VIRTUAL

        private String status; // UPCOMING, ACTIVE, CLOSED

        // Eligibility criteria
        private double minSkillScore;
        private String eligibleBranches; // store as comma separated
        private String eligibleYears;

        private LocalDateTime driveDate;
        private LocalDateTime lastDateToApply;
        private LocalDateTime createdAt;

        @ManyToOne
        @JoinColumn(name = "recruiter_id")
        private Recruiter recruiter;

        @ManyToOne
        @JoinColumn(name = "college_id")
        private College college;

        @JsonIgnore
        @OneToMany(mappedBy = "drive")
        private List<Application> applications;

        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
        }

}
