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
import org.springframework.stereotype.Service;

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
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue()
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> searchCategories(String searchTerm) {
        return categoryRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchTerm)
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    
    @Override
    public Optional<CategoryDetailedDTO> getCategoryDetailedById(Long id) {
        return categoryRepository.findById(id)
            .map(this::toDetailedDTO);
    }

    @Override
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .map(categoryMapper::toDTO);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateCategoryException("Já existe uma categoria com o nome: " + request.getName());
        }

        Category savedCategory = categoryRepository.save(categoryMapper.toEntity(request));
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
            .stream()
            .map(categoryMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO request) {
        Category existing = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateCategoryException("Já existe uma categoria com o nome: " + request.getName());
        }

        // Copy non-id properties from request to existing entity
        BeanUtils.copyProperties(request, existing, "id");
        Category updated = categoryRepository.save(existing);
        return categoryMapper.toDTO(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
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