package com.chronicle.dto;

import lombok.Data;

@Data
public class AiRequest {
    private String rawData;
    private String historyData;
    private String content;
    private String prompt;
}
