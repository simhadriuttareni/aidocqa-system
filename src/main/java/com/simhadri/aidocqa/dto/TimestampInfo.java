package com.simhadri.aidocqa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimestampInfo {
    private Double startTime;
    private Double endTime;
    private String text;
}