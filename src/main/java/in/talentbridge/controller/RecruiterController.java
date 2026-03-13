package in.talentbridge.controller;

import in.talentbridge.dto.RecruiterRequest;
import in.talentbridge.dto.RecruiterResponse;
import in.talentbridge.dto.profile.ChangePasswordRequest;
import in.talentbridge.dto.profile.RecruiterProfileRequest;
import in.talentbridge.service.RecruiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiters")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;

    @GetMapping
    public ResponseEntity<List<RecruiterResponse>> getAllRecruiters() {
        return ResponseEntity.ok(recruiterService.getAllRecruiters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecruiterResponse> getRecruiterById(@PathVariable String id) {
        return recruiterService.getRecruiterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<RecruiterResponse>> getRecruitersByCollege(@PathVariable String collegeId) {
        return ResponseEntity.ok(recruiterService.getRecruitersByCollege(collegeId));
    }

    @PostMapping
    public ResponseEntity<RecruiterResponse> createRecruiter(@Valid @RequestBody RecruiterRequest request) {
        return ResponseEntity.ok(recruiterService.createRecruiter(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecruiterResponse> updateRecruiter(@PathVariable String id, @Valid @RequestBody RecruiterRequest request) {
        return ResponseEntity.ok(recruiterService.updateRecruiter(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecruiter(@PathVariable String id) {
        recruiterService.deleteRecruiter(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/profile")
    public ResponseEntity<RecruiterResponse> updateProfile(
            @PathVariable String id,
            @Valid @RequestBody RecruiterProfileRequest request) {
        return ResponseEntity.ok(recruiterService.updateRecruiterProfile(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String id,
            @Valid @RequestBody ChangePasswordRequest request) {
        recruiterService.changeRecruiterPassword(id, request);
        return ResponseEntity.ok().build();
    }
}