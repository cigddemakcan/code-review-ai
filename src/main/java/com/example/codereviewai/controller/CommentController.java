package com.example.codereviewai.controller;

import com.example.codereviewai.dto.request.CommentRequest;
import com.example.codereviewai.dto.response.CommentResponse;
import com.example.codereviewai.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/snippet/{snippetId}")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long snippetId,
                                                      @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addComment(snippetId, request));
    }

    @GetMapping("/snippet/{snippetId}")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long snippetId) {
        return ResponseEntity.ok(commentService.getCommentsByReview(snippetId));
    }
}