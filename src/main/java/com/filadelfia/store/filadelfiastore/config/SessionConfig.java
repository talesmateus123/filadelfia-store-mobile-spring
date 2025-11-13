package com.filadelfia.store.filadelfiastore.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionTrackingMode;
import java.util.Collections;

@Configuration
public class SessionConfig implements WebMvcConfigurer {

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) {
                servletContext.setSessionTrackingModes(
                    Collections.singleton(SessionTrackingMode.COOKIE)
                );
            }
        };
    }
}