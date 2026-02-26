package com.filadelfia.store.filadelfiastore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserRoleConverter userRoleConverter;
    private final UserRoleToStringConverter userRoleToStringConverter;

    public WebConfig(UserRoleConverter userRoleConverter, UserRoleToStringConverter userRoleToStringConverter) {
        this.userRoleConverter = userRoleConverter;
        this.userRoleToStringConverter = userRoleToStringConverter;
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
}