package com.gastapp.controller;

import com.gastapp.model.Expense;
import com.gastapp.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador de gastos. Para verificación inicial devuelve gastos de prueba.
 * En producción, el userId debe venir de Spring Security (Authentication).
 */
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Lista gastos del usuario actual. Para demo: si no hay usuario logueado, usa usuario de prueba.
     */
    @GetMapping
    public ResponseEntity<List<Expense>> list(
        @RequestHeader(value = "X-User-Id", required = false) Optional<String> headerUserId
    ) {
        UUID userId = currentUserProvider.getCurrentUserId(headerUserId);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(expenseService.findAllByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getById(
        @PathVariable UUID id,
        @RequestHeader(value = "X-User-Id", required = false) Optional<String> headerUserId
    ) {
        UUID userId = currentUserProvider.getCurrentUserId(headerUserId);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        return expenseService.findByIdAndUserId(id, userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
