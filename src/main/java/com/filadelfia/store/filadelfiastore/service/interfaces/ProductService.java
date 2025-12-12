package com.filadelfia.store.filadelfiastore.service.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;

public interface ProductService {    
    ProductDTO createProduct(ProductDTO request);
    Optional<ProductDTO> getProductById(Long id);
    List<ProductDTO> getAllByCategoryName(String categoryName);
    List<ProductDTO> getAllProducts();
    Page<ProductDTO> getAllProducts(Pageable pageable);
    ProductDTO updateProduct(Long id, ProductDTO request);
    void deleteProduct(Long id);
    void activateProduct(Long id);

    List<ProductDTO> getAllActiveProducts();
    List<ProductDTO> getProductsByCategory(String categoryName);
    List<ProductDTO> searchProducts(String searchTerm);
    List<ProductDTO> getFeaturedProducts();
    
    // Featured products management
    ProductDTO setProductFeatured(Long id, Boolean featured);
    List<ProductDTO> getAllFeaturedProducts();
    
    // Stock management
    List<ProductDTO> getLowStockProducts();
    List<ProductDTO> getLowStockProducts(int threshold);
    Long getLowStockProductsCount();
    Long getLowStockProductsCount(int threshold);
    
    // Image management
    ProductDTO updateProductImage(Long productId, org.springframework.web.multipart.MultipartFile imageFile);
    boolean deleteProductImage(Long productId);
    boolean hasValidImage(Long productId);
}