package in.talentbridge.service;

import in.talentbridge.dto.DriveRequest;
import in.talentbridge.dto.DriveResponse;
import in.talentbridge.dto.EligibleDriveResponse;
import in.talentbridge.entity.Drive;
import in.talentbridge.entity.Recruiter;
import in.talentbridge.entity.Student;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.mapper.EntityMapper;
import in.talentbridge.repository.CollegeRepository;
import in.talentbridge.repository.DriveRepository;
import in.talentbridge.repository.RecruiterRepository;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepository;
    private final CollegeRepository collegeRepository;
    private final RecruiterRepository recruiterRepository;
    private final StudentRepository studentRepository;
    private final EntityMapper entityMapper;

    public List<DriveResponse> getAllDrives() {
        return driveRepository.findAll()
                .stream()
                .map(entityMapper::toDriveResponse)
                .collect(Collectors.toList());
    }

    public Optional<DriveResponse> getDriveById(String id) {
        return driveRepository.findById(id)
                .map(entityMapper::toDriveResponse);
    }

    /**
     * Returns ALL active drives for the student's college,
     * each annotated with eligibility status and reasons.
     * Eligible drives come first, ineligible come after (grayed out).
     * No CGPA filter — skill-first hiring only.
     */
    public List<EligibleDriveResponse> getEligibleDrives(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        String collegeId = student.getCollege() != null ? student.getCollege().getId() : null;
        if (collegeId == null) return List.of();

        List<Drive> drives = driveRepository.findByCollegeIdAndStatus(collegeId, "ACTIVE");

        List<EligibleDriveResponse> eligible   = new ArrayList<>();
        List<EligibleDriveResponse> ineligible = new ArrayList<>();

        for (Drive drive : drives) {
            List<String> reasons = checkIneligibilityReasons(student, drive);
            EligibleDriveResponse response = entityMapper.toEligibleDriveResponse(drive, reasons.isEmpty(), reasons);
            if (reasons.isEmpty()) {
                eligible.add(response);
            } else {
                ineligible.add(response);
            }
        }

        // Eligible drives first, then ineligible
        eligible.addAll(ineligible);
        return eligible;
    }

    /**
     * Returns a list of human-readable reasons why this student
     * does NOT qualify for the drive. Empty list = fully eligible.
     * CGPA is intentionally excluded — TalentBridge is skill-first.
     */
    private List<String> checkIneligibilityReasons(Student student, Drive drive) {
        List<String> reasons = new ArrayList<>();

        // 1. Branch check
        String[] eligibleBranches = drive.getEligibleBranches();
        if (eligibleBranches != null && eligibleBranches.length > 0) {
            boolean branchMatch = Arrays.stream(eligibleBranches)
                    .anyMatch(b -> b.equalsIgnoreCase(student.getBranch()));
            if (!branchMatch) {
                reasons.add("Branch not eligible (your branch: " + student.getBranch() + ")");
            }
        }

        // 2. Year check
        String[] eligibleYears = drive.getEligibleYears();
        if (eligibleYears != null && eligibleYears.length > 0) {
            boolean yearMatch = Arrays.stream(eligibleYears)
                    .anyMatch(y -> y.equalsIgnoreCase(student.getYear()));
            if (!yearMatch) {
                reasons.add("Year not eligible (your year: " + student.getYear() + ")");
            }
        }

        // 3. Skill score check (no CGPA — skill-first hiring)
        if (drive.getMinSkillScore() > 0 && student.getOverallScore() < drive.getMinSkillScore()) {
            int gap = (int) drive.getMinSkillScore() - student.getOverallScore();
            reasons.add("Need " + gap + " more skill points (yours: "
                    + student.getOverallScore() + ", required: " + (int) drive.getMinSkillScore() + ")");
        }

        return reasons;
    }

    public DriveResponse createDrive(DriveRequest request, String email) {
        Recruiter recruiter = recruiterRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        Drive drive = new Drive();
        drive.setPosition(request.getPosition());
        drive.setDescription(request.getDescription());
        drive.setOpenings(request.getOpenings());
        drive.setSalary(request.getSalary());
        drive.setLocation(request.getLocation());
        drive.setDriveType(request.getDriveType());
        drive.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        drive.setMinSkillScore(request.getMinSkillScore());
        drive.setEligibleBranches(request.getEligibleBranches());
        drive.setEligibleYears(request.getEligibleYears());
        drive.setDriveDate(request.getDriveDate());
        drive.setLastDateToApply(request.getLastDateToApply());
        drive.setRecruiter(recruiter);
        drive.setCollege(recruiter.getCollege());

        return entityMapper.toDriveResponse(driveRepository.save(drive));
    }

    public DriveResponse updateDrive(String id, DriveRequest request) {
        return driveRepository.findById(id).map(drive -> {
            drive.setPosition(request.getPosition());
            drive.setDescription(request.getDescription());
            drive.setOpenings(request.getOpenings());
            drive.setSalary(request.getSalary());
            drive.setLocation(request.getLocation());
            drive.setDriveType(request.getDriveType());
            drive.setStatus(request.getStatus());
            drive.setMinSkillScore(request.getMinSkillScore());
            drive.setEligibleBranches(request.getEligibleBranches());
            drive.setEligibleYears(request.getEligibleYears());
            drive.setDriveDate(request.getDriveDate());
            drive.setLastDateToApply(request.getLastDateToApply());

            if (request.getRecruiterId() != null) {
                recruiterRepository.findById(request.getRecruiterId())
                        .ifPresent(drive::setRecruiter);
            }
            if (request.getCollegeId() != null) {
                collegeRepository.findById(request.getCollegeId())
                        .ifPresent(drive::setCollege);
            }

            return entityMapper.toDriveResponse(driveRepository.save(drive));
        }).orElseThrow(() -> new RuntimeException("Drive not found"));
    }

    public void deleteDrive(String id) {
        driveRepository.deleteById(id);
    }

    public List<DriveResponse> getDrivesByCollege(String collegeId) {
        return driveRepository.findByCollegeId(collegeId)
                .stream()
                .map(entityMapper::toDriveResponse)
                .collect(Collectors.toList());
    }

    public List<DriveResponse> getDrivesByRecruiter(String recruiterId) {
        return driveRepository.findByRecruiterId(recruiterId)
                .stream()
                .map(entityMapper::toDriveResponse)
                .collect(Collectors.toList());
    }

    public List<DriveResponse> getDrivesByStatus(String status) {
        return driveRepository.findByStatus(status)
                .stream()
                .map(entityMapper::toDriveResponse)
                .collect(Collectors.toList());
    }
}