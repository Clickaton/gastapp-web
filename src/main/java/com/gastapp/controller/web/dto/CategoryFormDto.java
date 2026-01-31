package com.gastapp.controller.web.dto;

import com.gastapp.model.Category;
import com.gastapp.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryFormDto {

    private UUID id;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre = "";
    @Size(max = 50)
    private String icono = "";
    @Size(max = 20)
    private String color = "#6c757d";

    public static CategoryFormDto from(Category c) {
        CategoryFormDto dto = new CategoryFormDto();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setIcono(c.getIcono() != null ? c.getIcono() : "");
        String cColor = c.getColor();
        dto.setColor(cColor != null && !cColor.isBlank() ? (cColor.startsWith("#") ? cColor : "#" + cColor) : "#6c757d");
        return dto;
    }

    public Category toCategory(User user) {
        String colorValue = (color != null && !color.isBlank()) ? color.trim() : null;
        if (colorValue != null && !colorValue.startsWith("#")) {
            colorValue = "#" + colorValue;
        }
        return Category.builder()
            .id(id != null ? id : UUID.randomUUID())
            .nombre(nombre.trim())
            .icono(icono != null && !icono.isBlank() ? icono.trim() : null)
            .color(colorValue)
            .user(user)
            .build();
    }
}
