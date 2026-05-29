package com.example.codereviewai.controller;

import com.example.codereviewai.dto.request.GitHubPRRequest;
import com.example.codereviewai.dto.response.GitHubPRReviewResponse;
import com.example.codereviewai.service.GitHubPRService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubPRController {

    private final GitHubPRService gitHubPRService;

    @PostMapping("/review")
    public ResponseEntity<GitHubPRReviewResponse> reviewPR(@Valid @RequestBody GitHubPRRequest request) {
        return ResponseEntity.ok(gitHubPRService.reviewPR(request));
    }
}