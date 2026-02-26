package com.filadelfia.store.filadelfiastore.config;

import com.filadelfia.store.filadelfiastore.model.enums.UserRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Converter to handle UserRole enum conversion from form values with ROLE_ prefix
 */
@Component
public class UserRoleConverter implements Converter<String, UserRole> {

    @Override
    public UserRole convert(@NonNull String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        return UserRole.fromRoleName(source.trim());
    }
}