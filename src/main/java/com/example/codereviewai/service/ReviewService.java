package com.example.codereviewai.service;

import com.example.codereviewai.dto.response.AIReviewResponse;
import com.example.codereviewai.dto.response.ReviewResponse;
import com.example.codereviewai.entity.CodeSnippet;
import com.example.codereviewai.entity.Review;
import com.example.codereviewai.exception.DuplicateResourceException;
import com.example.codereviewai.exception.ResourceNotFoundException;
import com.example.codereviewai.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SnippetService snippetService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.api.key}")
    private String apiKey;

    private ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setSnippetId(review.getCodeSnippet().getId());
        response.setSnippetTitle(review.getCodeSnippet().getTitle());
        response.setContent(review.getContent());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }

    public ReviewResponse createReview(Long snippetId) {
        log.info("Creating review for snippet id: {}", snippetId);
        CodeSnippet snippet = snippetService.getSnippetEntityById(snippetId);

        if (reviewRepository.findByCodeSnippetId(snippetId).isPresent()) {
            log.warn("Review already exists for snippet id: {}", snippetId);
            throw new DuplicateResourceException("Review already exists for this snippet");
        }

        String reviewContent;
        try {
            log.debug("Calling OpenRouter API for snippet id: {}", snippetId);
            reviewContent = callOpenRouterApi(snippet.getCode(), snippet.getLanguage());
            log.info("Review created successfully for snippet id: {}", snippetId);
        } catch (Exception e) {
            log.error("Error calling AI service for snippet id: {}", snippetId, e);
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                throw new RuntimeException("AI service rate limit exceeded. Please try again in a few minutes.");
            }
            throw new RuntimeException("AI service is currently unavailable. Please try again later.");
        }

        Review review = new Review();
        review.setCodeSnippet(snippet);
        review.setContent(reviewContent);
        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse getReviewBySnippetId(Long snippetId) {
        log.info("Getting review for snippet id: {}", snippetId);
        Review review = reviewRepository.findByCodeSnippetId(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return toResponse(review);
    }

    public AIReviewResponse getParsedReview(Long snippetId) {
        log.info("Getting parsed review for snippet id: {}", snippetId);
        Review review = reviewRepository.findByCodeSnippetId(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        try {
            String content = review.getContent();
            content = content.replaceAll("```json", "").replaceAll("```", "").trim();
            return objectMapper.readValue(content, AIReviewResponse.class);
        } catch (Exception e) {
            log.warn("Could not parse review as JSON for snippet id: {}", snippetId);
            AIReviewResponse fallback = new AIReviewResponse();
            fallback.setSummary(review.getContent());
            return fallback;
        }
    }

    public Review getReviewEntityBySnippetId(Long snippetId) {
        return reviewRepository.findByCodeSnippetId(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
    }

    private String callOpenRouterApi(String code, String language) {
        String url = "https://openrouter.ai/api/v1/chat/completions";

        String prompt = "You are a code review assistant. Review the following " + language + " code. " +
                "Return ONLY a valid JSON object with no additional text, no markdown, no code blocks. " +
                "The JSON must have exactly these fields: " +
                "summary (string), bugs (array of strings), securityIssues (array of strings), " +
                "performanceIssues (array of strings), suggestions (array of strings), improvedCode (string). " +
                "Code to review:\n\n" + code;

        Map<String, Object> requestBody = Map.of(
                "model", "google/gemini-2.0-flash-001",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "http://localhost:8080");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, Object> body = response.getBody();
        List<Map> choices = (List<Map>) body.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }
}