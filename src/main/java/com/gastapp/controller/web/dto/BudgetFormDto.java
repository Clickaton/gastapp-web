package com.gastapp.controller.web.dto;

import com.gastapp.model.Budget;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BudgetFormDto {

    private UUID id;
    @NotNull(message = "La categoría es obligatoria")
    private UUID categoryId;
    @NotNull(message = "El mes es obligatorio")
    private Integer mes;
    @NotNull(message = "El año es obligatorio")
    private Integer anio;
    @NotNull(message = "El monto máximo es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal montoMaximo;

    public static BudgetFormDto from(Budget b) {
        BudgetFormDto dto = new BudgetFormDto();
        dto.setId(b.getId());
        dto.setCategoryId(b.getCategory().getId());
        dto.setMes(b.getMes());
        dto.setAnio(b.getAnio());
        dto.setMontoMaximo(b.getMontoMaximo());
        return dto;
    }
}
