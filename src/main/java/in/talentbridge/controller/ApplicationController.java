package in.talentbridge.controller;

import in.talentbridge.entity.Application;
import in.talentbridge.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable String id) {
        return applicationService.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Application>> getApplicationsByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(applicationService.getApplicationsByStudent(studentId));
    }

    @GetMapping("/drive/{driveId}")
    public ResponseEntity<List<Application>> getApplicationsByDrive(@PathVariable String driveId) {
        return ResponseEntity.ok(applicationService.getApplicationsByDrive(driveId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Application>> getApplicationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(applicationService.getApplicationsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        return ResponseEntity.ok(applicationService.createApplication(application));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Application> updateStatus(@PathVariable String id, @RequestParam String status) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}