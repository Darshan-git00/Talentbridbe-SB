package in.talentbridge.service;

import in.talentbridge.dto.RecruiterRequest;
import in.talentbridge.dto.RecruiterResponse;
import in.talentbridge.dto.profile.RecruiterProfileRequest;
import in.talentbridge.entity.Recruiter;
import in.talentbridge.exception.DuplicateResourceException;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.mapper.EntityMapper;
import in.talentbridge.repository.CollegeRepository;
import in.talentbridge.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import in.talentbridge.dto.profile.RecruiterProfileRequest;
import in.talentbridge.dto.profile.ChangePasswordRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private final RecruiterRepository recruiterRepository;
    private final CollegeRepository collegeRepository;
    private final EntityMapper entityMapper;
    private final BCryptPasswordEncoder passwordEncoder;



    public List<RecruiterResponse> getAllRecruiters() {
        return recruiterRepository.findAll()
                .stream()
                .map(entityMapper::toRecruiterResponse)
                .collect(Collectors.toList());
    }

    public Optional<RecruiterResponse> getRecruiterById(String id) {
        return recruiterRepository.findById(id)
                .map(entityMapper::toRecruiterResponse);
    }

    public RecruiterResponse createRecruiter(RecruiterRequest request) {
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

        return entityMapper.toRecruiterResponse(recruiterRepository.save(recruiter));
    }

    public RecruiterResponse updateRecruiter(String id, RecruiterRequest request) {
        return recruiterRepository.findById(id).map(recruiter -> {
            recruiter.setName(request.getName());
            recruiter.setEmail(request.getEmail());
            recruiter.setCompany(request.getCompany());
            recruiter.setDesignation(request.getDesignation());
            recruiter.setPhone(request.getPhone());
            recruiter.setCompanyWebsite(request.getCompanyWebsite());
            recruiter.setCompanyLocation(request.getCompanyLocation());
            recruiter.setIndustry(request.getIndustry());
            recruiter.setLinkedinProfile(request.getLinkedinProfile());

            if (request.getCollegeId() != null) {
                collegeRepository.findById(request.getCollegeId())
                        .ifPresent(recruiter::setCollege);
            }

            return entityMapper.toRecruiterResponse(recruiterRepository.save(recruiter));
        }).orElseThrow(() -> new ResourceNotFoundException("Recruiter with id " + id + " not found"));
    }

    public void deleteRecruiter(String id) {
        recruiterRepository.deleteById(id);
    }

    public List<RecruiterResponse> getRecruitersByCollege(String collegeId) {
        return recruiterRepository.findByCollegeId(collegeId)
                .stream()
                .map(entityMapper::toRecruiterResponse)
                .collect(Collectors.toList());
    }
    public RecruiterResponse updateRecruiterProfile(String id, RecruiterProfileRequest request) {
        Recruiter recruiter = recruiterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        recruiter.setName(request.getName());
        if (request.getCompany() != null) recruiter.setCompany(request.getCompany());
        if (request.getDesignation() != null) recruiter.setDesignation(request.getDesignation());
        if (request.getPhone() != null) recruiter.setPhone(request.getPhone());
        if (request.getCompanyWebsite() != null) recruiter.setCompanyWebsite(request.getCompanyWebsite());
        if (request.getCompanyLocation() != null) recruiter.setCompanyLocation(request.getCompanyLocation());
        if (request.getIndustry() != null) recruiter.setIndustry(request.getIndustry());
        if (request.getLinkedinProfile() != null) recruiter.setLinkedinProfile(request.getLinkedinProfile());

        return entityMapper.toRecruiterResponse(recruiterRepository.save(recruiter));
    }

    public void changeRecruiterPassword(String id, ChangePasswordRequest request) {
        Recruiter recruiter = recruiterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), recruiter.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        recruiter.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        recruiterRepository.save(recruiter);
    }
}