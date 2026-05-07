package com.simhadri.aidocqa.ai;
import com.simhadri.aidocqa.ai.GroqService;

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
public class GroqService {
    private static final Logger log = LoggerFactory.getLogger(GroqService.class);

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${groq.temperature:0.7}")
    private double temperature;

    @Value("${groq.max-tokens:1000}")
    private int maxTokens;

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

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("⚠️ Groq API key is not set! Get a free key from https://console.groq.com");
            log.warn("   Set it with: $env:GROQ_API_KEY='gsk_your_key'");
        } else {
            log.info("✅ GroqService initialized with model: {}", model);
        }
    }

    public String generateResponse(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Groq API key not configured. Returning fallback response.");
            return generateFallbackResponse(systemPrompt, userMessage);
        }

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

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

            Request request = new Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    log.error("Groq API error: {} - {}", response.code(), responseBody);
                    return generateFallbackResponse(systemPrompt, userMessage);
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
            log.error("Groq API error: {}", e.getMessage(), e);
            return generateFallbackResponse(systemPrompt, userMessage);
        }
    }

    public String generateSummary(String text) {
        String systemPrompt = "You are a document summarization expert. Create a concise summary of the following text. Focus on key points and main ideas. Keep it under 200 words.";
        String userMessage = "Please summarize this text:\n\n" + text;
        return generateResponse(systemPrompt, userMessage);
    }

    private String generateFallbackResponse(String systemPrompt, String userMessage) {
        log.info("Using fallback response (no API key)");

        // Extract key information from user message
        String summary = "Document processed successfully. ";

        if (userMessage.toLowerCase().contains("summary")) {
            summary = "This document contains technical content. For a detailed AI-powered summary, please configure your Groq API key. Get a free key at https://console.groq.com";
        } else if (userMessage.toLowerCase().contains("topic")) {
            summary = "The document covers Java programming concepts. For specific topic analysis, please configure your Groq API key.";
        } else {
            summary = "Your document has been uploaded and chunked successfully. To ask questions about the content, please configure your Groq API key at https://console.groq.com";
        }

        return summary + "\n\nNote: This is an automated response. Set GROQ_API_KEY environment variable for AI-powered answers.";
    }
}