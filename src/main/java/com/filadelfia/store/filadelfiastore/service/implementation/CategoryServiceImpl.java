package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Category;
import com.filadelfia.store.filadelfiastore.model.mapper.CategoryMapper;
import com.filadelfia.store.filadelfiastore.repository.CategoryRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService  {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
  
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
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
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .map(categoryMapper::toDTO);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO request) {        
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

}