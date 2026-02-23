package in.talentbridge.repository;

import in.talentbridge.entity.Drive;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DriveRepository extends JpaRepository<Drive, String> {
    List<Drive> findByStatus(String status);
    List<Drive> findByCollegeId(String collegeId);
    List<Drive> findByRecruiterId(String recruiterId);
}