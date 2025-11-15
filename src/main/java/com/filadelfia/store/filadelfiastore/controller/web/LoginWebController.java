package com.filadelfia.store.filadelfiastore.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginWebController {

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        
        model.addAttribute("pageTitle", "Login - Filadelfia Store");
        
        if (error != null) {
            String errorMessage = message != null ? message : "Email ou senha inválidos.";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("hasError", true);
        }
        
        if (logout != null) {
            model.addAttribute("logoutSuccess", true);
            model.addAttribute("successMessage", "Logout realizado com sucesso!");
        }
        
        return "pages/login/login";
    }
}