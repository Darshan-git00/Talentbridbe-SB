package in.talentbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DriveResponse extended with eligibility info.
 * Frontend uses `eligible` and `ineligibilityReasons` to
 * show drives grayed out with specific reasons.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibleDriveResponse {

    // ── Core drive fields (same as DriveResponse) ──────────────────────────
    private String id;
    private String position;
    private String description;
    private int    openings;
    private String salary;
    private String location;
    private String driveType;
    private String status;
    private double minSkillScore;
    private String[] eligibleBranches;
    private String[] eligibleYears;
    private LocalDate driveDate;
    private LocalDate lastDateToApply;
    private LocalDate createdAt;

    // Nested recruiter info
    private RecruiterInfo recruiter;

    // ── Eligibility fields ──────────────────────────────────────────────────
    /** true = student meets all criteria (branch, year, skill score) */
    private boolean eligible;

    /**
     * Human-readable reasons why student is NOT eligible.
     * Empty when eligible=true.
     * Examples:
     *   "Branch not eligible (your branch: ECE)"
     *   "Need 15 more skill points (yours: 45, required: 60)"
     *   "Year not eligible (your year: 2)"
     */
    private List<String> ineligibilityReasons;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruiterInfo {
        private String id;
        private String name;
        private String company;
        private String email;
        private String designation;
    }
}