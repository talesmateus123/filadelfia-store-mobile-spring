package com.filadelfia.store.filadelfiastore.config;

import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.enums.UserRole;
import com.filadelfia.store.filadelfiastore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Verifica se já existe um usuário admin
        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            createDefaultAdmin();
        }
    }

    private void createDefaultAdmin() {
        User admin = new User();
        admin.setName("Administrador");
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("12345678"));
        admin.setRole(UserRole.ADMIN);
        admin.setPhone("(11) 99999-9999");
        admin.setActive(true);

        userRepository.save(admin);
        
        System.out.println("Usuário administrador criado com sucesso!");
        System.out.println("Email: admin@filadelfia.com");
        System.out.println("Senha: admin123");
        System.out.println("IMPORTANTE: Altere a senha após o primeiro login!");
    }
}
