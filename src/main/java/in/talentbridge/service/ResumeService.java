package in.talentbridge.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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

    private static final List<String> KNOWN_SKILLS = List.of(
            "java", "python", "javascript", "typescript", "react", "angular", "vue",
            "spring", "spring boot", "node", "nodejs", "express", "django", "flask",
            "sql", "mysql", "postgresql", "mongodb", "redis", "docker", "kubernetes",
            "aws", "azure", "gcp", "git", "linux", "html", "css", "rest", "graphql",
            "machine learning", "deep learning", "tensorflow", "pytorch", "pandas",
            "numpy", "data science", "artificial intelligence", "c++", "c#", "kotlin",
            "swift", "flutter", "android", "ios", "devops", "ci/cd", "jenkins"
    );

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
        //System.out.println("Resume URL from Cloudinary: " + resumeUrl);

        // Parse PDF text
        String resumeText = extractTextFromPdf(file);
        List<String> extractedSkills = extractSkills(resumeText);
        int resumeScore = calculateResumeScore(resumeText, extractedSkills);

        int githubScore = student.getGithubScore();
        int overallScore;

        if (githubScore == 0) {
            overallScore = resumeScore;
        } else {
            overallScore = (resumeScore + githubScore) / 2;
        }

// Update query
        studentRepository.updateResumeDetails(
                studentId,
                resumeUrl,
                extractedSkills.toArray(new String[0]),
                resumeScore,
                overallScore
        );

        // Verify from DB
        Student fromDb = studentRepository.findById(studentId).orElseThrow();
        //System.out.println("From DB - resumeUrl: " + fromDb.getResumeUrl());

        Map<String, Object> result = new HashMap<>();
        result.put("resumeUrl", resumeUrl);
        result.put("extractedSkills", extractedSkills);
        result.put("resumeScore", resumeScore);
        result.put("message", "Resume uploaded and parsed successfully");
        return result;
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();
        return text.toLowerCase();
    }

    private List<String> extractSkills(String resumeText) {
        List<String> foundSkills = new ArrayList<>();
        for (String skill : KNOWN_SKILLS) {
            if (resumeText.contains(skill.toLowerCase())) {
                foundSkills.add(skill);
            }
        }
        return foundSkills;
    }

    private int calculateResumeScore(String resumeText, List<String> extractedSkills) {
        int score = 0;
        int skillPoints = Math.min(extractedSkills.size() * 4, 40);
        score += skillPoints;
        if (resumeText.contains("@")) score += 10;
        if (resumeText.contains("github")) score += 10;
        if (resumeText.contains("linkedin")) score += 10;
        if (resumeText.contains("project")) score += 10;
        if (resumeText.contains("experience") || resumeText.contains("internship")) score += 10;
        return Math.min(score, 100);
    }
}