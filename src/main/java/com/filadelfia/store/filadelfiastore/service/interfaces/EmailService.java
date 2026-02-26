package com.filadelfia.store.filadelfiastore.service.interfaces;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);
    void sendWelcomeEmail(String to, String name);
}