package in.talentbridge.dto;

import lombok.Data;

@Data
public class RecruiterRequest {
    private String name;
    private String email;
    private String password;
    private String company;
    private String designation;
    private String phone;
    private String companyWebsite;
    private String companyLocation;
    private String industry;
    private String linkedinProfile;
    private String collegeId;
}