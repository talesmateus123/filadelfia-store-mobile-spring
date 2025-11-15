package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

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

    @PostMapping("/create")
    public String createCategory(
            @Valid @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Nova Categoria");
            model.addAttribute("activePage", activePage);
            return "pages/category/create_category";
        }
        
        try {
            CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Categoria criada com sucesso!");
            return "redirect:/categories/" + createdCategory.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao criar categoria: " + e.getMessage());
            model.addAttribute("pageTitle", "Nova Categoria");
            model.addAttribute("activePage", activePage);
            return "pages/category/create_category";
        }
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Optional<CategoryDTO> categoryOpt = categoryService.getCategoryById(id);
        
        if (categoryOpt.isEmpty() || !categoryOpt.get().getActive()) {
            return "redirect:/categories";
        }
        
        model.addAttribute("pageTitle", "Editar Categoria");
        model.addAttribute("categoryDTO", categoryOpt.get());
        model.addAttribute("isEdit", true);        
        model.addAttribute("activePage", activePage);
        
        return "pages/category/create_category";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Editar Categoria");
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", activePage);
            return "pages/category/create_category";
        }
        
        try {
            categoryService.updateCategory(id, categoryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Categoria atualizada com sucesso!");
            return "redirect:/categories/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao atualizar categoria: " + e.getMessage());
            model.addAttribute("pageTitle", "Editar Categoria");
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", activePage);
            return "pages/category/create_category";
        }
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

    @PostMapping("/delete/{id}")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Categoria removida com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao remover categoria: " + e.getMessage());
        }
        
        return "redirect:/categories";
    }

}