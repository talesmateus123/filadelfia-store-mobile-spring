package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.service.PasswordResetService;
import com.filadelfia.store.filadelfiastore.service.interfaces.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;
    private final EmailService emailService;

    public ForgotPasswordController(
            PasswordResetService passwordResetService,
            EmailService emailService) {
        this.passwordResetService = passwordResetService;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("pageTitle", "Esqueci minha senha - Filadelfia Store");
        return "pages/login/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        try {
            String token = passwordResetService.createPasswordResetToken(email);
            
            // Send email if token was created (user exists)
            if (token != null) {
                try {
                    emailService.sendPasswordResetEmail(email, token);
                } catch (Exception e) {
                    // Log error but don't reveal to user
                    System.err.println("Failed to send password reset email: " + e.getMessage());
                }
            }
            
            // Always show success message for security (don't reveal if email exists)
            redirectAttributes.addFlashAttribute("successMessage", 
                "Se o email existir em nossa base de dados, você receberá um link para redefinir sua senha. " +
                "Verifique sua caixa de entrada e spam.");
            
            return "redirect:/forgot-password?success=true";
            
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Esqueci minha senha - Filadelfia Store");
            model.addAttribute("errorMessage", "Erro ao processar solicitação. Tente novamente.");
            return "pages/login/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam("token") String token,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!passwordResetService.validatePasswordResetToken(token)) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Link de redefinição de senha inválido ou expirado.");
            return "redirect:/forgot-password";
        }
        
        model.addAttribute("pageTitle", "Redefinir senha - Filadelfia Store");
        model.addAttribute("token", token);
        return "pages/login/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("pageTitle", "Redefinir senha - Filadelfia Store");
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "As senhas não coincidem.");
            return "pages/login/reset-password";
        }
        
        if (password.length() < 6) {
            model.addAttribute("pageTitle", "Redefinir senha - Filadelfia Store");
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "A senha deve ter no mínimo 6 caracteres.");
            return "pages/login/reset-password";
        }
        
        boolean success = passwordResetService.resetPassword(token, password);
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", 
                "Senha redefinida com sucesso! Faça login com sua nova senha.");
            return "redirect:/login?reset=true";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Link de redefinição de senha inválido ou expirado.");
            return "redirect:/forgot-password";
        }
    }
}