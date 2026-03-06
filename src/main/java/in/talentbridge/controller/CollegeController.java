package in.talentbridge.controller;


import in.talentbridge.entity.College;
import in.talentbridge.service.CollegeService;
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
    public ResponseEntity<List<College>> getAllColleges() {
        return ResponseEntity.ok(collegeService.getAllColleges());
    }

    @GetMapping("/{id}")
    public ResponseEntity<College> getCollegeById(@PathVariable String id) {
        return collegeService.getCollegeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<College> createCollege(@RequestBody College college) {
        return ResponseEntity.ok(collegeService.createCollege(college));
    }

    @PutMapping("/{id}")
    public ResponseEntity<College> updateCollege(@PathVariable String id, @RequestBody College college) {
        return ResponseEntity.ok(collegeService.updateCollege(id, college));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollege(@PathVariable String id) {
        collegeService.deleteCollege(id);
        return ResponseEntity.noContent().build();
    }
}
//```
//
//Now restart the app and test these endpoints in Postman:
//        ```
//GET    http://localhost:8080/api/colleges
//GET    http://localhost:8080/api/colleges/{id}
//POST   http://localhost:8080/api/colleges
//PUT    http://localhost:8080/api/colleges/{id}
//DELETE http://localhost:8080/api/colleges/{id}
