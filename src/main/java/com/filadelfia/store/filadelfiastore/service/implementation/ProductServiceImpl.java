package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Product;
import com.filadelfia.store.filadelfiastore.model.mapper.CategoryMapper;
import com.filadelfia.store.filadelfiastore.model.mapper.ProductMapper;
import com.filadelfia.store.filadelfiastore.repository.ProductRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;
import com.filadelfia.store.filadelfiastore.service.interfaces.ProductService;
import com.filadelfia.store.filadelfiastore.util.PageableValidator;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService  {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final PageableValidator pageableValidator;

    private final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
        "id", "name", "price", "createdAt", "updatedAt", "category"
    );

    public ProductServiceImpl(ProductRepository productRepository, CategoryService categoryService, ProductMapper productMapper, CategoryMapper categoryMapper, PageableValidator pageableValidator) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
        this.pageableValidator = pageableValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllActiveProducts() {
        return productRepository.findByActiveTrue()
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findTop4ByActiveTrueOrderByIdDesc()
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryNameAndActiveTrue(categoryName)
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String searchTerm) {
        // Validate and sanitize search term
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveProducts();
        }
        
        // Limit search term length to prevent abuse
        String sanitizedTerm = searchTerm.trim();
        if (sanitizedTerm.length() > 100) {
            sanitizedTerm = sanitizedTerm.substring(0, 100);
        }
        
        return productRepository.findByNameOrDescriptionContainingIgnoreCaseAndActiveTrue(sanitizedTerm)
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
            .map(productMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllByCategoryName(String categoryName) {
        return productRepository.findByCategoryNameAndActiveTrue(categoryName)
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO request) {
        CategoryDTO category = categoryService.getCategoryById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        Product product = productMapper.toEntity(request);
        product.setCategory(categoryMapper.toEntity(category));
        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Pageable safePageable = pageableValidator.validateAndSanitize(
            pageable, 
            ALLOWED_SORT_PROPERTIES,
            "name"
        );
        
        return productRepository.findAll(safePageable)
            .map(productMapper::toDTO);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO request) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        CategoryDTO category = categoryService.getCategoryById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        existing.setCategory(categoryMapper.toEntity(category));
        existing.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));

        // Copy properties from request to existing entity, ignoring id and password
        BeanUtils.copyProperties(request, existing, "id", "createdAt");
        Product updated = productRepository.save(existing);
        return productMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        
        product.setActive(false);
        product.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void activateProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        
        product.setActive(true);
        product.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts() {
        return getLowStockProducts(10); // Default threshold of 10 units
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(int threshold) {
        return productRepository.findByActiveTrueAndStockLessThan(threshold)
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLowStockProductsCount() {
        return getLowStockProductsCount(10); // Default threshold of 10 units
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLowStockProductsCount(int threshold) {
        return productRepository.countByActiveTrueAndStockLessThan(threshold);
    }

}