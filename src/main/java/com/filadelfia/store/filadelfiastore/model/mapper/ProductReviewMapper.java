package com.filadelfia.store.filadelfiastore.model.mapper;

import com.filadelfia.store.filadelfiastore.model.dto.ProductReviewDTO;
import com.filadelfia.store.filadelfiastore.model.entity.ProductReview;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductReviewMapper {
    
    public ProductReviewDTO toDTO(ProductReview review) {
        if (review == null) {
            return null;
        }
        
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getName());
        
        if (review.getOrder() != null) {
            dto.setOrderId(review.getOrder().getId());
            dto.setOrderNumber(review.getOrder().getOrderNumber());
        }
        
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setIsVerifiedPurchase(review.getIsVerifiedPurchase());
        dto.setIsApproved(review.getIsApproved());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        
        return dto;
    }
    
    public ProductReview toEntity(ProductReviewDTO dto) {
        if (dto == null) {
            return null;
        }
        
        ProductReview review = new ProductReview();
        review.setId(dto.getId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setIsVerifiedPurchase(dto.getIsVerifiedPurchase());
        review.setIsApproved(dto.getIsApproved());
        review.setHelpfulCount(dto.getHelpfulCount());
        
        return review;
    }
    
    public List<ProductReviewDTO> toDTOList(List<ProductReview> reviews) {
        return reviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}