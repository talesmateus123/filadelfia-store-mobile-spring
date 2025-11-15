package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.dto.UserNewDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

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
        model.addAttribute("userNewDTO", new UserNewDTO()); // Objeto para o formulário

        model.addAttribute("activePage", activePage);
        return "pages/user/create_user";
    }

    @PostMapping("/create")
    public String createUser(
            @Valid @ModelAttribute("userNewDTO") UserNewDTO userNewDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // For create operation, password is required
        if (userNewDTO.getPassword() == null || userNewDTO.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "NotEmpty", "Senha não pode estar vazia");
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Novo Usuário");
            model.addAttribute("activePage", activePage);
            return "pages/user/create_user";
        }
        
        try {
            UserDTO createdUser = userService.createUser(userNewDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário criado com sucesso!");
            return "redirect:/users/" + createdUser.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao criar usuário: " + e.getMessage());
            model.addAttribute("pageTitle", "Novo Usuário");
            model.addAttribute("activePage", activePage);
            return "pages/user/create_user";
        }
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        Optional<UserDTO> userOpt = userService.getUserById(id);
        
        if (userOpt.isEmpty() || !userOpt.get().getActive()) {
            return "redirect:/users";
        }
        
        UserDTO user = userOpt.get();
        
        // Convert UserDTO to UserNewDTO for editing
        UserNewDTO userNewDTO = new UserNewDTO();
        userNewDTO.setId(user.getId());
        userNewDTO.setName(user.getName());
        userNewDTO.setEmail(user.getEmail());
        userNewDTO.setRole(user.getRole()); // Role should be properly set here
        userNewDTO.setPhone(user.getPhone());
        userNewDTO.setActive(user.getActive());
        // Note: password is intentionally left null for security (optional in updates)
        
        model.addAttribute("pageTitle", "Editar Usuário");
        model.addAttribute("userNewDTO", userNewDTO);
        model.addAttribute("isEdit", true);        
        model.addAttribute("activePage", activePage);
        
        return "pages/user/create_user";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("userNewDTO") UserNewDTO userNewDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // For update operation, validate password only if provided and not empty
        String password = userNewDTO.getPassword();
        if (password != null && !password.trim().isEmpty() && password.trim().length() < 8) {
            bindingResult.rejectValue("password", "Size", "Senha deve ter no mínimo 8 caracteres");
        }
        // Clear password if it's empty to avoid validation issues
        if (password != null && password.trim().isEmpty()) {
            userNewDTO.setPassword(null);
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Editar Usuário");
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", activePage);
            return "pages/user/create_user";
        }
        
        try {
            userService.updateUser(id, userNewDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário atualizado com sucesso!");
            return "redirect:/users/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao atualizar usuário: " + e.getMessage());
            model.addAttribute("pageTitle", "Editar Usuário");
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", activePage);
            return "pages/user/create_user";
        }
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

    @PostMapping("/delete/{id}")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário removido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao remover usuário: " + e.getMessage());
        }
        
        return "redirect:/users";
    }

}