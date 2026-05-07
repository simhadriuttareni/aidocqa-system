// utils/ChunkingUtil.java
package com.simhadri.aidocqa.utils;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChunkingUtil {
    private static final Logger log = LoggerFactory.getLogger(ChunkingUtil.class);

    private static final int CHUNK_SIZE = 500; // Reduced from 1000
    private static final int OVERLAP = 50; // Reduced from 200

    public List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            log.warn("Empty text provided for chunking");
            return chunks;
        }

        // Limit total text length
        if (text.length() > 10000) {
            log.warn("Text too long ({} chars), truncating to 10000", text.length());
            text = text.substring(0, 10000);
        }

        log.info("Chunking text of length: {}", text.length());

        int start = 0;
        int chunkCount = 0;

        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());

            // Try to end at a newline or space for better chunks
            if (end < text.length()) {
                int lastNewline = text.lastIndexOf('\n', end);
                int lastSpace = text.lastIndexOf(' ', end);
                int breakPoint = Math.max(lastNewline, lastSpace);

                if (breakPoint > start + CHUNK_SIZE / 2) {
                    end = breakPoint;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
                chunkCount++;
            }

            start = end - OVERLAP;
            if (start < 0) start = 0;
            if (start >= text.length()) break;

            // Safety limit
            if (chunkCount > 200) {
                log.warn("Too many chunks ({}), stopping", chunkCount);
                break;
            }
        }

        log.info("Created {} chunks", chunks.size());
        return chunks;
    }
}