package com.example.codereviewai.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class AIReviewResponse {
    private String summary;
    private List<String> bugs;
    private List<String> securityIssues;
    private List<String> performanceIssues;
    private List<String> suggestions;
    private String improvedCode;
}