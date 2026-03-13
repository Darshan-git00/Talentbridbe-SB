package in.talentbridge.controller;

import in.talentbridge.dto.CollegeRequest;
import in.talentbridge.dto.CollegeResponse;
import in.talentbridge.dto.profile.ChangePasswordRequest;
import in.talentbridge.dto.profile.CollegeProfileRequest;
import in.talentbridge.service.CollegeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colleges")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;

    @GetMapping
    public ResponseEntity<List<CollegeResponse>> getAllColleges() {
        return ResponseEntity.ok(collegeService.getAllColleges());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollegeResponse> getCollegeById(@PathVariable String id) {
        return collegeService.getCollegeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CollegeResponse> createCollege(@Valid @RequestBody CollegeRequest request) {
        return ResponseEntity.ok(collegeService.createCollege(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollegeResponse> updateCollege(@PathVariable String id, @Valid @RequestBody CollegeRequest request) {
        return ResponseEntity.ok(collegeService.updateCollege(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollege(@PathVariable String id) {
        collegeService.deleteCollege(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/profile")
    public ResponseEntity<CollegeResponse> updateProfile(
            @PathVariable String id,
            @Valid @RequestBody CollegeProfileRequest request) {
        return ResponseEntity.ok(collegeService.updateCollegeProfile(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String id,
            @Valid @RequestBody ChangePasswordRequest request) {
        collegeService.changeCollegePassword(id, request);
        return ResponseEntity.ok().build();
    }
}