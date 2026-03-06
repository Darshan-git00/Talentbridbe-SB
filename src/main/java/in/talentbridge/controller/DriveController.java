package in.talentbridge.controller;

import in.talentbridge.entity.Drive;
import in.talentbridge.service.DriveService;
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
    public ResponseEntity<List<Drive>> getAllDrives() {
        return ResponseEntity.ok(driveService.getAllDrives());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Drive> getDriveById(@PathVariable String id) {
        return driveService.getDriveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<Drive>> getDrivesByCollege(@PathVariable String collegeId) {
        return ResponseEntity.ok(driveService.getDrivesByCollege(collegeId));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Drive>> getDrivesByRecruiter(@PathVariable String recruiterId) {
        return ResponseEntity.ok(driveService.getDrivesByRecruiter(recruiterId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Drive>> getDrivesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(driveService.getDrivesByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Drive> createDrive(@RequestBody Drive drive) {
        return ResponseEntity.ok(driveService.createDrive(drive));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Drive> updateDrive(@PathVariable String id, @RequestBody Drive drive) {
        return ResponseEntity.ok(driveService.updateDrive(id, drive));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrive(@PathVariable String id) {
        driveService.deleteDrive(id);
        return ResponseEntity.noContent().build();
    }
}