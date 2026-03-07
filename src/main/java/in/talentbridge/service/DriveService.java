package in.talentbridge.service;

import in.talentbridge.dto.DriveRequest;
import in.talentbridge.dto.DriveResponse;
import in.talentbridge.entity.Drive;
import in.talentbridge.mapper.EntityMapper;
import in.talentbridge.repository.CollegeRepository;
import in.talentbridge.repository.DriveRepository;
import in.talentbridge.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriveService {

    private final DriveRepository driveRepository;
    private final CollegeRepository collegeRepository;
    private final RecruiterRepository recruiterRepository;
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

    public DriveResponse createDrive(DriveRequest request) {
        Drive drive = new Drive();
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