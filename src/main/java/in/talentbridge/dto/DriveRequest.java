package in.talentbridge.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DriveRequest {
    private String position;
    private String description;
    private int openings;
    private String salary;
    private String location;
    private String driveType;
    private String status;
    private double minSkillScore;
    private String eligibleBranches;
    private String eligibleYears;
    private LocalDateTime driveDate;
    private LocalDateTime lastDateToApply;
    private String recruiterId;
    private String collegeId;
}