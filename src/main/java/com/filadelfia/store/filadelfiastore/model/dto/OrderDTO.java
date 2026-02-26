package com.filadelfia.store.filadelfiastore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import com.filadelfia.store.filadelfiastore.model.enums.OrderStatus;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userName;
    private String userEmail;
    private List<OrderItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private Boolean paymentConfirmed;
    
    // Shipping Address
    private String shippingStreet;
    private String shippingNumber;
    private String shippingComplement;
    private String shippingNeighborhood;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    
    private String notes;
    private String trackingCode;
    private Date createdAt;
    private Date updatedAt;
    private Date shippedAt;
    private Date deliveredAt;

    // Helper methods
    public int getTotalItems() {
        return items != null ? items.stream()
            .mapToInt(OrderItemDTO::getQuantity)
            .sum() : 0;
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public String getFullShippingAddress() {
        StringBuilder address = new StringBuilder();
        if (shippingStreet != null) {
            address.append(shippingStreet).append(", ").append(shippingNumber);
            if (shippingComplement != null && !shippingComplement.trim().isEmpty()) {
                address.append(", ").append(shippingComplement);
            }
            address.append(", ").append(shippingNeighborhood);
            address.append(", ").append(shippingCity).append(" - ").append(shippingState);
            address.append(", CEP: ").append(shippingZipCode);
        }
        return address.toString();
    }
}
