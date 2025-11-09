package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;

import jakarta.validation.Valid;

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
    public String createCategory(Model model) {
        
        model.addAttribute("pageTitle", "Nova Categoria");        
        model.addAttribute("categoryDTO", new CategoryDTO()); // Objeto para o formulário

        model.addAttribute("activePage", activePage);
        return "pages/category/create_category";
    }

    @PostMapping("/create")
    public String createCategory(
            @ModelAttribute("categoryDTO") @Valid CategoryDTO categoryDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Verifica se há erros de validação
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Nova Categoria");
            model.addAttribute("activePage", activePage);
        }

        try {
            // Cria a categoria
            CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
            
            // Adiciona mensagem de sucesso
            redirectAttributes.addFlashAttribute("success", 
                "Categoria '" + createdCategory.getName() + "' criada com sucesso!");

                
            model.addAttribute("success", "Sucesso!");
            model.addAttribute("successDescription", "Categoria '" + createdCategory.getName() + "' criada com sucesso!");
            
        } catch (Exception e) {
            // Trata erros (como categoria duplicada, etc)
            model.addAttribute("pageTitle", "Adicionar Categoria");
            model.addAttribute("error", "Erro ao criar categoria: ");
            model.addAttribute("errorDescription", e.getMessage());
            model.addAttribute("activePage", activePage);
        }
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

    @PostMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addAttribute("success", 
                "Categoria excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", 
                "Erro ao excluir categoria: " + e.getMessage());
        }
        
        return "pages/category/create_category";
    }
}