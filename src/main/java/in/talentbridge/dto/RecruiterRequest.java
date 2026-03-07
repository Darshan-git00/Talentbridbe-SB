package in.talentbridge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecruiterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Company is required")
    private String company;

    private String designation;
    private String phone;
    private String companyWebsite;
    private String companyLocation;
    private String industry;
    private String linkedinProfile;

    @NotBlank(message = "College ID is required")
    private String collegeId;
}