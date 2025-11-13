package com.filadelfia.store.filadelfiastore.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

import com.filadelfia.store.filadelfiastore.model.enums.UserRole;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UserDTO {
    private Long id;
    private Date createdAt;
    private Date updatedAt;
    @NotNull
    private String name;
    @NotNull
    private String email;
    private String password;    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String phone;
    private Boolean active = true;
}
