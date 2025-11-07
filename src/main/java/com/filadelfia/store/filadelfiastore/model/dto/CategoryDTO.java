package com.filadelfia.store.filadelfiastore.model.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private List<ProductDTO> products = new ArrayList<ProductDTO>();
    private Boolean active = true;

}
