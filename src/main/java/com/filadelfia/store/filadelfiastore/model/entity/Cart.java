package com.filadelfia.store.filadelfiastore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date(System.currentTimeMillis());
    
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date(System.currentTimeMillis());

    // Custom constructor
    public Cart(User user) {
        this.user = user;
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
        this.total = BigDecimal.ZERO;
    }

    // Business methods
    public void addItem(CartItem item) {
        // Check if product already exists in cart
        CartItem existingItem = findItemByProduct(item.getProduct());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            existingItem.calculateSubtotal();
        } else {
            items.add(item);
            item.setCart(this);
        }
        calculateTotal();
        this.updatedAt = new Date(System.currentTimeMillis());
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        calculateTotal();
        this.updatedAt = new Date(System.currentTimeMillis());
    }

    public void updateItemQuantity(Long productId, Integer quantity) {
        CartItem item = findItemByProductId(productId);
        if (item != null) {
            if (quantity <= 0) {
                removeItem(item);
            } else {
                item.setQuantity(quantity);
                item.calculateSubtotal();
                calculateTotal();
                this.updatedAt = new Date(System.currentTimeMillis());
            }
        }
    }

    public void clear() {
        items.clear();
        calculateTotal();
        this.updatedAt = new Date(System.currentTimeMillis());
    }

    public void calculateTotal() {
        total = items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public CartItem findItemByProduct(Product product) {
        return items.stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .findFirst()
            .orElse(null);
    }

    public CartItem findItemByProductId(Long productId) {
        return items.stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .orElse(null);
    }
}
