// utils/PdfParserUtil.java
package com.simhadri.aidocqa.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;

@Component
public class PdfParserUtil {
    private static final Logger log = LoggerFactory.getLogger(PdfParserUtil.class);

    public String extractText(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            log.info("Extracted {} characters from PDF", text.length());
            return text;

        } catch (Exception e) {
            log.error("Failed to parse PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to parse PDF file: " + e.getMessage());
        }
    }
}