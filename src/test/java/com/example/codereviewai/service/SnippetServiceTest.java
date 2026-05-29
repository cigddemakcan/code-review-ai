package com.example.codereviewai.service;

import com.example.codereviewai.dto.request.SnippetRequest;
import com.example.codereviewai.dto.response.SnippetResponse;
import com.example.codereviewai.entity.CodeSnippet;
import com.example.codereviewai.entity.User;
import com.example.codereviewai.exception.ResourceNotFoundException;
import com.example.codereviewai.exception.UnauthorizedException;
import com.example.codereviewai.repository.CodeSnippetRepository;
import com.example.codereviewai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnippetServiceTest {

    @Mock
    private CodeSnippetRepository snippetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SnippetService snippetService;

    private User user;
    private CodeSnippet snippet;
    private SnippetRequest snippetRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");

        snippet = new CodeSnippet();
        snippet.setId(1L);
        snippet.setTitle("Test Snippet");
        snippet.setCode("System.out.println('Hello');");
        snippet.setLanguage("Java");
        snippet.setUser(user);

        snippetRequest = new SnippetRequest();
        snippetRequest.setTitle("Test Snippet");
        snippetRequest.setCode("System.out.println('Hello');");
        snippetRequest.setLanguage("Java");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    }

    @Test
    void createSnippet_ShouldReturnSnippetResponse_WhenValidRequest() {
        when(snippetRepository.save(any(CodeSnippet.class))).thenReturn(snippet);

        SnippetResponse response = snippetService.createSnippet(snippetRequest);

        assertNotNull(response);
        assertEquals("Test Snippet", response.getTitle());
        assertEquals("Java", response.getLanguage());
        assertEquals("testuser", response.getUsername());
        verify(snippetRepository, times(1)).save(any(CodeSnippet.class));
    }

    @Test
    void getMySnippets_ShouldReturnList_WhenUserHasSnippets() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<CodeSnippet> snippetPage = new PageImpl<>(List.of(snippet));
        when(snippetRepository.findByUserId(1L, pageable)).thenReturn(snippetPage);

        Page<SnippetResponse> responses = snippetService.getMySnippets(0, 10);

        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
        assertEquals("Test Snippet", responses.getContent().get(0).getTitle());
    }

    @Test
    void getSnippetById_ShouldThrowException_WhenSnippetNotFound() {
        when(snippetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> snippetService.getSnippetById(99L));
    }

    @Test
    void getSnippetById_ShouldThrowException_WhenNotOwner() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        snippet.setUser(otherUser);

        when(snippetRepository.findById(1L)).thenReturn(Optional.of(snippet));

        assertThrows(UnauthorizedException.class, () -> snippetService.getSnippetById(1L));
    }

    @Test
    void deleteSnippet_ShouldDelete_WhenOwner() {
        when(snippetRepository.findById(1L)).thenReturn(Optional.of(snippet));

        snippetService.deleteSnippet(1L);

        verify(snippetRepository, times(1)).deleteById(1L);
    }
}