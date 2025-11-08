package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;
import com.filadelfia.store.filadelfiastore.service.interfaces.ProductService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductWebController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductWebController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        model.addAttribute("pageTitle", "Gerenciamento de Produtos");
        // model.addAttribute("breadcrumb", "Produtos");

        List<ProductDTO> products;
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search);
            model.addAttribute("searchTerm", search);
        } else if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            products = productService.getAllActiveProducts();
        }

        model.addAttribute("products", products);
        // model.addAttribute("categories", categoryService.getAllCategories());

        model.addAttribute("activePage", "products");
        return "pages/product/products";
    }


    @GetMapping("/create")
    public String createProduct(Model model) {
        
        model.addAttribute("pageTitle", "Adicionar Produto");
        // model.addAttribute("breadcrumb", "Produtos");

        // model.addAttribute("categories", categoryService.getAllCategories());

        model.addAttribute("activePage", "products");
        return "pages/product/create_product";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Optional<ProductDTO> product = productService.getProductById(id);
        
        if (product.isEmpty() || !product.get().getActive()) {
            return "redirect:/products";
        }

        model.addAttribute("pageTitle", product.get().getName());
        model.addAttribute("breadcrumb", "Detalhes do Produto");
        model.addAttribute("product", product.get());

        return "products/detail";
    }
}