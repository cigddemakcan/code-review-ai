package com.example.codereviewai.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GitHubPRReviewResponse {
    private String prUrl;
    private String repository;
    private Integer prNumber;
    private String summary;
    private List<String> bugs;
    private List<String> securityIssues;
    private List<String> performanceIssues;
    private List<String> suggestions;
    private LocalDateTime createdAt;
}