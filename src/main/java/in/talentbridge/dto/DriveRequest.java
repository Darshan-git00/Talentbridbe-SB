package in.talentbridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DriveRequest {
    @NotBlank(message = "Position is required")
    private String position;

    private String description;

    @Positive(message = "Openings must be greater than 0")
    private int openings;

    private String salary;
    private String location;
    private String driveType;

    @NotBlank(message = "Status is required")
    private String status;

    private double minSkillScore;
    private String[] eligibleBranches;
    private String[] eligibleYears;

    @NotNull(message = "Drive date is required")
    private LocalDate driveDate;

    @NotNull(message = "Last date to apply is required")
    private LocalDate lastDateToApply;

    //@NotBlank(message = "Recruiter ID is required")
    private String recruiterId;

    //@NotBlank(message = "College ID is required")
    private String collegeId;
}