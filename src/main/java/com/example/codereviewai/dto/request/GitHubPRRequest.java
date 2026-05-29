package com.example.codereviewai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GitHubPRRequest {
    @NotBlank(message = "PR URL cannot be blank")
    private String prUrl;
}