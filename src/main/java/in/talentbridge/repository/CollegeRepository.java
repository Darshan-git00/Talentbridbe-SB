package in.talentbridge.repository;

import in.talentbridge.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, String> {
    Optional<College> findByEmail(String email);
    Optional<College> findByPasswordResetToken(String token);
}