package in.talentbridge.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollegeProfileRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String location;
}