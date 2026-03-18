package in.talentbridge.service;

import in.talentbridge.entity.Student;
import in.talentbridge.exception.ResourceNotFoundException;
import in.talentbridge.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInterviewService {

    private final StudentRepository  studentRepository;
    private final HuggingFaceService huggingFaceService;  // ← HF, not Groq

    // ── System prompt — defines the interviewer persona ───────────────────────

    private String buildSystemPrompt(Student student) {
        return String.format("""
                You are Aria, a professional technical interviewer at a top tech company conducting
                a campus placement interview for the following candidate:
                
                Name: %s
                Course: %s | Branch: %s | Year: %s
                CGPA: %s
                Skills: %s
                Profile Score: %s / 100
                
                Interview guidelines:
                - Be professional, warm, and encouraging
                - Ask ONE question at a time — never multiple questions in one message
                - Progress naturally: self-introduction → technical skills → projects → problem-solving → career goals
                - If an answer is weak or vague, ask a follow-up on the same topic before moving on
                - After 8-10 candidate responses, start wrapping up the interview
                - Keep your responses concise: 1-2 sentences of acknowledgment + 1 question
                - Do NOT evaluate or score during the interview — just ask questions
                - Do NOT repeat questions already asked
                """,
                student.getName(),
                student.getCourse(), student.getBranch(), student.getYear(),
                student.getCgpa(),
                student.getSkills() != null ? String.join(", ", student.getSkills()) : "Not specified",
                student.getOverallScore()
        );
    }

    // ── Start ─────────────────────────────────────────────────────────────────

    public String startInterview(String studentId) {
        Student student = getStudent(studentId);
        log.info("[AI-INTERVIEW] Starting for student: {}", studentId);

        String systemPrompt = buildSystemPrompt(student);

        // Seed the conversation with a single user trigger so the AI opens naturally
        List<Map<String, String>> seed = List.of(
                Map.of("role", "user", "content",
                        "Hello, I'm here for the interview.")
        );

        return huggingFaceService.chat(systemPrompt, seed);
    }

    // ── Continue ──────────────────────────────────────────────────────────────

    public String continueInterview(String studentId,
                                    List<Map<String, String>> history) {
        Student student = getStudent(studentId);
        log.info("[AI-INTERVIEW] Continuing for student: {} | turns: {}", studentId, history.size());

        String systemPrompt = buildSystemPrompt(student);

        // Pass the full history — HF model sees entire conversation context
        return huggingFaceService.chat(systemPrompt, history);
    }

    // ── End + Score ───────────────────────────────────────────────────────────

    public Map<String, Object> endInterview(String studentId,
                                            List<Map<String, String>> history) {
        Student student = getStudent(studentId);
        log.info("[AI-INTERVIEW] Scoring interview for student: {}", studentId);

        // Build transcript string for the evaluator
        StringBuilder transcript = new StringBuilder();
        for (Map<String, String> msg : history) {
            String role    = msg.get("role");
            String content = msg.get("content");
            transcript.append("assistant".equals(role) ? "Interviewer: " : "Candidate: ")
                    .append(content).append("\n\n");
        }

        String systemPrompt = """
                You are an expert interview evaluator. You will be given a full interview transcript
                and must evaluate the candidate objectively.
                
                Respond ONLY with a valid JSON object — no markdown, no backticks, no explanation outside the JSON.
                """;

        String userMessage = String.format("""
                Evaluate this campus placement interview for:
                Name: %s | Skills: %s | CGPA: %s
                
                Transcript:
                %s
                
                Return ONLY this JSON:
                {
                  "score": <integer 0-100>,
                  "feedback": "<2-3 sentence overall assessment>",
                  "strengths": "<key strengths demonstrated>",
                  "improvements": "<specific areas to work on>"
                }
                
                Scoring (25 pts each):
                - Communication clarity
                - Technical knowledge
                - Problem-solving approach
                - Confidence and professionalism
                """,
                student.getName(),
                student.getSkills() != null ? String.join(", ", student.getSkills()) : "Not specified",
                student.getCgpa(),
                transcript.toString()
        );

        // Use single-turn prompt for scoring — no persona needed, just evaluation
        String raw    = huggingFaceService.prompt(systemPrompt, userMessage);
        Map<String, Object> result = parseScoreResponse(raw);

        // Save aiInterviewScore to student
        int score = result.get("score") instanceof Number n ? n.intValue() : 0;
        if (score > 0) {
            student.setAiInterviewScore(score);
            int overall = calculateOverall(
                    student.getPlatformScore(),
                    student.getGithubScore(),
                    score
            );
            student.setOverallScore(overall);
            studentRepository.save(student);
            result.put("overallScore", overall);
            log.info("[AI-INTERVIEW] Saved — score={} overall={}", score, overall);
        }

        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Student getStudent(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    private int calculateOverall(int platform, int github, int aiInterview) {
        List<Integer> scores = new ArrayList<>();
        if (platform    > 0) scores.add(platform);
        if (github      > 0) scores.add(github);
        if (aiInterview > 0) scores.add(aiInterview);
        if (scores.isEmpty()) return 0;
        return scores.stream().mapToInt(Integer::intValue).sum() / scores.size();
    }

    private Map<String, Object> parseScoreResponse(String raw) {
        Map<String, Object> result = new HashMap<>();
        result.put("score",        0);
        result.put("feedback",     raw);
        result.put("strengths",    "");
        result.put("improvements", "");
        try {
            String json = raw.trim()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*",     "")
                    .replaceAll("```$",              "")
                    .trim();
            // Find the JSON object boundaries
            int start = json.indexOf("{");
            int end   = json.lastIndexOf("}");
            if (start != -1 && end != -1) {
                json = json.substring(start, end + 1);
            }
            result.put("score",        extractInt(json,    "score"));
            result.put("feedback",     extractString(json, "feedback"));
            result.put("strengths",    extractString(json, "strengths"));
            result.put("improvements", extractString(json, "improvements"));
        } catch (Exception e) {
            log.warn("[AI-INTERVIEW] JSON parse failed: {}", e.getMessage());
        }
        return result;
    }

    private int extractInt(String json, String key) {
        try {
            int keyIdx = json.indexOf("\"" + key + "\"");
            if (keyIdx == -1) return 0;
            int colon = json.indexOf(":", keyIdx);
            int end   = json.indexOf(",", colon);
            if (end == -1) end = json.indexOf("}", colon);
            return Integer.parseInt(json.substring(colon + 1, end).trim());
        } catch (Exception e) { return 0; }
    }

    private String extractString(String json, String key) {
        try {
            int keyIdx = json.indexOf("\"" + key + "\"");
            if (keyIdx == -1) return "";
            int colon  = json.indexOf(":", keyIdx);
            int qStart = json.indexOf("\"", colon + 1) + 1;
            int qEnd   = qStart;
            while (qEnd < json.length()) {
                char c = json.charAt(qEnd);
                if (c == '"' && json.charAt(qEnd - 1) != '\\') break;
                qEnd++;
            }
            return json.substring(qStart, qEnd)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        } catch (Exception e) { return ""; }
    }
}