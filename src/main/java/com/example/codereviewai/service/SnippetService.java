package com.example.codereviewai.service;

import com.example.codereviewai.dto.request.SnippetRequest;
import com.example.codereviewai.dto.response.SnippetResponse;
import com.example.codereviewai.entity.CodeSnippet;
import com.example.codereviewai.entity.User;
import com.example.codereviewai.exception.ResourceNotFoundException;
import com.example.codereviewai.exception.UnauthorizedException;
import com.example.codereviewai.repository.CodeSnippetRepository;
import com.example.codereviewai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnippetService {

    private final CodeSnippetRepository snippetRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SnippetResponse toResponse(CodeSnippet snippet) {
        SnippetResponse response = new SnippetResponse();
        response.setId(snippet.getId());
        response.setTitle(snippet.getTitle());
        response.setCode(snippet.getCode());
        response.setLanguage(snippet.getLanguage());
        response.setUsername(snippet.getUser().getUsername());
        response.setCreatedAt(snippet.getCreatedAt());
        return response;
    }

    public SnippetResponse createSnippet(SnippetRequest request) {
        log.info("Creating snippet for user: {}", getCurrentUser().getUsername());
        CodeSnippet snippet = new CodeSnippet();
        snippet.setTitle(request.getTitle());
        snippet.setCode(request.getCode());
        snippet.setLanguage(request.getLanguage());
        snippet.setUser(getCurrentUser());
        SnippetResponse response = toResponse(snippetRepository.save(snippet));
        log.info("Snippet created successfully with id: {}", response.getId());
        return response;
    }

    public Page<SnippetResponse> getMySnippets(int page, int size) {
        log.info("Getting snippets for user: {}", getCurrentUser().getUsername());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return snippetRepository.findByUserId(getCurrentUser().getId(), pageable)
                .map(this::toResponse);
    }

    public SnippetResponse getSnippetById(Long id) {
        log.info("Getting snippet with id: {}", id);
        CodeSnippet snippet = snippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found"));
        if (!snippet.getUser().getUsername().equals(getCurrentUser().getUsername())) {
            log.warn("Unauthorized access attempt to snippet id: {}", id);
            throw new UnauthorizedException("Access denied");
        }
        return toResponse(snippet);
    }

    public CodeSnippet getSnippetEntityById(Long id) {
        CodeSnippet snippet = snippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found"));
        if (!snippet.getUser().getUsername().equals(getCurrentUser().getUsername())) {
            log.warn("Unauthorized access attempt to snippet entity id: {}", id);
            throw new UnauthorizedException("Access denied");
        }
        return snippet;
    }

    public void deleteSnippet(Long id) {
        log.info("Deleting snippet with id: {}", id);
        CodeSnippet snippet = snippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet not found"));
        if (!snippet.getUser().getUsername().equals(getCurrentUser().getUsername())) {
            log.warn("Unauthorized delete attempt to snippet id: {}", id);
            throw new UnauthorizedException("Access denied");
        }
        snippetRepository.deleteById(id);
        log.info("Snippet deleted successfully with id: {}", id);
    }
}