package com.example.codereviewai.controller;

import com.example.codereviewai.dto.request.SnippetRequest;
import com.example.codereviewai.dto.response.SnippetResponse;
import com.example.codereviewai.service.SnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snippets")
@RequiredArgsConstructor
public class SnippetController {

    private final SnippetService snippetService;

    @PostMapping
    public ResponseEntity<SnippetResponse> createSnippet(@Valid @RequestBody SnippetRequest request) {
        return ResponseEntity.ok(snippetService.createSnippet(request));
    }

    @GetMapping
    public ResponseEntity<Page<SnippetResponse>> getMySnippets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(snippetService.getMySnippets(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnippetResponse> getSnippet(@PathVariable Long id) {
        return ResponseEntity.ok(snippetService.getSnippetById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSnippet(@PathVariable Long id) {
        snippetService.deleteSnippet(id);
        return ResponseEntity.ok(Map.of("message", "Snippet deleted successfully"));
    }
}