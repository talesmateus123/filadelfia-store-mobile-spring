package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.AddressDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.AddressService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/addresses")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class AddressWebController {
    
    private final AddressService addressService;
    
    public AddressWebController(AddressService addressService) {
        this.addressService = addressService;
    }
    
    @GetMapping
    public String listAddresses(Model model) {
        Long userId = getCurrentUserId();
        List<AddressDTO> addresses = addressService.getUserAddresses(userId);
        
        model.addAttribute("addresses", addresses);
        model.addAttribute("activePage", "profile");
        model.addAttribute("pageTitle", "Meus Endereços");
        
        return "pages/user/addresses";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("address", new AddressDTO());
        model.addAttribute("activePage", "profile");
        model.addAttribute("pageTitle", "Novo Endereço");
        model.addAttribute("isEdit", false);
        
        return "pages/user/address_form";
    }
    
    @PostMapping("/create")
    public String createAddress(@Valid @ModelAttribute("address") AddressDTO addressDTO,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "profile");
            model.addAttribute("pageTitle", "Novo Endereço");
            model.addAttribute("isEdit", false);
            return "pages/user/address_form";
        }
        
        try {
            Long userId = getCurrentUserId();
            addressDTO = addressService.formatAddress(addressDTO);
            addressService.createAddress(userId, addressDTO);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Endereço criado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao criar endereço: " + e.getMessage());
        }
        
        return "redirect:/addresses";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            AddressDTO address = addressService.getAddressById(id);
            
            // Verify ownership
            if (!address.getUserId().equals(getCurrentUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Acesso negado!");
                return "redirect:/addresses";
            }
            
            model.addAttribute("address", address);
            model.addAttribute("activePage", "profile");
            model.addAttribute("pageTitle", "Editar Endereço");
            model.addAttribute("isEdit", true);
            
            return "pages/user/address_form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao carregar endereço: " + e.getMessage());
            return "redirect:/addresses";
        }
    }
    
    @PostMapping("/{id}/update")
    public String updateAddress(@PathVariable Long id,
                               @Valid @ModelAttribute("address") AddressDTO addressDTO,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("activePage", "profile");
            model.addAttribute("pageTitle", "Editar Endereço");
            model.addAttribute("isEdit", true);
            return "pages/user/address_form";
        }
        
        try {
            Long userId = getCurrentUserId();
            addressDTO = addressService.formatAddress(addressDTO);
            addressService.updateAddress(userId, id, addressDTO);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Endereço atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao atualizar endereço: " + e.getMessage());
        }
        
        return "redirect:/addresses";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId();
            addressService.deleteAddress(userId, id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Endereço excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao excluir endereço: " + e.getMessage());
        }
        
        return "redirect:/addresses";
    }
    
    @PostMapping("/{id}/set-default")
    public String setDefaultAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId();
            addressService.setDefaultAddress(userId, id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Endereço padrão definido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao definir endereço padrão: " + e.getMessage());
        }
        
        return "redirect:/addresses";
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.filadelfia.store.filadelfiastore.config.CustomUserDetailsService.CustomUserDetails) {
            com.filadelfia.store.filadelfiastore.config.CustomUserDetailsService.CustomUserDetails userDetails = 
                (com.filadelfia.store.filadelfiastore.config.CustomUserDetailsService.CustomUserDetails) auth.getPrincipal();
            return userDetails.getUser().getId();
        }
        throw new RuntimeException("User not authenticated");
    }
}