package com.filadelfia.store.filadelfiastore.model.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
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

public class UserNewDTO {
    private Long id;
    private Date createdAt;
    private Date updatedAt;
    @NotEmpty(message = "Nome não pode estar vazio")
    private String name;
    @NotEmpty(message = "Email não pode estar vazio")
    private String email;
    @NotEmpty(message = "Senha não pode estar vazia")
    @Min(value = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String phone;
    private Boolean active = true;
}
