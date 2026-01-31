package com.gastapp.controller.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255)
    private String nombre = "";

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email = "";

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "Mínimo 6 caracteres")
    private String password = "";
}
