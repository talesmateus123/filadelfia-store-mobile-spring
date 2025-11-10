package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/categories")
public class CategoriesWebController {

    private final CategoryService categoryService;
    private final String activePage = "categories";

    public CategoriesWebController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        model.addAttribute("pageTitle", "Gerenciamento de Categorias");

        List<CategoryDTO> categories;
        if (search != null && !search.isEmpty()) {
            categories = categoryService.searchCategories(search);
            model.addAttribute("searchTerm", search);
        } else {
            categories = categoryService.getAllActiveCategories();
        }

        model.addAttribute("categories", categories);
        // model.addAttribute("categories", categoryService.getAllCategories());

        model.addAttribute("activePage", activePage);
        return "pages/category/categories";
    }

    @GetMapping("/create")
    public String createCategoryForm(Model model) {        
        model.addAttribute("pageTitle", "Nova Categoria");        
        model.addAttribute("categoryDTO", new CategoryDTO()); // Objeto para o formulário

        model.addAttribute("activePage", activePage);
        return "pages/category/create_category";
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Optional<CategoryDTO> category = categoryService.getCategoryById(id);

        model.addAttribute("pageTitle", "Editar Categoria");
        model.addAttribute("categoryDTO", category.orElseThrow(() -> new RuntimeException("Categoria não encontrada")));
        model.addAttribute("isEdit", true);        

        model.addAttribute("activePage", activePage);
        return "pages/category/create_category";
    }

    @GetMapping("/{id}")
    public String categoryDetail(@PathVariable Long id, Model model) {
        Optional<CategoryDTO> category = categoryService.getCategoryById(id);
        
        if (category.isEmpty() || !category.get().getActive()) {
            return "redirect:/categories";
        }

        model.addAttribute("pageTitle", category.get().getName());
        model.addAttribute("category", category.get());

        model.addAttribute("activePage", activePage);
        return "pages/category/category_details";
    }

}