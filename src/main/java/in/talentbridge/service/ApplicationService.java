package in.talentbridge.service;

import in.talentbridge.entity.Application;
import in.talentbridge.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Optional<Application> getApplicationById(String id) {
        return applicationRepository.findById(id);
    }

    public Application createApplication(Application application) {
        return applicationRepository.save(application);
    }

    public Application updateApplicationStatus(String id, String status) {
        return applicationRepository.findById(id).map(application -> {
            application.setStatus(status);
            return applicationRepository.save(application);
        }).orElseThrow(() -> new RuntimeException("Application not found"));
    }

    public void deleteApplication(String id) {
        applicationRepository.deleteById(id);
    }

    public List<Application> getApplicationsByStudent(String studentId) {
        return applicationRepository.findByStudentId(studentId);
    }

    public List<Application> getApplicationsByDrive(String driveId) {
        return applicationRepository.findByDriveId(driveId);
    }

    public List<Application> getApplicationsByStatus(String status) {
        return applicationRepository.findByStatus(status);
    }
}