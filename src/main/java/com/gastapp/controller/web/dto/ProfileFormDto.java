package com.gastapp.controller.web.dto;

import com.gastapp.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileFormDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    private String currentPassword;

    // Remove validation annotation to allow empty value (optional)
    // We will validate manually in Controller if it's not empty
    private String newPassword;

    private MultipartFile foto;

    public static ProfileFormDto from(User user) {
        ProfileFormDto dto = new ProfileFormDto();
        dto.setNombre(user.getNombre());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
