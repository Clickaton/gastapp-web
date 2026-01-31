package com.gastapp.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Añade variables al modelo para todas las vistas Thymeleaf.
 * Evita usar #request en templates (no disponible por defecto en Thymeleaf 3.1+).
 */
@ControllerAdvice
public class ThymeleafControllerAdvice {

    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
