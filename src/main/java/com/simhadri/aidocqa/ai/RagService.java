package com.simhadri.aidocqa.ai;

import com.simhadri.aidocqa.model.Chunk;
import com.simhadri.aidocqa.model.Document;
import com.simhadri.aidocqa.repository.ChunkRepository;
import com.simhadri.aidocqa.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagService {
    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    @Autowired
    private GroqService groqService;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private DocumentRepository documentRepository;

    public String generateRagResponse(String question, String documentId) {
        log.info("Generating response for question: {}", question);

        try {
            // Get the document content
            Document document;
            if (documentId != null && !documentId.isEmpty()) {
                document = documentRepository.findById(documentId).orElse(null);
            } else {
                List<Document> documents = documentRepository.findAll();
                if (documents.isEmpty()) {
                    return "No documents found. Please upload a document first.";
                }
                document = documents.get(0);
            }

            if (document == null) {
                return "Document not found.";
            }

            // Get the full text content
            String fullText = document.getOriginalText();
            if (fullText == null || fullText.isEmpty()) {
                return "Document has no extractable text content.";
            }

            log.info("Using document: {}, text length: {}", document.getFilename(), fullText.length());

            // Simple excerpt extraction based on keywords
            String relevantExcerpt = extractRelevantExcerpt(fullText, question);

            // Generate answer using Groq
            String systemPrompt = "You are a helpful AI assistant. Answer the user's question based ONLY on the provided document excerpt.\n\n" +
                    "Document Excerpt:\n" + relevantExcerpt;

            String answer = groqService.generateResponse(systemPrompt, question);

            return answer;

        } catch (Exception e) {
            log.error("Error generating response: {}", e.getMessage(), e);
            return "Sorry, I encountered an error: " + e.getMessage();
        }
    }

    private String extractRelevantExcerpt(String fullText, String question) {
        // Simple keyword extraction from question
        String[] keywords = question.toLowerCase().split("\\s+");

        // Find sentences containing keywords
        String[] sentences = fullText.split("[.!?]+");
        List<String> relevantSentences = new ArrayList<>();

        for (String sentence : sentences) {
            String lowerSentence = sentence.toLowerCase();
            for (String keyword : keywords) {
                if (keyword.length() > 3 && lowerSentence.contains(keyword)) {
                    relevantSentences.add(sentence.trim());
                    break;
                }
            }
        }

        if (relevantSentences.isEmpty()) {
            // Return first 1000 characters if no keywords found
            return fullText.length() > 2000 ? fullText.substring(0, 2000) : fullText;
        }

        // Join relevant sentences
        String excerpt = String.join(". ", relevantSentences);
        if (excerpt.length() > 3000) {
            excerpt = excerpt.substring(0, 3000);
        }

        log.info("Extracted excerpt length: {} from {} sentences", excerpt.length(), relevantSentences.size());
        return excerpt;
    }
}