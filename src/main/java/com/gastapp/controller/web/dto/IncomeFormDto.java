package com.gastapp.controller.web.dto;

import com.gastapp.model.Account;
import com.gastapp.model.Income;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class IncomeFormDto {

    private UUID id;
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;
    private String descripcion = "";
    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha = LocalDate.now();
    @NotNull(message = "Elegí una cuenta")
    private UUID accountId;

    public static IncomeFormDto from(Income i) {
        IncomeFormDto dto = new IncomeFormDto();
        dto.setId(i.getId());
        dto.setMonto(i.getMonto());
        dto.setDescripcion(i.getDescripcion() != null ? i.getDescripcion() : "");
        dto.setFecha(i.getFecha());
        dto.setAccountId(i.getAccount().getId());
        return dto;
    }

    public Income toIncome(Account account) {
        return Income.builder()
            .id(id != null ? id : UUID.randomUUID())
            .monto(monto)
            .descripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null)
            .fecha(fecha)
            .account(account)
            .user(account.getUser())
            .build();
    }
}
