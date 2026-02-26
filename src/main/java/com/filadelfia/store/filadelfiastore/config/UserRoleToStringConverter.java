package com.filadelfia.store.filadelfiastore.config;

import com.filadelfia.store.filadelfiastore.model.enums.UserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Converter to handle UserRole enum to String conversion for forms
 */
@Component
public class UserRoleToStringConverter implements Converter<UserRole, String> {

    @Override
    public String convert(@NonNull UserRole source) {
        return source.getRoleName();
    }
}