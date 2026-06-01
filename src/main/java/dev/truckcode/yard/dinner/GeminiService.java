package dev.truckcode.yard.dinner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public GeminiService(RestClient.Builder builder, ObjectMapper objectMapper) {
        var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        this.restClient = builder
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
        this.objectMapper = objectMapper;
    }

    public GeminiResult generateRecipe(List<String> ingredients) {

        if (apiKey.isBlank()) {
            return new GeminiResult.Failure(new IllegalStateException("Gemini API key not configured"));
        }

        String prompt = """
                You are a practical home cook. The user has these ingredients available: %s.
                They also have basic staples: salt, pepper, oil, butter, water.

                Suggest ONE simple weeknight recipe using some or all of these ingredients.
                Keep it achievable and unpretentious.

                Respond with ONLY valid JSON — no markdown, no explanation:
                {
                  "title": "Recipe Name",
                  "ingredients": [
                    {"name": "ingredient", "quantity": "amount", "display": "amount ingredient", "type": "FRESH"}
                  ],
                  "method": ["Step 1.", "Step 2."]
                }

                For each ingredient, set "type" to one of: FRESH (perishables — meat, produce, dairy), PANTRY (shelf-stable — canned, dry goods, condiments), or STAPLE (salt, pepper, oil, butter, water).
                """
                .formatted(String.join(", ", ingredients));

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt)))));

        try {
            String response = restClient.post()
                    .uri(GEMINI_URL)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String text = root.at("/candidates/0/content/parts/0/text").asText().strip();
            if (text.startsWith("```")) {
                text = text.replaceAll("^```[a-z]*\\n?", "").replaceAll("\\n?```$", "").strip();
            }
            return new GeminiResult.Success(objectMapper.readValue(text, Recipe.class));
        } catch (RestClientResponseException e) {
            var status = e.getStatusCode().value();
            if (status == 429 || status == 503 || e.getStatusCode().is3xxRedirection()) {
                return new GeminiResult.OverCapacity();
            }
            return new GeminiResult.Failure(e);
        } catch (Exception e) {
            return new GeminiResult.Failure(e);
        }

    }
}
