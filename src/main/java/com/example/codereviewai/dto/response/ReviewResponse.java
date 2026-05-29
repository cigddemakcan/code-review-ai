package com.example.codereviewai.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long snippetId;
    private String snippetTitle;
    private String content;
    private LocalDateTime createdAt;
}