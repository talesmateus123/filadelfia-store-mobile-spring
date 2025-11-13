package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.CategoryDTO;
import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.CategoryService;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UsersWebController {

    private final UserService userService;
    private final String activePage = "users";

    public UsersWebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        model.addAttribute("pageTitle", "Gerenciamento de Usuários");

        List<UserDTO> users;
        if (search != null && !search.isEmpty()) {
            users = userService.searchUsers(search);
            model.addAttribute("searchTerm", search);
        } else {
            users = userService.getAllActiveUsers();
        }

        model.addAttribute("users", users);
        model.addAttribute("activePage", activePage);
        return "pages/user/users";
    }

    @GetMapping("/create")
    public String createUserForm(Model model) {        
        model.addAttribute("pageTitle", "Novo Usuário");        
        model.addAttribute("userDTO", new UserDTO()); // Objeto para o formulário

        model.addAttribute("activePage", activePage);
        return "pages/user/create_user";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Editar Usuário");
        Optional<UserDTO> user = userService.getUserById(id);
        model.addAttribute("userDTO", user.orElseThrow(() -> new RuntimeException("Usuário não encontrado")));
        model.addAttribute("isEdit", true);        

        model.addAttribute("activePage", activePage);
        return "pages/user/create_user";
    }

    @GetMapping("/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        Optional<UserDTO> user = userService.getUserById(id);
        
        if (user.isEmpty() || !user.get().getActive()) {
            return "redirect:/users";
        }

        model.addAttribute("pageTitle", user.get().getName());
        model.addAttribute("user", user.get());
        model.addAttribute("activePage", activePage);
        return "pages/user/user_details";
    }

}