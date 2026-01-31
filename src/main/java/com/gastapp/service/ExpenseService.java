package com.gastapp.service;

import com.gastapp.model.Expense;
import com.gastapp.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de gastos. Todas las operaciones exigen userId para evitar filtración de datos.
 * En producción, el userId debe obtenerse siempre de Spring Security (usuario logueado).
 */
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<Expense> findAllByUserId(UUID userId) {
        return expenseRepository.findByUserIdOrderByFechaDescWithCategory(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Expense> findByIdAndUserId(UUID expenseId, UUID userId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumMontoByUserIdAndMonth(UUID userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.sumMontoByUserIdAndFechaBetween(userId, start, end);
    }

    @Transactional
    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Transactional
    public boolean deleteByIdAndUserId(UUID expenseId, UUID userId) {
        if (!expenseRepository.existsByIdAndUserId(expenseId, userId)) {
            return false;
        }
        expenseRepository.deleteById(expenseId);
        return true;
    }
}
