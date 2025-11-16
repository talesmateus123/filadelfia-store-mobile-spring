package com.filadelfia.store.filadelfiastore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.filadelfia.store.filadelfiastore.model.enums.OrderStatus;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_confirmed")
    private Boolean paymentConfirmed = false;
    
    // Shipping Address
    @Column(nullable = false)
    private String shippingStreet;
    
    @Column(nullable = false)
    private String shippingNumber;
    
    private String shippingComplement;
    
    @Column(nullable = false)
    private String shippingNeighborhood;
    
    @Column(nullable = false)
    private String shippingCity;
    
    @Column(nullable = false)
    private String shippingState;
    
    @Column(nullable = false, length = 10)
    private String shippingZipCode;
    
    private String notes;
    
    @Column(name = "tracking_code")
    private String trackingCode;
    
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date(System.currentTimeMillis());
    
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date(System.currentTimeMillis());
    
    @Column(name = "shipped_at")
    private Date shippedAt;
    
    @Column(name = "delivered_at")
    private Date deliveredAt;

    // Custom constructor
    public Order(User user, PaymentMethod paymentMethod) {
        this.user = user;
        this.paymentMethod = paymentMethod;
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
        this.status = OrderStatus.PENDING;
        this.paymentConfirmed = false;
    }

    // Business methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        calculateTotals();
    }

    public void calculateTotals() {
        subtotal = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (shippingCost == null) {
            shippingCost = BigDecimal.ZERO;
        }
        
        total = subtotal.add(shippingCost);
    }

    public int getTotalItems() {
        return items.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }

    public String getFullShippingAddress() {
        StringBuilder address = new StringBuilder();
        address.append(shippingStreet).append(", ").append(shippingNumber);
        if (shippingComplement != null && !shippingComplement.trim().isEmpty()) {
            address.append(", ").append(shippingComplement);
        }
        address.append(", ").append(shippingNeighborhood);
        address.append(", ").append(shippingCity).append(" - ").append(shippingState);
        address.append(", CEP: ").append(shippingZipCode);
        return address.toString();
    }
}
