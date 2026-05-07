package com.simhadri.aidocqa.service;

import com.simhadri.aidocqa.model.Document;
import com.simhadri.aidocqa.model.Chunk;
import com.simhadri.aidocqa.repository.DocumentRepository;
import com.simhadri.aidocqa.repository.ChunkRepository;
import com.simhadri.aidocqa.ai.EmbeddingService;
import com.simhadri.aidocqa.ai.GroqService;
import com.simhadri.aidocqa.utils.PdfParserUtil;
import com.simhadri.aidocqa.utils.ChunkingUtil;
import com.simhadri.aidocqa.utils.WhisperTranscriptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DocumentService {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private PdfParserUtil pdfParserUtil;

    @Autowired
    private ChunkingUtil chunkingUtil;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private GroqService groqService;  // Using Groq instead of OpenAI

    @Autowired
    private WhisperTranscriptionUtil whisperTranscriptionUtil;

    private static final int MAX_TEXT_LENGTH = 50000; // Limit text to 50k chars for memory safety

    @Transactional
    public Document processDocument(MultipartFile file, String fileType) {
        log.info("Processing {} file: {}", fileType, file.getOriginalFilename());

        try {
            // 1. Extract text based on file type
            String extractedText = extractTextFromFile(file, fileType);

            // Check if extraction was successful
            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new RuntimeException("Failed to extract text from file. The file may be empty or corrupted.");
            }

            // Trim text if too long to prevent memory issues
            if (extractedText.length() > MAX_TEXT_LENGTH) {
                log.warn("Text too long ({} chars), truncating to {} chars", extractedText.length(), MAX_TEXT_LENGTH);
                extractedText = extractedText.substring(0, MAX_TEXT_LENGTH);
            }

            log.info("Extracted {} characters of text", extractedText.length());

            // 2. Create and save document
            Document document = new Document();
            document.setFilename(file.getOriginalFilename());
            document.setFileType(fileType);
            document.setOriginalText(extractedText);

            // 3. Generate summary using Groq API (Free)
            String summary = generateSummarySafely(extractedText);
            document.setSummary(summary);

            // 4. Save document first
            document = documentRepository.save(document);
            log.info("Document saved with ID: {}", document.getId());

            // 5. Chunk the text
            List<String> chunks = chunkingUtil.chunkText(extractedText);
            log.info("Created {} chunks from document", chunks.size());

            // 6. Generate embeddings and save chunks (batch processing to avoid memory issues)
            int successfulChunks = 0;
            for (int i = 0; i < chunks.size(); i++) {
                try {
                    String chunkText = chunks.get(i);
                    log.debug("Processing chunk {}/{}", i + 1, chunks.size());

                    // Skip empty chunks
                    if (chunkText == null || chunkText.trim().isEmpty()) {
                        log.warn("Skipping empty chunk at index {}", i);
                        continue;
                    }

                    List<Double> embedding = embeddingService.generateEmbedding(chunkText);

                    // Skip if embedding generation failed
                    if (embedding == null || embedding.isEmpty()) {
                        log.warn("Failed to generate embedding for chunk {}, skipping", i);
                        continue;
                    }

                    String embeddingStr = embeddingService.embeddingToString(embedding);

                    Chunk chunk = new Chunk();
                    chunk.setDocument(document);
                    chunk.setText(chunkText);
                    chunk.setChunkIndex(i);
                    chunk.setEmbedding(embeddingStr);

                    chunkRepository.save(chunk);
                    successfulChunks++;

                } catch (Exception e) {
                    log.error("Failed to process chunk {}: {}", i, e.getMessage());
                    // Continue with next chunk instead of failing the whole document
                }
            }

            log.info("Document processed successfully. ID: {}, Successfully processed chunks: {}/{}",
                    document.getId(), successfulChunks, chunks.size());
            return document;

        } catch (Exception e) {
            log.error("Failed to process document: {}", e.getMessage(), e);
            throw new RuntimeException("Document processing failed: " + e.getMessage());
        }
    }

    /**
     * Extract text from file based on file type
     */
    private String extractTextFromFile(MultipartFile file, String fileType) {
        try {
            if ("PDF".equalsIgnoreCase(fileType)) {
                log.info("Extracting text from PDF");
                return pdfParserUtil.extractText(file);
            } else if ("TEXT".equalsIgnoreCase(fileType)) {
                log.info("Reading text file directly");
                return readTextFile(file);
            } else if ("AUDIO".equalsIgnoreCase(fileType) || "VIDEO".equalsIgnoreCase(fileType)) {
                log.info("Transcribing audio/video file");
                return whisperTranscriptionUtil.transcribe(file);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + fileType + ". Supported: PDF, TEXT, AUDIO, VIDEO");
            }
        } catch (Exception e) {
            log.error("Text extraction failed for file type {}: {}", fileType, e.getMessage());
            throw new RuntimeException("Text extraction failed: " + e.getMessage());
        }
    }

    /**
     * Read text file content directly
     */
    private String readTextFile(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            log.info("Read text file, {} characters", content.length());
            return content;
        } catch (Exception e) {
            log.error("Failed to read text file: {}", e.getMessage());
            throw new RuntimeException("Failed to read text file: " + e.getMessage());
        }
    }

    /**
     * Generate summary safely using Groq API (Free)
     */
    private String generateSummarySafely(String text) {
        try {
            // Truncate text for summary if too long
            String textForSummary = text.length() > 10000 ? text.substring(0, 10000) : text;
            String summary = groqService.generateSummary(textForSummary);

            // Check if summary generation failed
            if (summary == null || summary.contains("error") || summary.contains("Sorry")) {
                log.warn("Summary generation returned error or empty, using fallback");
                return generateFallbackSummary(text);
            }

            return summary;
        } catch (Exception e) {
            log.error("Summary generation failed: {}", e.getMessage());
            return generateFallbackSummary(text);
        }
    }

    /**
     * Generate a fallback summary without API call
     */
    private String generateFallbackSummary(String text) {
        int wordCount = text.split("\\s+").length;
        String preview = text.length() > 500 ? text.substring(0, 500) : text;

        return String.format(
                "Document Summary (Auto-generated - API unavailable):\n\n" +
                        "This document contains approximately %d words.\n\n" +
                        "Preview:\n%s\n\n" +
                        "Note: Full AI-powered summarization requires a valid API key. " +
                        "For better summaries, please configure your API key in application.properties.",
                wordCount, preview
        );
    }

    public Document getDocument(String documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    @Transactional
    public void deleteDocument(String documentId) {
        Document document = getDocument(documentId);
        chunkRepository.deleteByDocument(document);
        documentRepository.delete(document);
        log.info("Document deleted: {}", documentId);
    }

    @Transactional
    public Document reprocessDocument(String documentId) {
        Document existingDocument = getDocument(documentId);

        // Delete existing chunks
        chunkRepository.deleteByDocument(existingDocument);

        // Reprocess the text
        String extractedText = existingDocument.getOriginalText();

        // Chunk the text
        List<String> chunks = chunkingUtil.chunkText(extractedText);
        log.info("Reprocessing document: {} chunks", chunks.size());

        // Generate embeddings and save chunks
        int successfulChunks = 0;
        for (int i = 0; i < chunks.size(); i++) {
            try {
                String chunkText = chunks.get(i);
                if (chunkText == null || chunkText.trim().isEmpty()) continue;

                List<Double> embedding = embeddingService.generateEmbedding(chunkText);
                if (embedding == null || embedding.isEmpty()) continue;

                String embeddingStr = embeddingService.embeddingToString(embedding);

                Chunk chunk = new Chunk();
                chunk.setDocument(existingDocument);
                chunk.setText(chunkText);
                chunk.setChunkIndex(i);
                chunk.setEmbedding(embeddingStr);

                chunkRepository.save(chunk);
                successfulChunks++;

            } catch (Exception e) {
                log.error("Failed to process chunk during reprocessing: {}", e.getMessage());
            }
        }

        log.info("Document reprocessed successfully. ID: {}, Chunks: {}/{}",
                existingDocument.getId(), successfulChunks, chunks.size());
        return existingDocument;
    }
}