package com.chronicle.controller;

import com.chronicle.entity.Review;
import com.chronicle.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<Review> list(@RequestParam Long entryId) {
        return reviewService.findByEntryId(entryId);
    }

    @PostMapping
    public Review create(@RequestBody Review review) {
        return reviewService.save(review);
    }

    @PutMapping("/{id}")
    public Review update(@PathVariable Long id, @RequestBody Review review) {
        review.setId(id);
        return reviewService.save(review);
    }

    @PutMapping("/{id}/approve")
    public Review approve(@PathVariable Long id) {
        return reviewService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public Review reject(@PathVariable Long id, @RequestBody ReviewRejectRequest request) {
        return reviewService.reject(id, request.comment());
    }

    public record ReviewRejectRequest(String comment) {}
}
