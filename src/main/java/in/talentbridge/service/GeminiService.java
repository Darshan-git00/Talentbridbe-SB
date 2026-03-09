package in.talentbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public GeminiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String prompt(String promptText) {
        try {
            return callGroq(promptText);
        } catch (Exception e) {
            System.out.println("Groq API error: " + e.getMessage());
            throw new RuntimeException("Groq API call failed: " + e.getMessage());
        }
    }

    private String callGroq(String promptText) throws Exception {
        String requestBody = """
                {
                    "model": "llama-3.3-70b-versatile",
                    "messages": [
                        {
                            "role": "user",
                            "content": "%s"
                        }
                    ],
                    "temperature": 0.3,
                    "max_tokens": 1000
                }
                """.formatted(escapeJson(promptText));

        String response = webClient.post()
                .uri(GROQ_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode root = objectMapper.readTree(response);
        return root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}