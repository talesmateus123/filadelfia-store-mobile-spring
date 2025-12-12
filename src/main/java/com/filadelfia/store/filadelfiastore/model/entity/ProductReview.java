package com.filadelfia.store.filadelfiastore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;

@Entity
@Table(name = "product_reviews")
@Getter
@Setter
@NoArgsConstructor
public class ProductReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // Optional: link to the order where this product was purchased
    
    @Column(nullable = false)
    private Double rating; // 1.0 to 5.0
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "is_verified_purchase")
    private Boolean isVerifiedPurchase = false; // True if user actually bought this product
    
    @Column(name = "is_approved")
    private Boolean isApproved = true; // For moderation if needed
    
    @Column(name = "helpful_count")
    private Integer helpfulCount = 0; // Number of users who found this review helpful
    
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date(System.currentTimeMillis());
    
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date(System.currentTimeMillis());
    
    // Constructor
    public ProductReview(Product product, User user, Double rating, String comment) {
        this.product = product;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
    }
    
    // Constructor with order (verified purchase)
    public ProductReview(Product product, User user, Order order, Double rating, String comment) {
        this.product = product;
        this.user = user;
        this.order = order;
        this.rating = rating;
        this.comment = comment;
        this.isVerifiedPurchase = true;
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
    }
    
    // Business methods
    public void updateReview(Double rating, String comment) {
        this.rating = rating;
        this.comment = comment;
        this.updatedAt = new Date(System.currentTimeMillis());
    }
    
    public void incrementHelpfulCount() {
        this.helpfulCount++;
        this.updatedAt = new Date(System.currentTimeMillis());
    }
    
    public String getRatingStars() {
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
}