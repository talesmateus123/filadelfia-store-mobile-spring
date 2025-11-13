package com.filadelfia.store.filadelfiastore.model.dto;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CategoryDetailedDTO {
    private Long id;
    private Date createdAt;
    private Date updatedAt;
    @NotNull
    private String name;
    @NotNull
    private String description;
    private Boolean active = true;
    private List<ProductDTO> products = new ArrayList<>();

}
