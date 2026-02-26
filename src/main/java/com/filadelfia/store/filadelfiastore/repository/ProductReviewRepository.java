package com.filadelfia.store.filadelfiastore.repository;

import com.filadelfia.store.filadelfiastore.model.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    
    // Find reviews by product
    List<ProductReview> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(Long productId);
    Page<ProductReview> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(Long productId, Pageable pageable);
    
    // Find reviews by user
    List<ProductReview> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<ProductReview> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Check if user has already reviewed a product
    Optional<ProductReview> findByProductIdAndUserId(Long productId, Long userId);
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    
    // Find verified purchase reviews
    List<ProductReview> findByProductIdAndIsVerifiedPurchaseTrueOrderByCreatedAtDesc(Long productId);
    
    // Reviews requiring moderation
    List<ProductReview> findByIsApprovedFalseOrderByCreatedAtDesc();
    Page<ProductReview> findByIsApprovedFalseOrderByCreatedAtDesc(Pageable pageable);
    
    // Statistics queries
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    Double getAverageRatingForProduct(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    Long getReviewCountForProduct(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.rating = :rating AND r.isApproved = true")
    Long getReviewCountForProductByRating(@Param("productId") Long productId, @Param("rating") Double rating);
    
    // Find reviews with helpful count greater than threshold
    List<ProductReview> findByProductIdAndHelpfulCountGreaterThanOrderByHelpfulCountDesc(Long productId, Integer threshold);
    
    // Recent reviews
    @Query("SELECT r FROM ProductReview r WHERE r.isApproved = true ORDER BY r.createdAt DESC")
    List<ProductReview> findRecentReviews(Pageable pageable);
}