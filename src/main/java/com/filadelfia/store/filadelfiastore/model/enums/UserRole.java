package com.filadelfia.store.filadelfiastore.model.enums;

public enum UserRole {
    ADMIN(1, "ROLE_ADMIN"),
    MANAGER(2, "ROLE_MANAGER");
    
    private final int code;
    private final String description;

    private UserRole(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
    public static UserRole toEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserRole role : UserRole.values()) {
            if (code.equals(role.getCode())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + code);
    }
}