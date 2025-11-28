package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;
import com.filadelfia.store.filadelfiastore.service.interfaces.ProductService;

import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/products")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ProductsWebController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final String activePage = "products";

    public ProductsWebController(ProductService productService, CategoryService categoryService) {
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
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);
        // model.addAttribute("categories", categoryService.getAllCategories());

        model.addAttribute("activePage", activePage);
        return "pages/product/products";
    }


    @GetMapping("/create")
    public String createProduct(Model model) {        
        model.addAttribute("pageTitle", "Novo Produto");          
        model.addAttribute("productDTO", new ProductDTO()); // Objeto para o formulário
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", false);
        
        model.addAttribute("activePage", activePage);
        return "pages/product/create_product";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Novo Produto");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", false);
            model.addAttribute("activePage", activePage);
            return "pages/product/create_product";
        }
        
        try {
            // Create product first
            ProductDTO createdProduct = productService.createProduct(productDTO);
            
            // Upload image if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    productService.updateProductImage(createdProduct.getId(), imageFile);
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Produto criado com sucesso e imagem enviada!");
                } catch (Exception imageError) {
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Produto criado com sucesso, mas houve erro no envio da imagem: " + imageError.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Produto criado com sucesso!");
            }
            
            return "redirect:/products/" + createdProduct.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao criar produto: " + e.getMessage());
            model.addAttribute("pageTitle", "Novo Produto");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", false);
            model.addAttribute("activePage", activePage);
            return "pages/product/create_product";
        }
    }

    
    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Optional<ProductDTO> productOpt = productService.getProductById(id);
        
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }
        
        model.addAttribute("pageTitle", "Editar Produto");
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("productDTO", productOpt.get());
        model.addAttribute("isEdit", true);        
        model.addAttribute("activePage", activePage);
        
        return "pages/product/create_product";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Editar Produto");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", activePage);
            return "pages/product/create_product";
        }
        
        try {
            // Update product first
            productService.updateProduct(id, productDTO);
            
            // Upload new image if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    productService.updateProductImage(id, imageFile);
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Produto atualizado com sucesso e nova imagem enviada!");
                } catch (Exception imageError) {
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Produto atualizado com sucesso, mas houve erro no envio da imagem: " + imageError.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Produto atualizado com sucesso!");
            }
            
            return "redirect:/products/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao atualizar produto: " + e.getMessage());
            model.addAttribute("pageTitle", "Editar Produto");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", activePage);
            return "pages/product/create_product";
        }
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Optional<ProductDTO> product = productService.getProductById(id);
        
        if (product.isEmpty()) {
            return "redirect:/products";
        }

        model.addAttribute("pageTitle", product.get().getName());
        model.addAttribute("breadcrumb", "Detalhes do Produto");
        model.addAttribute("product", product.get());
        
        model.addAttribute("activePage", activePage);
        return "pages/product/product_details";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produto desabilitado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao desabilitar produto: " + e.getMessage());
        }
        
        return "redirect:/products";
    }

    @PostMapping("/activate/{id}")
    public String activateProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            productService.activateProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produto ativado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao ativar produto: " + e.getMessage());
        }
        
        return "redirect:/products";
    }

    // Image management endpoints
    @PostMapping("/{id}/upload-image")
    public String uploadProductImage(
            @PathVariable Long id,
            @RequestParam("imageFile") org.springframework.web.multipart.MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (imageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Por favor, selecione uma imagem para enviar.");
                return "redirect:/products/" + id;
            }

            ProductDTO updatedProduct = productService.updateProductImage(id, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Imagem do produto '" + updatedProduct.getName() + "' atualizada com sucesso!");
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao fazer upload da imagem: " + e.getMessage());
        }
        
        return "redirect:/products/" + id;
    }

    @PostMapping("/{id}/delete-image")
    public String deleteProductImage(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            boolean deleted = productService.deleteProductImage(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Imagem do produto removida com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Nenhuma imagem encontrada para remover.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao remover imagem: " + e.getMessage());
        }
        
        return "redirect:/products/" + id;
    }

    // Featured products management endpoints
    @GetMapping("/featured")
    public String listFeaturedProducts(Model model) {
        List<ProductDTO> featuredProducts = productService.getAllFeaturedProducts();
        
        model.addAttribute("pageTitle", "Produtos em Destaque");
        model.addAttribute("products", featuredProducts);
        model.addAttribute("showFeaturedOnly", true);
        model.addAttribute("activePage", activePage);
        
        return "pages/product/products";
    }

    @PostMapping("/set-featured/{id}")
    public String toggleProductFeatured(
            @PathVariable Long id,
            @RequestParam("featured") Boolean featured,
            RedirectAttributes redirectAttributes) {
        
        try {
            ProductDTO updatedProduct = productService.setProductFeatured(id, featured);
            
            String message = featured ? 
                "Produto '" + updatedProduct.getName() + "' marcado como destaque!" :
                "Produto '" + updatedProduct.getName() + "' removido dos destaques!";
            
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao modificar destaque do produto: " + e.getMessage());
        }
        
        return "redirect:/products/" + id;
    }
}