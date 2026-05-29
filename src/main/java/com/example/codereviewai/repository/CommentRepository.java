package com.example.codereviewai.repository;

import com.example.codereviewai.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReviewId(Long reviewId);
    List<Comment> findByUserId(Long userId);
}