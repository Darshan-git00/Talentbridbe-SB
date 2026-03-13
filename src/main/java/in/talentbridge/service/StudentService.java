package in.talentbridge.service;

import in.talentbridge.dto.StudentRequest;
import in.talentbridge.dto.StudentResponse;
import in.talentbridge.dto.profile.ChangePasswordRequest;
import in.talentbridge.entity.Student;
import in.talentbridge.exception.DuplicateResourceException;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.mapper.EntityMapper;
import in.talentbridge.repository.CollegeRepository;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import in.talentbridge.dto.profile.StudentProfileRequest;
import in.talentbridge.dto.profile.ChangePasswordRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final CollegeRepository collegeRepository;
    private final EntityMapper entityMapper;
    private final BCryptPasswordEncoder passwordEncoder;

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
        student.setPasswordHash(passwordEncoder.encode(request.getPassword()));
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
    public StudentResponse updateStudentProfile(String id, StudentProfileRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        student.setName(request.getName());
        if (request.getCourse() != null) student.setCourse(request.getCourse());
        if (request.getBranch() != null) student.setBranch(request.getBranch());
        if (request.getYear() != null) student.setYear(String.valueOf(request.getYear()));
        if (request.getCgpa() != null) student.setCgpa(request.getCgpa());
        if (request.getSkills() != null) student.setSkills(request.getSkills());
        if (request.getCertifications() != null) student.setCertifications(request.getCertifications());
        if (request.getGithubProfile() != null) student.setGithubProfile(request.getGithubProfile());
        if (request.getHackerrankProfile() != null) student.setHackerrankProfile(request.getHackerrankProfile());
        if (request.getLinkedinProfile() != null) student.setLinkedinProfile(request.getLinkedinProfile());
        if (request.getPortfolioUrl() != null) student.setPortfolioUrl(request.getPortfolioUrl());

        return entityMapper.toStudentResponse(studentRepository.save(student));
    }

    public void changeStudentPassword(String id, ChangePasswordRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), student.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        student.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        studentRepository.save(student);
    }
}