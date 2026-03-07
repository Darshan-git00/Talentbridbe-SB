package in.talentbridge.dto;

import lombok.Data;

@Data
public class RecruiterResponse {
    private String id;
    private String name;
    private String email;
    private String company;
    private String designation;
    private String phone;
    private String companyWebsite;
    private String companyLocation;
    private String industry;
    private String linkedinProfile;
    private String profilePicture;
    private String status;
    private String collegeId;
    private String collegeName;
    private String createdAt;
}