package com.filadelfia.store.filadelfiastore.service.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
import com.filadelfia.store.filadelfiastore.model.dto.CategoryDetailedDTO;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO request);
    Optional<CategoryDetailedDTO> getCategoryDetailedById(Long id);
    Optional<CategoryDTO> getCategoryById(Long id);
    List<CategoryDTO> getAllCategories();
    Page<CategoryDTO> getAllCategories(Pageable pageable);
    CategoryDTO updateCategory(Long id, CategoryDTO request);
    void deleteCategory(Long id);

    List<CategoryDTO> searchCategories(String searchTerm);
    List<CategoryDTO> getAllActiveCategories();
}