package com.gastapp.controller.web.dto;

import com.gastapp.model.Account;
import com.gastapp.model.Category;
import com.gastapp.model.Expense;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha = LocalDate.now();

    @NotNull(message = "Elegí una categoría")
    private UUID categoryId;
    @NotNull(message = "Elegí una cuenta")
    private UUID accountId;

    private boolean esCuotas = false;
    private Integer totalCuotas = 1;

    public static ExpenseFormDto from(Expense e) {
        ExpenseFormDto dto = new ExpenseFormDto();
        dto.setId(e.getId());
        dto.setMonto(e.getMonto());
        dto.setDescripcion(e.getDescripcion() != null ? e.getDescripcion() : "");
        dto.setFecha(e.getFecha());
        dto.setCategoryId(e.getCategory().getId());
        dto.setAccountId(e.getAccount().getId());
        dto.setEsCuotas(e.isEsCuotas());
        dto.setTotalCuotas(e.getTotalCuotas());
        return dto;
    }

    public Expense toExpense(Category category, Account account) {
        return Expense.builder()
            .id(id != null ? id : UUID.randomUUID())
            .monto(monto)
            .descripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null)
            .fecha(fecha)
            .category(category)
            .account(account)
            .user(category.getUser())
            .esCuotas(esCuotas)
            .totalCuotas(totalCuotas != null ? totalCuotas : 1)
            .cuotaActual(1)
            .build();
    }
}
