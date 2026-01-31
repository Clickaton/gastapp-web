package com.gastapp.controller;

import com.gastapp.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private final ExpenseService expenseService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Suma de gastos del mes actual del usuario.
     */
    @GetMapping("/mes-actual")
    public ResponseEntity<Map<String, Object>> gastosMesActual(
        @RequestHeader(value = "X-User-Id", required = false) Optional<String> headerUserId
    ) {
        UUID userId = currentUserProvider.getCurrentUserId(headerUserId);
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        LocalDate now = LocalDate.now();
        BigDecimal total = expenseService.sumMontoByUserIdAndMonth(userId, now.getYear(), now.getMonthValue());
        return ResponseEntity.ok(Map.of(
            "year", now.getYear(),
            "month", now.getMonthValue(),
            "total", total
        ));
    }
}
