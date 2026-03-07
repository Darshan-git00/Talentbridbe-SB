package in.talentbridge.dto;

import lombok.Data;

@Data
public class StudentRequest {
    private String name;
    private String email;
    private String password;
    private String course;
    private String branch;
    private String year;
    private double cgpa;
    private String[] skills;
    private String[] certifications;
    private String status;
    private String githubProfile;
    private String hackerrankProfile;
    private String linkedinProfile;
    private String portfolioUrl;
    private String collegeId;
}