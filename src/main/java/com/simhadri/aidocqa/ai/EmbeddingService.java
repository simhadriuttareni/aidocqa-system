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
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class EmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.embedding.model:text-embedding-ada-002}")
    private String embeddingModel;

    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;
    private final Map<String, List<Double>> embeddingCache = new HashMap<>();

    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        log.info("EmbeddingService initialized with model: {}", embeddingModel);
    }

    public List<Double> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Attempted to generate embedding for empty text");
            return Collections.emptyList();
        }

        String trimmedText = text.trim();

        // Check cache
        if (embeddingCache.containsKey(trimmedText)) {
            log.debug("Cache hit for text");
            return embeddingCache.get(trimmedText);
        }

        try {
            String truncatedText = trimmedText.length() > 8000 ? trimmedText.substring(0, 8000) : trimmedText;

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", embeddingModel);
            ArrayNode input = objectMapper.createArrayNode();
            input.add(truncatedText);
            requestBody.set("input", input);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // FIXED: Correct order - MediaType first, then content
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/embeddings")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    log.error("Embedding API error: {} - {}", response.code(), responseBody);
                    return Collections.emptyList();
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                List<Double> embedding = new ArrayList<>();
                JsonNode embeddingNode = jsonResponse.path("data").path(0).path("embedding");

                for (JsonNode value : embeddingNode) {
                    embedding.add(value.asDouble());
                }

                embeddingCache.put(trimmedText, embedding);
                log.debug("Generated embedding of size: {}", embedding.size());

                return embedding;
            }

        } catch (IOException e) {
            log.error("Failed to generate embedding: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public String embeddingToString(List<Double> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (IOException e) {
            log.error("Failed to convert embedding to string", e);
            return "[]";
        }
    }

    public List<Double> stringToEmbedding(String embeddingStr) {
        try {
            if (embeddingStr == null || embeddingStr.isEmpty()) {
                return Collections.emptyList();
            }
            JsonNode node = objectMapper.readTree(embeddingStr);
            List<Double> embedding = new ArrayList<>();
            for (JsonNode value : node) {
                embedding.add(value.asDouble());
            }
            return embedding;
        } catch (IOException e) {
            log.error("Failed to parse embedding string", e);
            return Collections.emptyList();
        }
    }

    public double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.isEmpty() || vec2.isEmpty()) {
            return 0.0;
        }

        if (vec1.size() != vec2.size()) {
            log.error("Vector dimension mismatch: {} vs {}", vec1.size(), vec2.size());
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public void clearCache() {
        embeddingCache.clear();
        log.info("Embedding cache cleared");
    }
}