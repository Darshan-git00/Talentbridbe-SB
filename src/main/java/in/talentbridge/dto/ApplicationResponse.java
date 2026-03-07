package in.talentbridge.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private String id;
    private String status;
    private String feedback;
    private LocalDateTime interviewDate;
    private String interviewMode;
    private LocalDateTime appliedDate;
    private LocalDateTime updatedAt;

    // Student details
    private String studentId;
    private String studentName;
    private String studentEmail;

    // Drive details
    private String driveId;
    private String position;
    private String company;
}