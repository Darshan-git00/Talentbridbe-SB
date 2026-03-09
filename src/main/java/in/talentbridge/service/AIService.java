package in.talentbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.talentbridge.entity.Drive;
import in.talentbridge.entity.Student;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.repository.DriveRepository;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    private final GeminiService geminiService;
    private final StudentRepository studentRepository;
    private final DriveRepository driveRepository;
    private final ObjectMapper objectMapper;

    // Match student to a specific drive
    public Map<String, Object> matchStudentToDrive(String studentId, String driveId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found"));

        String prompt = """
                You are a recruitment expert. Analyze how well this student fits this job drive.
                Return ONLY a JSON object with no markdown.
                
                Student Profile:
                - Name: %s
                - Course: %s, Branch: %s, Year: %s
                - CGPA: %s
                - Skills: %s
                - Overall Score: %d
                
                Job Drive:
                - Position: %s
                - Company: (via recruiter)
                - Description: %s
                - Required Min Skill Score: %s
                - Eligible Branches: %s
                
                Return exactly:
                {
                    "matchScore": 85,
                    "matchLevel": "Strong Match",
                    "strengths": ["strength1", "strength2"],
                    "gaps": ["gap1", "gap2"],
                    "recommendation": "2 sentence recommendation on whether to apply",
                    "skillsToLearn": ["skill1", "skill2"]
                }
                
                matchLevel must be one of: "Strong Match", "Good Match", "Partial Match", "Weak Match"
                """.formatted(
                student.getName(),
                student.getCourse(), student.getBranch(), student.getYear(),
                student.getCgpa(),
                String.join(", ", student.getSkills() != null ? List.of(student.getSkills()) : List.of()),
                student.getOverallScore(),
                drive.getPosition(),
                drive.getDescription(),
                drive.getMinSkillScore(),
                drive.getEligibleBranches()
        );

        try {
            String response = geminiService.prompt(prompt);
            String cleaned = response.replace("```json", "").replace("```", "").trim();
            JsonNode json = objectMapper.readTree(cleaned);

            List<String> strengths = new ArrayList<>();
            List<String> gaps = new ArrayList<>();
            List<String> skillsToLearn = new ArrayList<>();

            json.path("strengths").forEach(s -> strengths.add(s.asText()));
            json.path("gaps").forEach(s -> gaps.add(s.asText()));
            json.path("skillsToLearn").forEach(s -> skillsToLearn.add(s.asText()));

            Map<String, Object> result = new HashMap<>();
            result.put("matchScore", json.path("matchScore").asInt(50));
            result.put("matchLevel", json.path("matchLevel").asText("Partial Match"));
            result.put("strengths", strengths);
            result.put("gaps", gaps);
            result.put("recommendation", json.path("recommendation").asText(""));
            result.put("skillsToLearn", skillsToLearn);
            result.put("studentName", student.getName());
            result.put("drivePosition", drive.getPosition());
            return result;

        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("matchScore", 0);
            fallback.put("matchLevel", "Unknown");
            fallback.put("recommendation", "AI analysis temporarily unavailable.");
            return fallback;
        }
    }

    // Get AI feedback on student profile
    public Map<String, Object> getProfileFeedback(String studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        String prompt = """
                You are a career counselor. Review this student's profile and give actionable feedback.
                Return ONLY a JSON object with no markdown.
                
                Student Profile:
                - Name: %s
                - Course: %s, Branch: %s, Year: %s
                - CGPA: %s
                - Skills: %s
                - GitHub: %s
                - LinkedIn: %s
                - Resume Score: %d
                - GitHub Score: %d
                - Overall Score: %d
                
                Return exactly:
                {
                    "overallFeedback": "2-3 sentence overall assessment",
                    "strengths": ["strength1", "strength2", "strength3"],
                    "improvements": ["improvement1", "improvement2", "improvement3"],
                    "priorityActions": ["action1", "action2"],
                    "careerReadiness": 75,
                    "topRolesMatch": ["role1", "role2", "role3"]
                }
                
                careerReadiness is a score out of 100.
                topRolesMatch should suggest 3 job roles this student is best suited for.
                """.formatted(
                student.getName(),
                student.getCourse(), student.getBranch(), student.getYear(),
                student.getCgpa(),
                String.join(", ", student.getSkills() != null ? List.of(student.getSkills()) : List.of()),
                student.getGithubProfile() != null ? student.getGithubProfile() : "Not linked",
                student.getLinkedinProfile() != null ? student.getLinkedinProfile() : "Not linked",
                student.getPlatformScore(),
                student.getGithubScore(),
                student.getOverallScore()
        );

        try {
            String response = geminiService.prompt(prompt);
            String cleaned = response.replace("```json", "").replace("```", "").trim();
            JsonNode json = objectMapper.readTree(cleaned);

            List<String> strengths = new ArrayList<>();
            List<String> improvements = new ArrayList<>();
            List<String> priorityActions = new ArrayList<>();
            List<String> topRolesMatch = new ArrayList<>();

            json.path("strengths").forEach(s -> strengths.add(s.asText()));
            json.path("improvements").forEach(s -> improvements.add(s.asText()));
            json.path("priorityActions").forEach(s -> priorityActions.add(s.asText()));
            json.path("topRolesMatch").forEach(s -> topRolesMatch.add(s.asText()));

            Map<String, Object> result = new HashMap<>();
            result.put("overallFeedback", json.path("overallFeedback").asText(""));
            result.put("strengths", strengths);
            result.put("improvements", improvements);
            result.put("priorityActions", priorityActions);
            result.put("careerReadiness", json.path("careerReadiness").asInt(50));
            result.put("topRolesMatch", topRolesMatch);
            result.put("studentName", student.getName());
            return result;

        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("overallFeedback", "AI analysis temporarily unavailable.");
            return fallback;
        }
    }
}