package in.talentbridge.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DriveResponse {
    private String id;
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
    private String recruiterName;
    private String company;
    private String collegeId;
    private String collegeName;
    private LocalDateTime createdAt;
}