package in.talentbridge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationRequest {
    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Drive ID is required")
    private String driveId;
}