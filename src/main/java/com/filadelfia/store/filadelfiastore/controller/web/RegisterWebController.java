package com.filadelfia.store.filadelfiastore.controller.web;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.filadelfia.store.filadelfiastore.model.dto.RegisterDTO;
import com.filadelfia.store.filadelfiastore.model.dto.UserNewDTO;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.enums.UserRole;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

import jakarta.validation.Valid;

@Controller
public class RegisterWebController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RegisterWebController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("pageTitle", "Cadastro - Filadelfia Store");
        model.addAttribute("registerDTO", new RegisterDTO());
        return "pages/login/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Cadastro - Filadelfia Store");
            model.addAttribute("errorMessage", "Por favor, corrija os erros no formulário.");
            return "pages/login/register";
        }

        // Check if passwords match
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            model.addAttribute("pageTitle", "Cadastro - Filadelfia Store");
            model.addAttribute("errorMessage", "As senhas não coincidem.");
            return "pages/login/register";
        }

        try {
            // Check if email already exists
            if (userService.existsByEmail(registerDTO.getEmail())) {
                model.addAttribute("pageTitle", "Cadastro - Filadelfia Store");
                model.addAttribute("errorMessage", "Este email já está cadastrado.");
                return "pages/login/register";
            }

            // Create new user
            UserNewDTO newUser = new UserNewDTO();
            newUser.setName(registerDTO.getName());
            newUser.setEmail(registerDTO.getEmail());
            newUser.setPassword(registerDTO.getPassword());
            newUser.setPhone(registerDTO.getPhone());
            newUser.setRole(UserRole.USER);
            newUser.setActive(true);

            userService.createUser(newUser);

            redirectAttributes.addFlashAttribute("successMessage", 
                "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/login?registered=true";

        } catch (Exception e) {
            model.addAttribute("pageTitle", "Cadastro - Filadelfia Store");
            model.addAttribute("errorMessage", "Erro ao criar conta. Tente novamente.");
            return "pages/login/register";
        }
    }
}