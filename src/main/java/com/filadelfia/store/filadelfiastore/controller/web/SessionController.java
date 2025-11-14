package com.filadelfia.store.filadelfiastore.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class SessionController {

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Recuperar do contexto de segurança
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "Usuário";
        
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser")) {
            username = authentication.getName();
            session.setAttribute("username", username);
        } else {
            session.setAttribute("username", username);
        }
        
        model.addAttribute("username", username);
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @PostMapping("/session/session/set-theme")
    public String setTheme(@RequestParam String theme, HttpSession session) {
        session.setAttribute("theme", theme);
        return "redirect:/dashboard";
    }

    // @GetMapping("/session/session/info")
    // public String sessionInfo(HttpSession session, Model model) {
    //     model.addAttribute("sessionId", session.getId());
    //     model.addAttribute("creationTime", session.getCreationTime());
    //     model.addAttribute("lastAccessedTime", session.getLastAccessedTime());
    //     model.addAttribute("maxInactiveInterval", session.getMaxInactiveInterval());
        
    //     return "session-info";
    // }
}