package in.talentbridge.repository;

import in.talentbridge.entity.Recruiter;
import in.talentbridge.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecruiterRepository extends JpaRepository<Recruiter, String> {
    Optional<Recruiter> findByEmail(String email);

    List<Recruiter> findByCollegeId(String collegeId);
    Optional<Recruiter> findByPasswordResetToken(String token);
}