package com.example.codereviewai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
}