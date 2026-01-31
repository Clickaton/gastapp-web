package com.gastapp.controller.web.dto;

import com.gastapp.model.Category;
import com.gastapp.model.Expense;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ExpenseFormDto {

    private UUID id;
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;
    private String descripcion = "";
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha = LocalDate.now();
    @NotNull(message = "Elegí una categoría")
    private UUID categoryId;

    public static ExpenseFormDto from(Expense e) {
        ExpenseFormDto dto = new ExpenseFormDto();
        dto.setId(e.getId());
        dto.setMonto(e.getMonto());
        dto.setDescripcion(e.getDescripcion() != null ? e.getDescripcion() : "");
        dto.setFecha(e.getFecha());
        dto.setCategoryId(e.getCategory().getId());
        return dto;
    }

    public Expense toExpense(Category category) {
        return Expense.builder()
            .id(id != null ? id : UUID.randomUUID())
            .monto(monto)
            .descripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null)
            .fecha(fecha)
            .category(category)
            .user(category.getUser())
            .build();
    }
}
