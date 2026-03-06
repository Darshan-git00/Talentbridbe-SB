package in.talentbridge.repository;

import in.talentbridge.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByEmail(String email);

    List<Student> findByCollegeId(String collegeId);
}