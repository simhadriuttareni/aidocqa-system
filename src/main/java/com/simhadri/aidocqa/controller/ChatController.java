// controller/ChatController.java
package com.simhadri.aidocqa.controller;

import com.simhadri.aidocqa.dto.ChatRequest;
import com.simhadri.aidocqa.dto.ChatResponse;
import com.simhadri.aidocqa.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request) {
        try {
            log.info("Chat request: {}", request.getQuestion());

            ChatResponse response = chatService.askQuestion(
                    request.getQuestion(),
                    request.getDocumentId()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Chat failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}