package com.filadelfia.store.filadelfiastore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductReviewDTO {
    
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private Long orderId;
    private String orderNumber;
    private Double rating;
    private String comment;
    private Boolean isVerifiedPurchase;
    private Boolean isApproved;
    private Integer helpfulCount;
    private Date createdAt;
    private Date updatedAt;
    
    // Helper methods
    public String getRatingStars() {
        if (rating == null) return "☆☆☆☆☆";
        
        StringBuilder stars = new StringBuilder();
        int fullStars = rating.intValue();
        boolean hasHalfStar = (rating - fullStars) >= 0.5;
        
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("☆");
        }
        for (int i = fullStars + (hasHalfStar ? 1 : 0); i < 5; i++) {
            stars.append("☆");
        }
        
        return stars.toString();
    }
    
    public String getFormattedRating() {
        return rating != null ? String.format("%.1f", rating) : "0.0";
    }
    
    public String getShortComment() {
        if (comment == null || comment.length() <= 100) {
            return comment;
        }
        return comment.substring(0, 97) + "...";
    }
    
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
    
    public String getTimeAgo() {
        if (createdAt == null) return "Recentemente";
        
        long diff = System.currentTimeMillis() - createdAt.getTime();
        long days = diff / (24 * 60 * 60 * 1000);
        
        if (days == 0) return "Hoje";
        if (days == 1) return "Ontem";
        if (days < 7) return days + " dias atrás";
        if (days < 30) return (days / 7) + " semanas atrás";
        if (days < 365) return (days / 30) + " meses atrás";
        return (days / 365) + " anos atrás";
    }
}