package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserEmail(Long postId, String userEmail);
    long countByPostIdAndReaction(Long postId, String reaction);
    void deleteByPostId(Long postId);
}
