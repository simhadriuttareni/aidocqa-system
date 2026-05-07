// controller/DocumentController.java
package com.simhadri.aidocqa.controller;

import com.simhadri.aidocqa.model.Document;
import com.simhadri.aidocqa.service.DocumentService;
import com.simhadri.aidocqa.dto.UploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:3000")
public class DocumentController {
    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        try {
            log.info("Upload request received: {} ({})", file.getOriginalFilename(), type);

            Document document = documentService.processDocument(file, type);

            UploadResponse response = new UploadResponse(
                    document.getId(),
                    document.getFilename(),
                    document.getSummary(),
                    document.getChunks().size(),
                    "Document uploaded and processed successfully"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable String id) {
        return ResponseEntity.ok(documentService.getDocument(id));
    }
}