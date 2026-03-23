package in.talentbridge.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class StudentProfileRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String course;
    private String branch;
    private Integer year;
    private Double cgpa;
    private String[] skills;
    private String[] certifications;
    private String githubProfile;
    private String hackerrankProfile;
    private String linkedinProfile;
    private String portfolioUrl;
    private String competitiveProgrammingProfile;
}