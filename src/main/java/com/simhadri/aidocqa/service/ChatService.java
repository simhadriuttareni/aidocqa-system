package com.simhadri.aidocqa.service;

import com.simhadri.aidocqa.ai.RagService;
import com.simhadri.aidocqa.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private RagService ragService;

    public ChatResponse askQuestion(String question, String documentId) {
        log.info("Processing question: {}", question);

        if (question == null || question.trim().isEmpty()) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setAnswer("Please enter a valid question.");
            errorResponse.setSources(Collections.emptyList());
            errorResponse.setTimestamps(Collections.emptyList());
            return errorResponse;
        }

        try {
            String answer = ragService.generateRagResponse(question, documentId);

            ChatResponse response = new ChatResponse();
            response.setAnswer(answer);
            response.setSources(Collections.emptyList());
            response.setTimestamps(Collections.emptyList());

            return response;

        } catch (Exception e) {
            log.error("Error processing question: {}", e.getMessage(), e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setAnswer("Sorry, I encountered an error. Please try again.");
            errorResponse.setSources(Collections.emptyList());
            errorResponse.setTimestamps(Collections.emptyList());
            return errorResponse;
        }
    }

    public ChatResponse askQuestion(String question) {
        return askQuestion(question, null);
    }
}