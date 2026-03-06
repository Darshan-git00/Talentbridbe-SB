package in.talentbridge.service;

import in.talentbridge.entity.College;
import in.talentbridge.repository.CollegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository collegeRepository;

    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    public Optional<College> getCollegeById(String id) {
        return collegeRepository.findById(id);
    }

    public College createCollege(College college) {
        return collegeRepository.save(college);
    }

    public College updateCollege(String id, College updatedCollege) {
        return collegeRepository.findById(id).map(college -> {
            college.setName(updatedCollege.getName());
            college.setEmail(updatedCollege.getEmail());
            college.setLocation(updatedCollege.getLocation());
            return collegeRepository.save(college);
        }).orElseThrow(() -> new RuntimeException("College not found"));
    }

    public void deleteCollege(String id) {
        collegeRepository.deleteById(id);
    }
}