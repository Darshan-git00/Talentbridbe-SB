package in.talentbridge.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


@Data
public class StudentRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Course is required")
    private String course;

    @NotBlank(message = "Branch is required")
    private String branch;

    @NotBlank(message = "Year is required")
    private String year;

    @Positive(message = "CGPA must be positive")
    private double cgpa;

    private String[] skills;
    private String[] certifications;
    private String status;
    private String githubProfile;
    private String hackerrankProfile;
    private String linkedinProfile;
    private String portfolioUrl;


    @NotBlank(message = "College ID is required")
    private String collegeId;
}