package com.chronicle.service;

import com.chronicle.entity.Review;
import com.chronicle.entity.Review.ReviewStatus;
import com.chronicle.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public List<Review> findByEntryId(Long entryId) {
        return reviewRepository.findByEntryId(entryId);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public Review approve(Long id) {
        Review review = reviewRepository.findById(id).orElseThrow();
        review.setStatus(ReviewStatus.APPROVED);
        review.setReviewedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public Review reject(Long id, String comment) {
        Review review = reviewRepository.findById(id).orElseThrow();
        review.setStatus(ReviewStatus.REJECTED);
        review.setComment(comment);
        review.setReviewedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }
}
