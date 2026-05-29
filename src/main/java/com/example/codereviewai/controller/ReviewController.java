package com.example.codereviewai.controller;

import com.example.codereviewai.dto.response.ReviewResponse;
import com.example.codereviewai.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.codereviewai.dto.response.AIReviewResponse;
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/snippet/{snippetId}")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long snippetId) {
        return ResponseEntity.ok(reviewService.createReview(snippetId));
    }

    @GetMapping("/snippet/{snippetId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long snippetId) {
        return ResponseEntity.ok(reviewService.getReviewBySnippetId(snippetId));
    }
    @GetMapping("/snippet/{snippetId}/parsed")
    public ResponseEntity<AIReviewResponse> getParsedReview(@PathVariable Long snippetId) {
        return ResponseEntity.ok(reviewService.getParsedReview(snippetId));
    }
}