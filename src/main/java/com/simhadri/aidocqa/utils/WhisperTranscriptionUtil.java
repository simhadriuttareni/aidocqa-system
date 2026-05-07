// utils/WhisperTranscriptionUtil.java (Simpler Version)
package com.simhadri.aidocqa.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class WhisperTranscriptionUtil {
    private static final Logger log = LoggerFactory.getLogger(WhisperTranscriptionUtil.class);

    @Value("${openai.api.key}")
    private String apiKey;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        log.info("WhisperTranscriptionUtil initialized");
    }

    public String transcribe(MultipartFile file) {
        log.info("Transcribing file: {}", file.getOriginalFilename());

        File tempFile = null;
        try {
            // Convert MultipartFile to File
            tempFile = File.createTempFile("whisper_", ".mp3");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            // Prepare request
            String url = "https://api.openai.com/v1/audio/transcriptions";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));
            body.add("model", "whisper-1");
            body.add("response_format", "json");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String transcript = json.get("text").asText();
                log.info("Transcription successful. Length: {}", transcript.length());
                return transcript;
            } else {
                throw new RuntimeException("Whisper API returned: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Transcription failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to transcribe: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}