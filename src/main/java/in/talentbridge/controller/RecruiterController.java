package in.talentbridge.controller;

import in.talentbridge.entity.Recruiter;
import in.talentbridge.service.RecruiterService;
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
    public ResponseEntity<List<Recruiter>> getAllRecruiters() {
        return ResponseEntity.ok(recruiterService.getAllRecruiters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recruiter> getRecruiterById(@PathVariable String id) {
        return recruiterService.getRecruiterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<Recruiter>> getRecruitersByCollege(@PathVariable String collegeId) {
        return ResponseEntity.ok(recruiterService.getRecruitersByCollege(collegeId));
    }

    @PostMapping
    public ResponseEntity<Recruiter> createRecruiter(@RequestBody Recruiter recruiter) {
        return ResponseEntity.ok(recruiterService.createRecruiter(recruiter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recruiter> updateRecruiter(@PathVariable String id, @RequestBody Recruiter recruiter) {
        return ResponseEntity.ok(recruiterService.updateRecruiter(id, recruiter));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecruiter(@PathVariable String id) {
        recruiterService.deleteRecruiter(id);
        return ResponseEntity.noContent().build();
    }
}