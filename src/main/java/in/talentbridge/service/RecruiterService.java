package in.talentbridge.service;

import in.talentbridge.entity.Recruiter;
import in.talentbridge.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private final RecruiterRepository recruiterRepository;

    public List<Recruiter> getAllRecruiters() {
        return recruiterRepository.findAll();
    }

    public Optional<Recruiter> getRecruiterById(String id) {
        return recruiterRepository.findById(id);
    }

    public Recruiter createRecruiter(Recruiter recruiter) {
        return recruiterRepository.save(recruiter);
    }

    public Recruiter updateRecruiter(String id, Recruiter updatedRecruiter) {
        return recruiterRepository.findById(id).map(recruiter -> {
            recruiter.setName(updatedRecruiter.getName());
            recruiter.setEmail(updatedRecruiter.getEmail());
            recruiter.setCompany(updatedRecruiter.getCompany());
            recruiter.setDesignation(updatedRecruiter.getDesignation());
            recruiter.setPhone(updatedRecruiter.getPhone());
            recruiter.setCompanyWebsite(updatedRecruiter.getCompanyWebsite());
            recruiter.setCompanyLocation(updatedRecruiter.getCompanyLocation());
            recruiter.setIndustry(updatedRecruiter.getIndustry());
            recruiter.setLinkedinProfile(updatedRecruiter.getLinkedinProfile());
            recruiter.setStatus(updatedRecruiter.getStatus());
            return recruiterRepository.save(recruiter);
        }).orElseThrow(() -> new RuntimeException("Recruiter not found"));
    }

    public void deleteRecruiter(String id) {
        recruiterRepository.deleteById(id);
    }

    public List<Recruiter> getRecruitersByCollege(String collegeId) {
        return recruiterRepository.findByCollegeId(collegeId);
    }
}