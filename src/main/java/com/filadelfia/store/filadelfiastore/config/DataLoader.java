package com.filadelfia.store.filadelfiastore.config;

import com.filadelfia.store.filadelfiastore.model.dto.UserNewDTO;
import com.filadelfia.store.filadelfiastore.model.enums.UserRole;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {

    private UserService userService;

    public DataLoader(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // Verifica se já existe um usuário admin
            if (userService.getUserByEmail("admin@admin.com").isEmpty()) {
                createDefaultAdmin();
            }
        } catch (Exception e) {
            System.err.println("Error in DataLoader - this might be due to discriminator column issues:");
            System.err.println("Please run the fix_discriminator.sql script manually in your database");
            System.err.println("SQL: UPDATE users SET user_type = 'USER' WHERE user_type IS NULL OR user_type = '';");
            throw e;
        }
    }

    private void createDefaultAdmin() {
        UserNewDTO admin = new UserNewDTO();
        admin.setName("Administrador");
        admin.setEmail("admin@admin.com");
        admin.setPassword("12345678");
        admin.setRole(UserRole.ADMIN);
        admin.setPhone("(11) 99999-9999");
        admin.setActive(true);

        userService.createUser(admin);

        System.out.println("Usuário administrador criado com sucesso!");
        System.out.println("Email: admin@filadelfia.com");
        System.out.println("Senha: 12345678");
        System.out.println("IMPORTANTE: Altere a senha após o primeiro login!");
    }
}
