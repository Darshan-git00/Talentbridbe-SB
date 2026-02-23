package in.talentbridge.repository;

import in.talentbridge.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, String> {
    List<Application> findByStudentId(String studentId);
    List<Application> findByDriveId(String driveId);
    List<Application> findByStatus(String status);
}