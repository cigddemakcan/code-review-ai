package com.example.codereviewai.controller;

import com.example.codereviewai.dto.request.TagRequest;
import com.example.codereviewai.dto.response.TagResponse;
import com.example.codereviewai.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping("/snippet/{snippetId}")
    public ResponseEntity<TagResponse> addTag(@PathVariable Long snippetId,
                                              @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.addTag(snippetId, request));
    }

    @GetMapping("/snippet/{snippetId}")
    public ResponseEntity<List<TagResponse>> getTags(@PathVariable Long snippetId) {
        return ResponseEntity.ok(tagService.getTagsBySnippet(snippetId));
    }
}