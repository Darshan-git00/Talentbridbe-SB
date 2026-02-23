package in.talentbridge.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "colleges")
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    private String location;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "college")
    private List<Student> students;

    @OneToMany(mappedBy = "college")
    private List<Recruiter> recruiters;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}