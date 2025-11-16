package com.filadelfia.store.filadelfiastore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private List<CartItemDTO> items;
    private BigDecimal total;
    private Date createdAt;
    private Date updatedAt;

    public int getTotalItems() {
        return items != null ? items.stream().mapToInt(CartItemDTO::getQuantity).sum() : 0;
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
