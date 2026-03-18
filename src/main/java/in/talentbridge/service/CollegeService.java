package in.talentbridge.service;

import in.talentbridge.dto.CollegeRequest;
import in.talentbridge.dto.CollegeResponse;
import in.talentbridge.entity.College;
import in.talentbridge.exception.DuplicateResourceException;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.mapper.EntityMapper;
import in.talentbridge.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import in.talentbridge.dto.profile.CollegeProfileRequest;
import in.talentbridge.dto.profile.ChangePasswordRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final EntityMapper entityMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<CollegeResponse> getAllColleges() {
        return collegeRepository.findAll()
                .stream()
                .map(entityMapper::toCollegeResponse)
                .collect(Collectors.toList());
    }

    public Optional<CollegeResponse> getCollegeById(String id) {
        return collegeRepository.findById(id)
                .map(entityMapper::toCollegeResponse);
    }

    public CollegeResponse createCollege(CollegeRequest request) {
        if (collegeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("College with email " + request.getEmail() + " already exists");
        }
        College college = new College();
        college.setName(request.getName());
        college.setEmail(request.getEmail());
        college.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        college.setLocation(request.getLocation());
        return entityMapper.toCollegeResponse(collegeRepository.save(college));
    }

    public CollegeResponse updateCollege(String id, CollegeRequest request) {
        return collegeRepository.findById(id).map(college -> {
            college.setName(request.getName());
            college.setEmail(request.getEmail());
            college.setLocation(request.getLocation());
            return entityMapper.toCollegeResponse(collegeRepository.save(college));
        }).orElseThrow(() -> new ResourceNotFoundException("College with id " + id + " not found"));
    }

    public void deleteCollege(String id) {
        collegeRepository.deleteById(id);
    }

    public CollegeResponse updateCollegeProfile(String id, CollegeProfileRequest request) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found"));

        college.setName(request.getName());
        if (request.getLocation() != null) college.setLocation(request.getLocation());

        return entityMapper.toCollegeResponse(collegeRepository.save(college));
    }

    public void changeCollegePassword(String id, ChangePasswordRequest request) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), college.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        college.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        collegeRepository.save(college);
    }

}