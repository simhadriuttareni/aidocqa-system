package com.simhadri.aidocqa.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAIService {
    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        log.info("OpenAIService initialized with model: {}", model);

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_OPENAI_API_KEY_HERE")) {
            log.warn("⚠️ OpenAI API key is not set! Please set OPENAI_API_KEY environment variable");
        }
    }

    public String generateResponse(String systemPrompt, String userMessage) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            ArrayNode messages = objectMapper.createArrayNode();

            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            ObjectNode userMessageNode = objectMapper.createObjectNode();
            userMessageNode.put("role", "user");
            userMessageNode.put("content", userMessage);
            messages.add(userMessageNode);

            requestBody.set("messages", messages);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // FIXED: Correct order - MediaType first, then content
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    log.error("OpenAI API error: {} - {}", response.code(), responseBody);
                    return "Sorry, I encountered an error. Please try again later.";
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String content = jsonResponse
                        .path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText();

                log.info("Generated response of length: {}", content.length());
                return content;
            }

        } catch (IOException e) {
            log.error("OpenAI API error: {}", e.getMessage(), e);
            return "Sorry, I encountered an error. Please check your API key and try again.";
        }
    }

    public String generateSummary(String text) {
        String systemPrompt = "You are a document summarization expert. Create a concise summary of the following text. Focus on key points and main ideas. Keep it under 200 words.";
        String userMessage = "Please summarize this text:\n\n" + text;
        return generateResponse(systemPrompt, userMessage);
    }
}