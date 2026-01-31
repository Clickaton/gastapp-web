package com.gastapp.controller.web;

import com.gastapp.controller.web.dto.RegisterForm;
import com.gastapp.model.User;
import com.gastapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            result.rejectValue("email", "email.duplicate", "Ya existe un usuario con ese email.");
            return "auth/register";
        }
        User user = User.builder()
            .id(UUID.randomUUID())
            .email(form.getEmail().trim().toLowerCase())
            .passwordHash(passwordEncoder.encode(form.getPassword()))
            .nombre(form.getNombre().trim())
            .build();
        userRepository.save(user);
        redirect.addFlashAttribute("message", "Cuenta creada. Iniciá sesión.");
        return "redirect:/login";
    }

}
