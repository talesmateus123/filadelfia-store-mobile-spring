package com.filadelfia.store.filadelfiastore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Locale;
import java.text.NumberFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ProductDTO {
    private Long id;
    private Date createdAt;
    private Date updatedAt;
    @NotNull
    private String name;
    @NotNull
    @NotEmpty
    private String description;
    @NotNull
    private BigDecimal price;
    private Integer stock = 0;
    private String imageUrl;
    @NotNull
    private Long categoryId;
    private String categoryName;
    private Boolean active = true;

    public String getPriceFormatted() {
        if (price == null) {
            return "R$ 0,00";
        }
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        return currencyFormat.format(price);
    }

}