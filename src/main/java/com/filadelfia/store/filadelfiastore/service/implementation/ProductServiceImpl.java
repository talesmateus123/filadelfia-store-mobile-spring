package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Product;
import com.filadelfia.store.filadelfiastore.model.mapper.ProductMapper;
import com.filadelfia.store.filadelfiastore.repository.ProductRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.ProductService;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService  {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;      
  
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductDTO> getAllActiveProducts() {
        return productRepository.findByActiveTrue()
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findTop4ByActiveTrueOrderByIdDesc()
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryNameAndActiveTrue(categoryName)
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> searchProducts(String searchTerm) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchTerm)
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
            .map(productMapper::toDTO);
    }

    @Override
    public List<ProductDTO> getAllByCategoryName(String categoryName) {
        return productRepository.findByCategoryNameAndActiveTrue(categoryName)
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ProductDTO createProduct(ProductDTO request) {        
        Product savedProduct = productRepository.save(productMapper.toEntity(request));
        return productMapper.toDTO(savedProduct);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO request) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Copy non-id properties from request to existing entity
        BeanUtils.copyProperties(request, existing, "id");
        Product updated = productRepository.save(existing);
        return productMapper.toDTO(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

}