package in.talentbridge.service;

import in.talentbridge.dto.*;
import in.talentbridge.entity.College;
import in.talentbridge.entity.Recruiter;
import in.talentbridge.entity.Student;
import in.talentbridge.exception.DuplicateResourceException;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.repository.CollegeRepository;
import in.talentbridge.repository.RecruiterRepository;
import in.talentbridge.repository.StudentRepository;
import in.talentbridge.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentRepository studentRepository;
    private final RecruiterRepository recruiterRepository;
    private final CollegeRepository collegeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // LOGIN
    public AuthResponse login(LoginRequest request) {
        switch (request.getRole().toUpperCase()) {
            case "STUDENT" -> {
                Student student = studentRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + request.getEmail()));
                if (!passwordEncoder.matches(request.getPassword(), student.getPasswordHash())) {
                    throw new IllegalArgumentException("Invalid password");
                }
                String token = jwtUtil.generateToken(student.getEmail(), "STUDENT", student.getId());
                return new AuthResponse(token, "STUDENT", student.getId(), student.getName(), student.getEmail());
            }
            case "RECRUITER" -> {
                Recruiter recruiter = recruiterRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with email: " + request.getEmail()));
                if (!passwordEncoder.matches(request.getPassword(), recruiter.getPasswordHash())) {
                    throw new IllegalArgumentException("Invalid password");
                }
                String token = jwtUtil.generateToken(recruiter.getEmail(), "RECRUITER", recruiter.getId());
                return new AuthResponse(token, "RECRUITER", recruiter.getId(), recruiter.getName(), recruiter.getEmail());
            }
            case "COLLEGE" -> {
                College college = collegeRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("College not found with email: " + request.getEmail()));
                if (!passwordEncoder.matches(request.getPassword(), college.getPasswordHash())) {
                    throw new IllegalArgumentException("Invalid password");
                }
                String token = jwtUtil.generateToken(college.getEmail(), "COLLEGE", college.getId());
                return new AuthResponse(token, "COLLEGE", college.getId(), college.getName(), college.getEmail());
            }
            default -> throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
    }

    // SIGNUP STUDENT
    public AuthResponse signupStudent(StudentRequest request) {
        if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Student with email " + request.getEmail() + " already exists");
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
        student.setStatus("ACTIVE");
        student.setGithubProfile(request.getGithubProfile());
        student.setCompetitiveProgrammingProfile(request.getCompetitiveProgrammingProfile());
        student.setLinkedinProfile(request.getLinkedinProfile());
        student.setPortfolioUrl(request.getPortfolioUrl());     

        if (request.getCollegeId() != null) {
            collegeRepository.findById(request.getCollegeId())
                    .ifPresent(student::setCollege);
        }

        Student saved = studentRepository.save(student);
        String token = jwtUtil.generateToken(saved.getEmail(), "STUDENT", saved.getId());
        return new AuthResponse(token, "STUDENT", saved.getId(), saved.getName(), saved.getEmail());
    }

    // SIGNUP RECRUITER
    public AuthResponse signupRecruiter(RecruiterRequest request) {
        if (recruiterRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Recruiter with email " + request.getEmail() + " already exists");
        }

        Recruiter recruiter = new Recruiter();
        recruiter.setName(request.getName());
        recruiter.setEmail(request.getEmail());
        recruiter.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        recruiter.setCompany(request.getCompany());
        recruiter.setDesignation(request.getDesignation());
        recruiter.setPhone(request.getPhone());
        recruiter.setCompanyWebsite(request.getCompanyWebsite());
        recruiter.setCompanyLocation(request.getCompanyLocation());
        recruiter.setIndustry(request.getIndustry());
        recruiter.setLinkedinProfile(request.getLinkedinProfile());
        recruiter.setStatus("ACTIVE");

        if (request.getCollegeId() != null) {
            collegeRepository.findById(request.getCollegeId())
                    .ifPresent(recruiter::setCollege);
        }

        Recruiter saved = recruiterRepository.save(recruiter);
        String token = jwtUtil.generateToken(saved.getEmail(), "RECRUITER", saved.getId());
        return new AuthResponse(token, "RECRUITER", saved.getId(), saved.getName(), saved.getEmail());
    }

    // SIGNUP COLLEGE
    public AuthResponse signupCollege(CollegeRequest request) {
        if (collegeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("College with email " + request.getEmail() + " already exists");
        }

        College college = new College();
        college.setName(request.getName());
        college.setEmail(request.getEmail());
        college.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        college.setLocation(request.getLocation());

        College saved = collegeRepository.save(college);
        String token = jwtUtil.generateToken(saved.getEmail(), "COLLEGE", saved.getId());
        return new AuthResponse(token, "COLLEGE", saved.getId(), saved.getName(), saved.getEmail());
    }

    public AuthResponse getMe(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        switch (role.toUpperCase()) {
            case "STUDENT" -> {
                Student foundStudent = studentRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
                return new AuthResponse(token, "STUDENT", foundStudent.getId(), foundStudent.getName(), foundStudent.getEmail());
            }
            case "RECRUITER" -> {
                Recruiter foundRecruiter = recruiterRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));
                return new AuthResponse(token, "RECRUITER", foundRecruiter.getId(), foundRecruiter.getName(), foundRecruiter.getEmail());
            }
            case "COLLEGE" -> {
                College foundCollege = collegeRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("College not found"));
                return new AuthResponse(token, "COLLEGE", foundCollege.getId(), foundCollege.getName(), foundCollege.getEmail());
            }
            default -> throw new IllegalArgumentException("Invalid role");
        }
    }


}