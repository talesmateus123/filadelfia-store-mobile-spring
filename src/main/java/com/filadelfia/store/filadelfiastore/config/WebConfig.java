package com.filadelfia.store.filadelfiastore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserRoleConverter userRoleConverter;
    private final UserRoleToStringConverter userRoleToStringConverter;
    private final String uploadDir;

    public WebConfig(UserRoleConverter userRoleConverter,
                     UserRoleToStringConverter userRoleToStringConverter,
                     @Value("${file.upload.dir:src/main/resources/static/images/products}") String uploadDir) {
        this.userRoleConverter = userRoleConverter;
        this.userRoleToStringConverter = userRoleToStringConverter;
        this.uploadDir = uploadDir;
    }

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        if (userRoleConverter != null) {
            registry.addConverter(userRoleConverter);
        }
        if (userRoleToStringConverter != null) {
            registry.addConverter(userRoleToStringConverter);
        }
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }
}