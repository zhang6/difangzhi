package com.chronicle.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final AiServiceClient aiServiceClient;

    public List<Map<String, Object>> search(String query, Integer year, String department, int limit) {
        return aiServiceClient.search(query, year, department, limit);
    }

    public Map<String, String> ask(String question) {
        return aiServiceClient.ask(question);
    }
}
