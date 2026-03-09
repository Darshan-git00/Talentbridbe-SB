package in.talentbridge.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.talentbridge.entity.Student;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final Cloudinary cloudinary;
    private final StudentRepository studentRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> uploadAndParseResume(String studentId, MultipartFile file) throws IOException {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        // Upload to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "folder", "talentbridge/resumes",
                        "public_id", "resume_" + studentId,
                        "overwrite", true
                )
        );

        String resumeUrl = (String) uploadResult.get("secure_url");

        // Extract text from PDF
        String resumeText = extractTextFromPdf(file);

        // Use Gemini AI to analyze resume
        Map<String, Object> aiAnalysis = analyzeResumeWithAI(resumeText);

        List<String> extractedSkills = (List<String>) aiAnalysis.get("skills");
        int resumeScore = (int) aiAnalysis.get("score");
        String feedback = (String) aiAnalysis.get("feedback");
        String summary = (String) aiAnalysis.get("summary");

        // Calculate overall score
        int githubScore = student.getGithubScore();
        int overallScore = githubScore == 0 ? resumeScore : (resumeScore + githubScore) / 2;

        // Save to DB
        studentRepository.updateResumeDetails(
                studentId,
                resumeUrl,
                extractedSkills.toArray(new String[0]),
                resumeScore,
                overallScore
        );

        Map<String, Object> result = new HashMap<>();
        result.put("resumeUrl", resumeUrl);
        result.put("extractedSkills", extractedSkills);
        result.put("resumeScore", resumeScore);
        result.put("overallScore", overallScore);
        result.put("feedback", feedback);
        result.put("summary", summary);
        result.put("message", "Resume analyzed by AI successfully");
        return result;
    }

    private Map<String, Object> analyzeResumeWithAI(String resumeText) {
        String prompt = """
                Analyze this resume and return ONLY a JSON object with no markdown, no explanation, just raw JSON.
                
                Resume text:
                %s
                
                Return exactly this JSON structure:
                {
                    "skills": ["skill1", "skill2", "skill3"],
                    "score": 85,
                    "feedback": "2-3 sentences of specific improvement suggestions",
                    "summary": "2 sentence professional summary of this candidate"
                }
                
                Scoring criteria (out of 100):
                - Skills breadth and relevance: 40 points
                - Projects and experience: 25 points
                - Education and certifications: 15 points
                - Profile completeness (GitHub, LinkedIn, etc): 10 points
                - Overall presentation: 10 points
                
                Extract ALL technical and soft skills mentioned.
                Be honest and specific in feedback.
                """.formatted(resumeText.substring(0, Math.min(resumeText.length(), 3000)));

        try {
            String response = geminiService.prompt(prompt);

            // Clean response — remove markdown if present
            String cleaned = response
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode json = objectMapper.readTree(cleaned);

            List<String> skills = new ArrayList<>();
            json.path("skills").forEach(s -> skills.add(s.asText()));

            int score = json.path("score").asInt(50);
            String feedback = json.path("feedback").asText("Keep improving your profile.");
            String summary = json.path("summary").asText("");

            Map<String, Object> result = new HashMap<>();
            result.put("skills", skills);
            result.put("score", Math.min(score, 100));
            result.put("feedback", feedback);
            result.put("summary", summary);
            return result;

        } catch (Exception e) {
            System.out.println("AI Analysis error: " + e.getMessage());
            e.printStackTrace();
            // Fallback if AI fails
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("skills", List.of());
            fallback.put("score", 50);
            fallback.put("feedback", "Resume uploaded successfully. AI analysis temporarily unavailable.");
            fallback.put("summary", "");
            return fallback;
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return text.toLowerCase();
    }
}