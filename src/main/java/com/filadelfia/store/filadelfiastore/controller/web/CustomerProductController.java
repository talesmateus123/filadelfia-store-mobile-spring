package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
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
@RequestMapping("/shop")
public class CustomerProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public CustomerProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String shopHome(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        model.addAttribute("pageTitle", "Filadelfia Store - Produtos");
        model.addAttribute("activePage", "shop");

        // Get all categories for filter
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // Get products based on filters
        List<ProductDTO> products;
        
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search.trim());
            model.addAttribute("searchTerm", search);
            model.addAttribute("pageTitle", "Pesquisa: " + search);
        } else if (category != null && !category.trim().isEmpty()) {
            products = productService.getProductsByCategory(category);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("pageTitle", "Categoria: " + category);
        } else {
            products = productService.getAllActiveProducts();
        }

        // Filter only active products for customers
        products = products.stream()
                .filter(ProductDTO::getActive)
                .toList();

        model.addAttribute("products", products);
        model.addAttribute("totalProducts", products.size());

        return "pages/customer/shop";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Optional<ProductDTO> productOpt = productService.getProductById(id);
        
        if (productOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Produto não encontrado.");
            return "error";
        }

        ProductDTO product = productOpt.get();
        
        // Only show active products to customers
        if (!product.getActive()) {
            model.addAttribute("errorMessage", "Produto não disponível.");
            return "error";
        }

        model.addAttribute("product", product);
        model.addAttribute("pageTitle", product.getName());
        model.addAttribute("activePage", "shop");

        // Get related products from the same category
        List<ProductDTO> relatedProducts = productService.getProductsByCategory(product.getCategoryName())
                .stream()
                .filter(p -> !p.getId().equals(id) && p.getActive())
                .limit(4)
                .toList();
        
        model.addAttribute("relatedProducts", relatedProducts);

        return "pages/customer/product-detail";
    }

    @GetMapping("/category/{categoryName}")
    public String productsByCategory(@PathVariable String categoryName, Model model) {
        List<ProductDTO> products = productService.getProductsByCategory(categoryName)
                .stream()
                .filter(ProductDTO::getActive)
                .toList();

        // Get all categories for filter
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        
        model.addAttribute("products", products);
        model.addAttribute("selectedCategory", categoryName);
        model.addAttribute("pageTitle", "Categoria: " + categoryName);
        model.addAttribute("activePage", "shop");
        model.addAttribute("totalProducts", products.size());

        return "pages/customer/shop";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("q") String query, Model model) {
        List<ProductDTO> products = productService.searchProducts(query)
                .stream()
                .filter(ProductDTO::getActive)
                .toList();
        
        // Get all categories for filter
        List<CategoryDTO> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        model.addAttribute("products", products);
        model.addAttribute("searchTerm", query);
        model.addAttribute("pageTitle", "Pesquisa: " + query);
        model.addAttribute("activePage", "shop");
        model.addAttribute("totalProducts", products.size());

        return "pages/customer/shop";
    }
}
