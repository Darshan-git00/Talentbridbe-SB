package in.talentbridge.service;

import in.talentbridge.dto.ApplicationRequest;
import in.talentbridge.dto.ApplicationResponse;
import in.talentbridge.entity.Application;
import in.talentbridge.exception.DuplicateResourceException;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.mapper.EntityMapper;
import in.talentbridge.repository.ApplicationRepository;
import in.talentbridge.repository.DriveRepository;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final DriveRepository driveRepository;
    private final EntityMapper entityMapper;

    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(entityMapper::toApplicationResponse)
                .collect(Collectors.toList());
    }

    public Optional<ApplicationResponse> getApplicationById(String id) {
        return applicationRepository.findById(id)
                .map(entityMapper::toApplicationResponse);
    }

    public ApplicationResponse createApplication(ApplicationRequest request) {

        List<Application> existing = applicationRepository.findByStudentId(request.getStudentId());
        boolean alreadyApplied = existing.stream()
                .anyMatch(app -> app.getDrive().getId().equals(request.getDriveId()));
        if (alreadyApplied) {
            throw new DuplicateResourceException("Student has already applied to this drive");
        }
        Application application = new Application();

        if (request.getStudentId() != null) {
            studentRepository.findById(request.getStudentId())
                    .ifPresent(application::setStudent);
        }

        if (request.getDriveId() != null) {
            driveRepository.findById(request.getDriveId())
                    .ifPresent(application::setDrive);
        }

        return entityMapper.toApplicationResponse(applicationRepository.save(application));
    }

    public ApplicationResponse updateApplicationStatus(String id, String status) {
        return applicationRepository.findById(id).map(application -> {
            application.setStatus(status);
            return entityMapper.toApplicationResponse(applicationRepository.save(application));
        }).orElseThrow(() -> new ResourceNotFoundException("Application with id " + id + " not found"));
    }

    public void deleteApplication(String id) {
        applicationRepository.deleteById(id);
    }

    public List<ApplicationResponse> getApplicationsByStudent(String studentId) {
        return applicationRepository.findByStudentId(studentId)
                .stream()
                .map(entityMapper::toApplicationResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsByDrive(String driveId) {
        return applicationRepository.findByDriveId(driveId)
                .stream()
                .map(entityMapper::toApplicationResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsByStatus(String status) {
        return applicationRepository.findByStatus(status)
                .stream()
                .map(entityMapper::toApplicationResponse)
                .collect(Collectors.toList());
    }
}