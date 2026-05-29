package com.example.codereviewai.service;

import com.example.codereviewai.dto.response.ReviewResponse;
import com.example.codereviewai.entity.CodeSnippet;
import com.example.codereviewai.entity.Review;
import com.example.codereviewai.entity.User;
import com.example.codereviewai.exception.DuplicateResourceException;
import com.example.codereviewai.exception.ResourceNotFoundException;
import com.example.codereviewai.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SnippetService snippetService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private CodeSnippet snippet;
    private Review review;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        snippet = new CodeSnippet();
        snippet.setId(1L);
        snippet.setTitle("Test Snippet");
        snippet.setCode("System.out.println('Hello');");
        snippet.setLanguage("Java");
        snippet.setUser(user);

        review = new Review();
        review.setId(1L);
        review.setCodeSnippet(snippet);
        review.setContent("{\"summary\":\"Good code\",\"bugs\":[],\"securityIssues\":[],\"performanceIssues\":[],\"suggestions\":[],\"improvedCode\":\"\"}");
    }

    @Test
    void createReview_ShouldThrowException_WhenReviewAlreadyExists() {
        when(snippetService.getSnippetEntityById(1L)).thenReturn(snippet);
        when(reviewRepository.findByCodeSnippetId(1L)).thenReturn(Optional.of(review));

        assertThrows(DuplicateResourceException.class, () -> reviewService.createReview(1L));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewBySnippetId_ShouldReturnReview_WhenExists() {
        when(reviewRepository.findByCodeSnippetId(1L)).thenReturn(Optional.of(review));

        ReviewResponse response = reviewService.getReviewBySnippetId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getSnippetId());
        assertEquals("Test Snippet", response.getSnippetTitle());
    }

    @Test
    void getReviewBySnippetId_ShouldThrowException_WhenNotFound() {
        when(reviewRepository.findByCodeSnippetId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewBySnippetId(99L));
    }

    @Test
    void getReviewEntityBySnippetId_ShouldReturnEntity_WhenExists() {
        when(reviewRepository.findByCodeSnippetId(1L)).thenReturn(Optional.of(review));

        Review result = reviewService.getReviewEntityBySnippetId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}