package in.talentbridge.dto;

import lombok.Data;

@Data
public class StudentResponse {
    private String id;
    private String name;
    private String email;
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
    private int aiInterviewScore;
    private int skillMatchPercentage;
    private int projectExperience;
    private int platformScore;
    private int overallScore;
    private int githubScore;
    private String collegeId;
    private String collegeName;
    private String createdAt;
    private String resumeUrl;
}