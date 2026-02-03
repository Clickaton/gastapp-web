package com.gastapp.controller.web;

import com.gastapp.controller.web.dto.ProfileFormDto;
import com.gastapp.model.User;
import com.gastapp.repository.UserRepository;
import com.gastapp.service.CurrentUserService;
import com.gastapp.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileWebController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String profile(Model model) {
        User user = currentUserService.getCurrentUserOrThrow();
        model.addAttribute("profile", ProfileFormDto.from(user));
        return "profile/form";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profile") ProfileFormDto form,
                                BindingResult result,
                                RedirectAttributes redirect) {
        User user = currentUserService.getCurrentUserOrThrow();

        if (result.hasErrors()) {
            return "profile/form";
        }

        // Check email uniqueness if changed
        if (!user.getEmail().equalsIgnoreCase(form.getEmail()) && userRepository.existsByEmail(form.getEmail())) {
            result.rejectValue("email", "email.duplicate", "Ya existe un usuario con ese email.");
            return "profile/form";
        }

        // Handle Password Change
        if (form.getNewPassword() != null && !form.getNewPassword().isBlank()) {
            if (form.getNewPassword().length() < 6) {
                 result.rejectValue("newPassword", "password.length", "Mínimo 6 caracteres");
                 return "profile/form";
            }
            if (form.getCurrentPassword() == null || form.getCurrentPassword().isBlank()) {
                result.rejectValue("currentPassword", "password.required", "Ingresá tu contraseña actual para cambiarla.");
                return "profile/form";
            }
            if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
                result.rejectValue("currentPassword", "password.invalid", "Contraseña actual incorrecta.");
                return "profile/form";
            }
            user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        }

        // Handle Photo
        if (form.getFoto() != null && !form.getFoto().isEmpty()) {
            try {
                String filename = fileStorageService.store(form.getFoto());
                user.setFotoPerfil(filename);
            } catch (Exception e) {
                result.rejectValue("foto", "foto.error", "Error al subir la imagen.");
                return "profile/form";
            }
        }

        user.setNombre(form.getNombre());
        user.setEmail(form.getEmail());

        userRepository.save(user);

        redirect.addFlashAttribute("message", "Perfil actualizado correctamente.");
        return "redirect:/profile";
    }
}
