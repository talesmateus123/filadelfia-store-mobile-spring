package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginWebController {
    private final UserService userService;

    public LoginWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public String login(Model model) {        
        model.addAttribute("pageTitle", "Login");
        return "pages/login/login";
    }

}