package com.filadelfia.store.filadelfiastore.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;

public interface ProductService {    
    ProductDTO createProduct(ProductDTO request);
    Optional<ProductDTO> getProductById(Long id);
    List<ProductDTO> getAllByCategoryName(String categoryName);
    List<ProductDTO> getAllProducts();
    ProductDTO updateProduct(Long id, ProductDTO request);
    void deleteProduct(Long id);

    List<ProductDTO> getAllActiveProducts();
    List<ProductDTO> getProductsByCategory(String categoryName);
    List<ProductDTO> searchProducts(String searchTerm);
    List<ProductDTO> getFeaturedProducts();
}