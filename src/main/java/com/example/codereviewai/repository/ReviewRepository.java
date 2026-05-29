package com.example.codereviewai.repository;

import com.example.codereviewai.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByCodeSnippetId(Long snippetId);
}