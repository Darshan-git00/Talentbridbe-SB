package in.talentbridge.controller;

import in.talentbridge.entity.Student;
import in.talentbridge.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable String id) {
        return studentService.getStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/college/{collegeId}")
    public ResponseEntity<List<Student>> getStudentsByCollege(@PathVariable String collegeId) {
        return ResponseEntity.ok(studentService.getStudentsByCollege(collegeId));
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return ResponseEntity.ok(studentService.createStudent(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable String id, @RequestBody Student student) {
        return ResponseEntity.ok(studentService.updateStudent(id, student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}

//GET    http://localhost:8080/api/students
//GET    http://localhost:8080/api/students/{id}
//GET    http://localhost:8080/api/students/college/{collegeId}
//POST   http://localhost:8080/api/students
//PUT    http://localhost:8080/api/students/{id}
//DELETE http://localhost:8080/api/students/{id}