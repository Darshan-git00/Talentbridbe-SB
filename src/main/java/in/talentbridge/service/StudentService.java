package in.talentbridge.service;

import in.talentbridge.dto.StudentRequest;
import in.talentbridge.dto.StudentResponse;
import in.talentbridge.entity.Student;
import in.talentbridge.exception.DuplicateResourceException;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.mapper.EntityMapper;
import in.talentbridge.repository.CollegeRepository;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final EntityMapper entityMapper;

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(entityMapper::toStudentResponse)
                .collect(Collectors.toList());
    }

    public Optional<StudentResponse> getStudentById(String id) {
        return studentRepository.findById(id)
                .map(entityMapper::toStudentResponse);
    }

    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Student with email " + request.getEmail() + " already exists, try with different one!");
        }
        Student student = new Student();
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPasswordHash(request.getPassword());
        student.setCourse(request.getCourse());
        student.setBranch(request.getBranch());
        student.setYear(request.getYear());
        student.setCgpa(request.getCgpa());
        student.setSkills(request.getSkills());
        student.setCertifications(request.getCertifications());
        student.setStatus(request.getStatus());
        student.setGithubProfile(request.getGithubProfile());
        student.setHackerrankProfile(request.getHackerrankProfile());
        student.setLinkedinProfile(request.getLinkedinProfile());
        student.setPortfolioUrl(request.getPortfolioUrl());

        // set college
        if (request.getCollegeId() != null) {
            collegeRepository.findById(request.getCollegeId())
                    .ifPresent(student::setCollege);
        }

        return entityMapper.toStudentResponse(studentRepository.save(student));
    }

    public StudentResponse updateStudent(String id, StudentRequest request) {

        return studentRepository.findById(id).map(student -> {
            student.setName(request.getName());
            student.setEmail(request.getEmail());
            student.setCourse(request.getCourse());
            student.setBranch(request.getBranch());
            student.setYear(request.getYear());
            student.setCgpa(request.getCgpa());
            student.setSkills(request.getSkills());
            student.setCertifications(request.getCertifications());
            student.setStatus(request.getStatus());
            student.setGithubProfile(request.getGithubProfile());
            student.setHackerrankProfile(request.getHackerrankProfile());
            student.setLinkedinProfile(request.getLinkedinProfile());
            student.setPortfolioUrl(request.getPortfolioUrl());

            if (request.getCollegeId() != null) {
                collegeRepository.findById(request.getCollegeId())
                        .ifPresent(student::setCollege);
            }

            return entityMapper.toStudentResponse(studentRepository.save(student));
        }).orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " not found"));
    }

    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }

    public List<StudentResponse> getStudentsByCollege(String collegeId) {
        return studentRepository.findByCollegeId(collegeId)
                .stream()
                .map(entityMapper::toStudentResponse)
                .collect(Collectors.toList());
    }
}