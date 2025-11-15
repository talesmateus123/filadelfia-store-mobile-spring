package com.filadelfia.store.filadelfiastore.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        
        String errorMessage = "Erro ao fazer login. Tente novamente.";
        
        if (exception instanceof BadCredentialsException) {
            errorMessage = "Email ou senha inválidos.";
        } else if (exception instanceof LockedException) {
            errorMessage = "Sua conta está bloqueada. Entre em contato com o administrador.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Sua conta está desativada. Entre em contato com o administrador.";
        }
        
        // Encode error message for URL
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        
        // Redirect to login with error parameter
        setDefaultFailureUrl("/login?error=true&message=" + encodedMessage);
        
        super.onAuthenticationFailure(request, response, exception);
    }
}