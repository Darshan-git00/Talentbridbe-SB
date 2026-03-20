package in.talentbridge.repository;

import in.talentbridge.entity.Student;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByEmail(String email);

    List<Student> findByCollegeId(String collegeId);

    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.resumeUrl = :resumeUrl, s.skills = :skills, s.platformScore = :platformScore WHERE s.id = :studentId")
    void updateResumeDetails(
            @Param("studentId") String studentId,
            @Param("resumeUrl") String resumeUrl,
            @Param("skills") String[] skills,
            @Param("platformScore") int platformScore,
            @Param("overallScore") int overallScore);

    @Modifying
    @Transactional
    @Query("UPDATE Student s SET s.githubScore = :githubScore, s.overallScore = :overallScore WHERE s.id = :studentId")
    void updateGitHubDetails(
            @Param("studentId") String studentId,
            @Param("githubScore") int githubScore,
            @Param("overallScore") int overallScore
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE Student s
        SET s.platformScore = :platformScore,
            s.competitiveProgrammingScore = :platformScore,
            s.overallScore = :overallScore
        WHERE s.id = :studentId
        """)
    void updatePlatformDetails(
            @Param("studentId")     String studentId,
            @Param("platformScore") int    platformScore,
            @Param("overallScore")  int    overallScore
    );

    Optional<Student> findByPasswordResetToken(String token);
}