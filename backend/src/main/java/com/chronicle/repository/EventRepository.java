package com.chronicle.repository;

import com.chronicle.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventTimeBetweenOrderByEventTimeDesc(LocalDate start, LocalDate end);
}
