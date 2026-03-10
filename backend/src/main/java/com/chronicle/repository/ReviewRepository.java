package com.chronicle.repository;

import com.chronicle.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEntryId(Long entryId);
}
