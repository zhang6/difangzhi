package com.chronicle.service;

import com.chronicle.entity.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Service
public class AiServiceClient {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public AiServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String summarize(String content, int length) {
        try {
            Map<String, Object> req = Map.of("content", content, "length", length);
            var resp = restTemplate.postForObject(aiServiceUrl + "/api/summarize", req, Map.class);
            return resp != null && resp.containsKey("summary") ? (String) resp.get("summary") : content.substring(0, Math.min(length, content.length()));
        } catch (Exception e) {
            return content.substring(0, Math.min(length, content.length()));
        }
    }

    public String generateEntry(String title, String existingContent) {
        try {
            Map<String, Object> req = Map.of("title", title, "context", existingContent != null ? existingContent : "");
            var resp = restTemplate.postForObject(aiServiceUrl + "/api/generate-entry", req, Map.class);
            return resp != null && resp.containsKey("content") ? (String) resp.get("content") : "";
        } catch (Exception e) {
            return "";
        }
    }

    public List<Event> generateEvents() {
        try {
            var resp = restTemplate.postForObject(aiServiceUrl + "/api/generate-events", Map.of(), List.class);
            if (resp != null) {
                List<Event> events = new ArrayList<>();
                for (Object o : resp) {
                    if (o instanceof Map m) {
                        Event e = new Event();
                        e.setTitle((String) m.getOrDefault("title", ""));
                        e.setContent((String) m.getOrDefault("content", ""));
                        e.setEventTime(LocalDate.parse((String) m.getOrDefault("time", LocalDate.now().toString())));
                        events.add(e);
                    }
                }
                return events;
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> search(String query, Integer year, String department, int limit) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("query", query);
            if (year != null) req.put("year", year);
            if (department != null) req.put("department", department);
            req.put("limit", limit);
            var resp = restTemplate.postForObject(aiServiceUrl + "/api/search", req, List.class);
            return resp != null ? (List<Map<String, Object>>) resp : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    public Map<String, String> ask(String question) {
        try {
            var resp = restTemplate.postForObject(aiServiceUrl + "/api/ask", Map.of("question", question), Map.class);
            return resp != null ? (Map<String, String>) resp : Map.of("answer", "暂无答案");
        } catch (Exception e) {
            return Map.of("answer", "服务暂时不可用");
        }
    }
}
