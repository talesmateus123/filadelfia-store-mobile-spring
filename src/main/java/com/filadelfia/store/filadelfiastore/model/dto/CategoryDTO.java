package com.filadelfia.store.filadelfiastore.model.dto;

import java.sql.Date;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CategoryDTO {
    private Long id;
    private Date createdAt;
    private Date updatedAt;
    @NotNull
    private String name;
    @NotNull
    private String description;
    private Boolean active = true;

}
