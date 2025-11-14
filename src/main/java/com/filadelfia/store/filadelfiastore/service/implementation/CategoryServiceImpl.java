package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.exception.custom.DuplicateCategoryException;
import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
import com.filadelfia.store.filadelfiastore.model.dto.CategoryDetailedDTO;
import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Category;
import com.filadelfia.store.filadelfiastore.model.mapper.CategoryMapper;
import com.filadelfia.store.filadelfiastore.model.mapper.ProductMapper;
import com.filadelfia.store.filadelfiastore.repository.CategoryRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService  {

    private final ProductMapper productMapper;

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
  
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue()
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> searchCategories(String searchTerm) {
        // Validate and sanitize search term
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveCategories();
        }
        
        // Limit search term length to prevent abuse
        String sanitizedTerm = searchTerm.trim();
        if (sanitizedTerm.length() > 100) {
            sanitizedTerm = sanitizedTerm.substring(0, 100);
        }
        
        return categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(sanitizedTerm)
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDetailedDTO> getCategoryDetailedById(Long id) {
        return categoryRepository.findById(id)
            .map(this::toDetailedDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .map(categoryMapper::toDTO);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateCategoryException("Já existe uma categoria com o nome: " + request.getName());
        }

        Category savedCategory = categoryRepository.save(categoryMapper.toEntity(request));
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
            .map(categoryMapper::toDTO);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO request) {
    Category existing = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    
    // Verifica se existe outra categoria com o mesmo nome (excluindo a atual)
    if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
        throw new DuplicateCategoryException("Já existe uma categoria com o nome: " + request.getName());
    }

    existing.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));

    // Copy properties from request to existing entity, ignoring id and password
    BeanUtils.copyProperties(request, existing, "id", "createdAt");
    Category updated = categoryRepository.save(existing);
    return categoryMapper.toDTO(updated);
}

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
                
        category.setActive(false);
        category.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
        categoryRepository.save(category);
    }


    public CategoryDetailedDTO toDetailedDTO(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryDetailedDTO dto = new CategoryDetailedDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setActive(category.getActive());
        if (category.getProducts() != null) {
            List<ProductDTO> productDTOs = category.getProducts().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
            dto.setProducts(productDTOs);
        } else {
            dto.setProducts(Collections.emptyList());
        }
        return dto;
    }

}