package com.filadelfia.store.filadelfiastore.service.interfaces;

import com.filadelfia.store.filadelfiastore.model.dto.ProductReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductReviewService {
    
    // Review CRUD operations
    ProductReviewDTO createReview(Long userId, Long productId, Double rating, String comment);
    ProductReviewDTO createReviewFromOrder(Long userId, Long productId, Long orderId, Double rating, String comment);
    ProductReviewDTO updateReview(Long userId, Long reviewId, Double rating, String comment);
    void deleteReview(Long userId, Long reviewId);
    ProductReviewDTO getReviewById(Long reviewId);
    
    // Product reviews
    List<ProductReviewDTO> getProductReviews(Long productId);
    Page<ProductReviewDTO> getProductReviews(Long productId, Pageable pageable);
    List<ProductReviewDTO> getVerifiedProductReviews(Long productId);
    
    // User reviews
    List<ProductReviewDTO> getUserReviews(Long userId);
    Page<ProductReviewDTO> getUserReviews(Long userId, Pageable pageable);
    
    // Review validation
    boolean canUserReviewProduct(Long userId, Long productId);
    boolean hasUserReviewedProduct(Long userId, Long productId);
    boolean hasUserPurchasedProduct(Long userId, Long productId);
    
    // Review statistics
    Double getAverageRating(Long productId);
    Long getReviewCount(Long productId);
    ProductReviewStatsDTO getProductReviewStats(Long productId);
    
    // Moderation
    List<ProductReviewDTO> getPendingReviews();
    ProductReviewDTO approveReview(Long reviewId);
    ProductReviewDTO rejectReview(Long reviewId);
    
    // Helpful reviews
    void markReviewHelpful(Long reviewId);
    List<ProductReviewDTO> getMostHelpfulReviews(Long productId);
    
    // Recent reviews
    List<ProductReviewDTO> getRecentReviews(int limit);
    
    // Stats DTO class
    class ProductReviewStatsDTO {
        private Double averageRating;
        private Long totalReviews;
        private Long fiveStarCount;
        private Long fourStarCount;
        private Long threeStarCount;
        private Long twoStarCount;
        private Long oneStarCount;
        
        public ProductReviewStatsDTO(Double averageRating, Long totalReviews, Long fiveStarCount, 
                                   Long fourStarCount, Long threeStarCount, Long twoStarCount, Long oneStarCount) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.fiveStarCount = fiveStarCount;
            this.fourStarCount = fourStarCount;
            this.threeStarCount = threeStarCount;
            this.twoStarCount = twoStarCount;
            this.oneStarCount = oneStarCount;
        }
        
        // Getters
        public Double getAverageRating() { return averageRating; }
        public Long getTotalReviews() { return totalReviews; }
        public Long getFiveStarCount() { return fiveStarCount; }
        public Long getFourStarCount() { return fourStarCount; }
        public Long getThreeStarCount() { return threeStarCount; }
        public Long getTwoStarCount() { return twoStarCount; }
        public Long getOneStarCount() { return oneStarCount; }
        
        public Double getFiveStarPercentage() {
            return totalReviews > 0 ? (fiveStarCount * 100.0) / totalReviews : 0.0;
        }
        
        public Double getFourStarPercentage() {
            return totalReviews > 0 ? (fourStarCount * 100.0) / totalReviews : 0.0;
        }
        
        public Double getThreeStarPercentage() {
            return totalReviews > 0 ? (threeStarCount * 100.0) / totalReviews : 0.0;
        }
        
        public Double getTwoStarPercentage() {
            return totalReviews > 0 ? (twoStarCount * 100.0) / totalReviews : 0.0;
        }
        
        public Double getOneStarPercentage() {
            return totalReviews > 0 ? (oneStarCount * 100.0) / totalReviews : 0.0;
        }
    }
}