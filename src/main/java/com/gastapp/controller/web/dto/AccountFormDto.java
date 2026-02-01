package com.gastapp.controller.web.dto;

import com.gastapp.model.Account;
import com.gastapp.model.AccountType;
import com.gastapp.model.User;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountFormDto {

    private UUID id;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre = "";
    @NotNull(message = "Elegí un tipo de cuenta")
    private AccountType tipo;
    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0", message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial = BigDecimal.ZERO;
    @Size(max = 50)
    private String icono = "";
    @Size(max = 20)
    private String color = "#6c757d";

    private boolean isShared = false;
    private String sharedUserEmail = "";

    public static AccountFormDto from(Account a) {
        AccountFormDto dto = new AccountFormDto();
        dto.setId(a.getId());
        dto.setNombre(a.getNombre());
        dto.setTipo(a.getTipo());
        dto.setSaldoInicial(a.getSaldoInicial());
        dto.setIcono(a.getIcono() != null ? a.getIcono() : "");
        String aColor = a.getColor();
        dto.setColor(aColor != null && !aColor.isBlank() ? (aColor.startsWith("#") ? aColor : "#" + aColor) : "#6c757d");
        dto.setShared(a.isShared());
        // For sharedUserEmail, if there are shared users, pick one (first).
        // Requirement says "allow selecting a user". Assuming one for simplicity or first one.
        if (!a.getSharedUsers().isEmpty()) {
            dto.setSharedUserEmail(a.getSharedUsers().iterator().next().getEmail());
        }
        return dto;
    }

    public Account toAccount(User user) {
        String colorValue = (color != null && !color.isBlank()) ? color.trim() : null;
        if (colorValue != null && !colorValue.startsWith("#")) {
            colorValue = "#" + colorValue;
        }
        return Account.builder()
            .id(id != null ? id : UUID.randomUUID())
            .nombre(nombre.trim())
            .tipo(tipo)
            .saldoInicial(saldoInicial != null ? saldoInicial : BigDecimal.ZERO)
            .icono(icono != null && !icono.isBlank() ? icono.trim() : null)
            .color(colorValue)
            .user(user)
            .isShared(isShared)
            .build();
    }
}
