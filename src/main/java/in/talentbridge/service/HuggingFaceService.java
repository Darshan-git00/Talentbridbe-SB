package in.talentbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HuggingFaceService {

    // Separate Groq API key — keeps interview token usage isolated from
    // the main GeminiService key used for resume parsing and feedback
    @Value("${groq.interview.api.key}")
    private String apiKey;

    private final WebClient    webClient;
    private final ObjectMapper objectMapper;

    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final String URL   = "https://api.groq.com/openai/v1/chat/completions";

    public HuggingFaceService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient    = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Single-turn call — used for scoring/evaluation at interview end.
     * System prompt sets the evaluator role, low temperature for consistent output.
     */
    public String prompt(String systemPrompt, String userMessage) {
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user",   "content", userMessage)
        );
        return call(messages, 0.3, 1000);
    }

    /**
     * Multi-turn call — used during the live interview.
     * Full conversation history passed each time so the AI remembers context.
     *
     * @param systemPrompt  Interviewer persona — prepended to every request
     * @param history       Full conversation so far [{role, content}, ...]
     */
    public String chat(String systemPrompt, List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(history);
        // Higher temperature = more natural, varied interview responses
        return call(messages, 0.7, 512);
    }

    // ── Core call ─────────────────────────────────────────────────────────────

    private String call(List<Map<String, String>> messages, double temperature, int maxTokens) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model",       MODEL,
                    "messages",    messages,
                    "temperature", temperature,
                    "max_tokens",  maxTokens
            ));

            log.debug("[INTERVIEW-LLM] Calling Groq | model: {} | messages: {}", MODEL, messages.size());

            String response = webClient.post()
                    .uri(URL)
                    .header("Content-Type",  "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            String content = root
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            log.debug("[INTERVIEW-LLM] Response length: {}", content.length());
            return content;

        } catch (Exception e) {
            log.error("[INTERVIEW-LLM] Call failed: {}", e.getMessage());
            throw new RuntimeException("Interview LLM call failed: " + e.getMessage(), e);
        }
    }
}