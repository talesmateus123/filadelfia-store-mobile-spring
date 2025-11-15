package com.filadelfia.store.filadelfiastore.service;

import com.filadelfia.store.filadelfiastore.model.entity.PasswordResetToken;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.repository.PasswordResetTokenRepository;
import com.filadelfia.store.filadelfiastore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.getUserByEmail(email);
        
        if (userOptional.isEmpty()) {
            // Don't reveal if email exists or not for security
            return null;
        }
        
        User user = userOptional.get();
        
        // Generate unique token
        String token = UUID.randomUUID().toString();
        
        // Create and save token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        
        tokenRepository.save(resetToken);
        
        return token;
    }

    @Transactional
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        
        if (resetToken.isEmpty()) {
            return false;
        }
        
        PasswordResetToken passwordResetToken = resetToken.get();
        
        return !passwordResetToken.isExpired() && !passwordResetToken.getUsed();
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOptional = tokenRepository.findByToken(token);
        
        if (resetTokenOptional.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = resetTokenOptional.get();
        
        if (resetToken.isExpired() || resetToken.getUsed()) {
            return false;
        }
        
        // Update user password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        return true;
    }

    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}