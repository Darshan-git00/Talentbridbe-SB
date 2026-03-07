package in.talentbridge.dto;

import lombok.Data;

@Data
public class CollegeResponse {
    private String id;
    private String name;
    private String email;
    private String location;
    private String createdAt;
}