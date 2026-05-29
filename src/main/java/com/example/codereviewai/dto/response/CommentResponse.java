package com.example.codereviewai.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private String username;
    private LocalDateTime createdAt;
}