package com.simhadri.aidocqa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String documentId;
    private String filename;
    private String summary;
    private Integer chunkCount;
    private String message;
}