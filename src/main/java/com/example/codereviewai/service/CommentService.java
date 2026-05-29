package com.example.codereviewai.service;

import com.example.codereviewai.dto.request.CommentRequest;
import com.example.codereviewai.dto.response.CommentResponse;
import com.example.codereviewai.entity.Comment;
import com.example.codereviewai.entity.Review;
import com.example.codereviewai.entity.User;
import com.example.codereviewai.exception.ResourceNotFoundException;
import com.example.codereviewai.repository.CommentRepository;
import com.example.codereviewai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUsername(comment.getUser().getUsername());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }

    public CommentResponse addComment(Long snippetId, CommentRequest request) {
        Review review = reviewService.getReviewEntityBySnippetId(snippetId);
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setReview(review);
        comment.setUser(getCurrentUser());
        return toResponse(commentRepository.save(comment));
    }

    public List<CommentResponse> getCommentsByReview(Long snippetId) {
        Review review = reviewService.getReviewEntityBySnippetId(snippetId);
        return commentRepository.findByReviewId(review.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }
}