package in.talentbridge.repository;

import in.talentbridge.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecruiterRepository extends JpaRepository<Recruiter, String> {
    Optional<Recruiter> findByEmail(String email);
}