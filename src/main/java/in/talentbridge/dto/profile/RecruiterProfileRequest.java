package in.talentbridge.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecruiterProfileRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String company;
    private String designation;
    private String phone;
    private String companyWebsite;
    private String companyLocation;
    private String industry;
    private String linkedinProfile;
}