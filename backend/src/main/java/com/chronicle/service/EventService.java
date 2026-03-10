package com.chronicle.service;

import com.chronicle.entity.Event;
import com.chronicle.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AiServiceClient aiServiceClient;

    public List<Event> search(LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return eventRepository.findByEventTimeBetweenOrderByEventTimeDesc(start, end);
        }
        return eventRepository.findAll();
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    public void deleteById(Long id) {
        eventRepository.deleteById(id);
    }

    public List<Event> aiGenerate() {
        return aiServiceClient.generateEvents();
    }
}
