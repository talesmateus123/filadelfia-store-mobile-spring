package com.filadelfia.store.filadelfiastore.controller.web;

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
        // Adicionar atributos à sessão
        String username = (String) session.getAttribute("username");
        if (username == null) {
            // Recuperar do contexto de segurança
            username = "Usuário";
            session.setAttribute("username", username);
        }
        
        model.addAttribute("username", username);
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