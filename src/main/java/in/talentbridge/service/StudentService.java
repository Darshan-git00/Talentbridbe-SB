package in.talentbridge.service;

import in.talentbridge.entity.Student;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student updateStudent(String id, Student updatedStudent) {
        return studentRepository.findById(id).map(student -> {
            student.setName(updatedStudent.getName());
            student.setEmail(updatedStudent.getEmail());
            student.setCourse(updatedStudent.getCourse());
            student.setBranch(updatedStudent.getBranch());
            student.setYear(updatedStudent.getYear());
            student.setCgpa(updatedStudent.getCgpa());
            student.setSkills(updatedStudent.getSkills());
            student.setStatus(updatedStudent.getStatus());
            student.setGithubProfile(updatedStudent.getGithubProfile());
            student.setLinkedinProfile(updatedStudent.getLinkedinProfile());
            student.setPortfolioUrl(updatedStudent.getPortfolioUrl());
            student.setPlatformScore(updatedStudent.getPlatformScore());
            return studentRepository.save(student);
        }).orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }

    public List<Student> getStudentsByCollege(String collegeId) {
        return studentRepository.findByCollegeId(collegeId);
    }
}