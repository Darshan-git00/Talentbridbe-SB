package in.talentbridge.dto;

import lombok.Data;

@Data
public class CollegeRequest {
    private String name;
    private String email;
    private String password;
    private String location;
}