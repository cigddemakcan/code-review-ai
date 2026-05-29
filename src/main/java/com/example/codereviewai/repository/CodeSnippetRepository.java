package com.example.codereviewai.repository;

import com.example.codereviewai.entity.CodeSnippet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {
    List<CodeSnippet> findByUserId(Long userId);
    List<CodeSnippet> findByLanguage(String language);
    Page<CodeSnippet> findByUserId(Long userId, Pageable pageable);
}