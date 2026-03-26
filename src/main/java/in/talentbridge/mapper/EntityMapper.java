package in.talentbridge.mapper;

import in.talentbridge.dto.*;
import in.talentbridge.entity.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class EntityMapper {

    // College mappings
    public CollegeResponse toCollegeResponse(College college) {
        CollegeResponse response = new CollegeResponse();
        response.setId(college.getId());
        response.setName(college.getName());
        response.setEmail(college.getEmail());
        response.setLocation(college.getLocation());
        response.setCreatedAt(college.getCreatedAt() != null ? college.getCreatedAt().toString() : null);
        return response;
    }

    // Student mappings
    public StudentResponse toStudentResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setName(student.getName());
        response.setEmail(student.getEmail());
        response.setCourse(student.getCourse());
        response.setBranch(student.getBranch());
        response.setYear(student.getYear());
        response.setCgpa(student.getCgpa());
        response.setSkills(student.getSkills());
        response.setCertifications(student.getCertifications());
        response.setStatus(student.getStatus());
        response.setGithubProfile(student.getGithubProfile());
        response.setLinkedinProfile(student.getLinkedinProfile());
        response.setPortfolioUrl(student.getPortfolioUrl());
        response.setAiInterviewScore(student.getAiInterviewScore());
        response.setSkillMatchPercentage(student.getSkillMatchPercentage());
        response.setProjectExperience(student.getProjectExperience());
        response.setPlatformScore(student.getPlatformScore());
        response.setOverallScore(student.getOverallScore());
        response.setGithubScore(student.getGithubScore());
        response.setResumeUrl(student.getResumeUrl());
        response.setCreatedAt(student.getCreatedAt() != null ? student.getCreatedAt().toString() : null);
        if (student.getCollege() != null) {
            response.setCollegeId(student.getCollege().getId());
            response.setCollegeName(student.getCollege().getName());
        }
        return response;
    }

    // Recruiter mappings
    public RecruiterResponse toRecruiterResponse(Recruiter recruiter) {
        RecruiterResponse response = new RecruiterResponse();
        response.setId(recruiter.getId());
        response.setName(recruiter.getName());
        response.setEmail(recruiter.getEmail());
        response.setCompany(recruiter.getCompany());
        response.setDesignation(recruiter.getDesignation());
        response.setPhone(recruiter.getPhone());
        response.setCompanyWebsite(recruiter.getCompanyWebsite());
        response.setCompanyLocation(recruiter.getCompanyLocation());
        response.setIndustry(recruiter.getIndustry());
        response.setLinkedinProfile(recruiter.getLinkedinProfile());
        response.setProfilePicture(recruiter.getProfilePicture());
        response.setStatus(recruiter.getStatus());
        response.setCreatedAt(recruiter.getCreatedAt() != null ? recruiter.getCreatedAt().toString() : null);
        if (recruiter.getCollege() != null) {
            response.setCollegeId(recruiter.getCollege().getId());
            response.setCollegeName(recruiter.getCollege().getName());
        }
        return response;
    }

    // Drive mappings
    public DriveResponse toDriveResponse(Drive drive) {
        DriveResponse response = new DriveResponse();
        response.setId(drive.getId());
        response.setPosition(drive.getPosition());
        response.setDescription(drive.getDescription());
        response.setOpenings(drive.getOpenings());
        response.setSalary(drive.getSalary());
        response.setLocation(drive.getLocation());
        response.setDriveType(drive.getDriveType());
        response.setStatus(drive.getStatus());
        response.setMinSkillScore(drive.getMinSkillScore());
        response.setEligibleBranches(drive.getEligibleBranches());
        response.setEligibleYears(drive.getEligibleYears());
        response.setDriveDate(drive.getDriveDate());
        response.setLastDateToApply(drive.getLastDateToApply());
        response.setCreatedAt(drive.getCreatedAt());
        if (drive.getRecruiter() != null) {
            response.setRecruiterId(drive.getRecruiter().getId());
            response.setRecruiterName(drive.getRecruiter().getName());
            response.setCompany(drive.getRecruiter().getCompany());
            response.setRecruiter(toRecruiterResponse(drive.getRecruiter()));
        }
        if (drive.getCollege() != null) {
            response.setCollegeId(drive.getCollege().getId());
            response.setCollegeName(drive.getCollege().getName());
        }
        return response;
    }

    // Application mappings
    public ApplicationResponse toApplicationResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setStatus(application.getStatus());
        response.setFeedback(application.getFeedback());
        response.setInterviewDate(application.getInterviewDate());
        response.setInterviewMode(application.getInterviewMode());
        response.setAppliedDate(application.getAppliedDate());
        response.setUpdatedAt(application.getUpdatedAt());
        if (application.getStudent() != null) {
            response.setStudentId(application.getStudent().getId());
            response.setStudentName(application.getStudent().getName());
            response.setStudentEmail(application.getStudent().getEmail());
            response.setStudent(toStudentResponse(application.getStudent()));
        }
        if (application.getDrive() != null) {
            response.setDriveId(application.getDrive().getId());
            response.setPosition(application.getDrive().getPosition());
            if (application.getDrive().getRecruiter() != null) {
                response.setCompany(application.getDrive().getRecruiter().getCompany());
            }
            response.setDrive(toDriveResponse(application.getDrive()));
        }

        return response;
    }

    public EligibleDriveResponse toEligibleDriveResponse(
            Drive drive,
            boolean eligible,
            List<String> ineligibilityReasons) {

        EligibleDriveResponse r = new EligibleDriveResponse();
        r.setId(drive.getId());
        r.setPosition(drive.getPosition());
        r.setDescription(drive.getDescription());
        r.setOpenings(drive.getOpenings());
        r.setSalary(drive.getSalary());
        r.setLocation(drive.getLocation());
        r.setDriveType(drive.getDriveType());
        r.setStatus(drive.getStatus());
        r.setMinSkillScore(drive.getMinSkillScore());
        r.setEligibleBranches(drive.getEligibleBranches());
        r.setEligibleYears(drive.getEligibleYears());
        r.setDriveDate(drive.getDriveDate());
        r.setLastDateToApply(drive.getLastDateToApply());
        r.setCreatedAt(drive.getCreatedAt());
        r.setEligible(eligible);
        r.setIneligibilityReasons(ineligibilityReasons);

        if (drive.getRecruiter() != null) {
            EligibleDriveResponse.RecruiterInfo ri = new EligibleDriveResponse.RecruiterInfo();
            ri.setId(drive.getRecruiter().getId());
            ri.setName(drive.getRecruiter().getName());
            ri.setCompany(drive.getRecruiter().getCompany());
            ri.setEmail(drive.getRecruiter().getEmail());
            ri.setDesignation(drive.getRecruiter().getDesignation());
            r.setRecruiter(ri);
        }

        return r;
    }
}