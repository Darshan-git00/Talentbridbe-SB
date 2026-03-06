package in.talentbridge.service;

import in.talentbridge.entity.Drive;
import in.talentbridge.repository.DriveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepository;

    public List<Drive> getAllDrives() {
        return driveRepository.findAll();
    }

    public Optional<Drive> getDriveById(String id) {
        return driveRepository.findById(id);
    }

    public Drive createDrive(Drive drive) {
        return driveRepository.save(drive);
    }

    public Drive updateDrive(String id, Drive updatedDrive) {
        return driveRepository.findById(id).map(drive -> {
            drive.setPosition(updatedDrive.getPosition());
            drive.setDescription(updatedDrive.getDescription());
            drive.setOpenings(updatedDrive.getOpenings());
            drive.setSalary(updatedDrive.getSalary());
            drive.setLocation(updatedDrive.getLocation());
            drive.setDriveType(updatedDrive.getDriveType());
            drive.setStatus(updatedDrive.getStatus());
            drive.setMinSkillScore(updatedDrive.getMinSkillScore());
            drive.setEligibleBranches(updatedDrive.getEligibleBranches());
            drive.setEligibleYears(updatedDrive.getEligibleYears());
            drive.setDriveDate(updatedDrive.getDriveDate());
            drive.setLastDateToApply(updatedDrive.getLastDateToApply());
            return driveRepository.save(drive);
        }).orElseThrow(() -> new RuntimeException("Drive not found"));
    }

    public void deleteDrive(String id) {
        driveRepository.deleteById(id);
    }

    public List<Drive> getDrivesByCollege(String collegeId) {
        return driveRepository.findByCollegeId(collegeId);
    }

    public List<Drive> getDrivesByRecruiter(String recruiterId) {
        return driveRepository.findByRecruiterId(recruiterId);
    }

    public List<Drive> getDrivesByStatus(String status) {
        return driveRepository.findByStatus(status);
    }
}