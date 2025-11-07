package com.filadelfia.store.filadelfiastore.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO request);
    Optional<CategoryDTO> getCategoryById(Long id);
    List<CategoryDTO> getAllCategories();
    CategoryDTO updateCategory(Long id, CategoryDTO request);
    void deleteCategory(Long id);

    List<CategoryDTO> searchCategories(String searchTerm);
    List<CategoryDTO> getAllActiveCategories();
}