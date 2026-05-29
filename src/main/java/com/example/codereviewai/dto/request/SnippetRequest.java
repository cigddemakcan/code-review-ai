package com.example.codereviewai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SnippetRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;
    @NotBlank(message = "Code cannot be blank")
    private String code;
    @NotBlank(message = "Language cannot be blank")
    private String language;
}