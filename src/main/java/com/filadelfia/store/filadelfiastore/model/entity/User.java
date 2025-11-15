package com.filadelfia.store.filadelfiastore.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

import com.filadelfia.store.filadelfiastore.model.enums.UserRole;

@Entity
@Table(name = "users")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Date createdAt = new Date(System.currentTimeMillis());

    @Column(nullable = true)
    private Date updatedAt = new Date(System.currentTimeMillis());

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private UserRole role;

    @Column(nullable = true)
    private String phone;

    private Boolean active = true;
   
}
