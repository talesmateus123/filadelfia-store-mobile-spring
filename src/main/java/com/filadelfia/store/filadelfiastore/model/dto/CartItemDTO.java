package com.filadelfia.store.filadelfiastore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    
    private Long id;
    private Long cartId;
    private Long productId;
    private String productName;
    private String productDescription;
    private String productImageUrl;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Date createdAt;
    private Date updatedAt;

    public int getTotalItems() {
        return quantity != null ? quantity : 0;
    }

    public boolean isEmpty() {
        return quantity == null || quantity <= 0;
    }
}
