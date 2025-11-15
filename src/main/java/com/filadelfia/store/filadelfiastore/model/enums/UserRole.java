package com.filadelfia.store.filadelfiastore.model.enums;

public enum UserRole {
    USER,
    ADMIN,
    MANAGER;
    
    /**
     * Gets the role name with ROLE_ prefix for Spring Security
     */
    public String getRoleName() {
        return "ROLE_" + this.name();
    }
    
    /**
     * Converts from ROLE_ prefixed string to enum
     */
    public static UserRole fromRoleName(String roleName) {
        if (roleName == null) {
            return null;
        }
        String enumName = roleName.startsWith("ROLE_") ? roleName.substring(5) : roleName;
        try {
            return UserRole.valueOf(enumName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}