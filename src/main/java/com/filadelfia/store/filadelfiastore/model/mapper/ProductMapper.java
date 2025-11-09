package com.filadelfia.store.filadelfiastore.model.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Product;

import lombok.Builder;

@Component
@Builder
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    ProductMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }
    
    public ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
         dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.getActive());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        return dto;
    }
    
    public Product toEntity(ProductDTO productDTO) {
        Product product = new Product();
        product.setId(productDTO.getId());
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setImageUrl(productDTO.getImageUrl());
        product.setActive(productDTO.getActive());

        return product;
    }
}
