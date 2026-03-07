package in.talentbridge.controller;

import in.talentbridge.dto.DriveRequest;
import in.talentbridge.dto.DriveResponse;
import in.talentbridge.service.DriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drives")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;

    @GetMapping
    public ResponseEntity<List<DriveResponse>> getAllDrives() {
        return ResponseEntity.ok(driveService.getAllDrives());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriveResponse> getDriveById(@PathVariable String id) {
        return driveService.getDriveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<DriveResponse>> getDrivesByCollege(@PathVariable String collegeId) {
        return ResponseEntity.ok(driveService.getDrivesByCollege(collegeId));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<DriveResponse>> getDrivesByRecruiter(@PathVariable String recruiterId) {
        return ResponseEntity.ok(driveService.getDrivesByRecruiter(recruiterId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DriveResponse>> getDrivesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(driveService.getDrivesByStatus(status));
    }

    @PostMapping
    public ResponseEntity<DriveResponse> createDrive(@Valid @RequestBody DriveRequest request) {
        return ResponseEntity.ok(driveService.createDrive(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriveResponse> updateDrive(@PathVariable String id, @Valid @RequestBody DriveRequest request) {
        return ResponseEntity.ok(driveService.updateDrive(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrive(@PathVariable String id) {
        driveService.deleteDrive(id);
        return ResponseEntity.noContent().build();
    }
}