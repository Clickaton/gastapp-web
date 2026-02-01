package com.gastapp.config;

import com.gastapp.model.User;
import com.gastapp.repository.UserRepository;
import com.gastapp.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/**
 * Añade variables al modelo para todas las vistas Thymeleaf.
 * Evita usar #request en templates (no disponible por defecto en Thymeleaf 3.1+).
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ThymeleafControllerAdvice {

    private final CurrentUserService currentUserService;

    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        return currentUserService.getCurrentUser().orElse(null);
    }
}
