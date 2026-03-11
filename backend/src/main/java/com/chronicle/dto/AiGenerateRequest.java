package com.chronicle.dto;

import lombok.Data;

@Data
public class AiGenerateRequest {
    private String rawData;
    private String historyData;
}
