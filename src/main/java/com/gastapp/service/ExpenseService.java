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
    private final BudgetService budgetService;

    @Transactional(readOnly = true)
    public List<Expense> findAllByUserId(UUID userId) {
        return expenseRepository.findExpensesForUser(userId);
    }

    @Transactional(readOnly = true)
    public List<Expense> findByAccountIds(List<UUID> accountIds) {
        return expenseRepository.findByAccountIdInOrderByFechaDesc(accountIds);
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

    @Transactional(readOnly = true)
    public BigDecimal sumMontoByAccountIdsAndMonth(List<UUID> accountIds, int year, int month) {
        if (accountIds == null || accountIds.isEmpty()) return BigDecimal.ZERO;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.sumMontoByAccountIdsAndFechaBetween(accountIds, start, end);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumMontoByUserIdAndCategoryIdAndMonth(UUID userId, UUID categoryId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return expenseRepository.sumMontoByUserIdAndCategoryIdAndFechaBetween(userId, categoryId, start, end);
    }

    /**
     * Valida si al registrar/editar un gasto se supera el presupuesto de la categoría para el mes.
     */
    @Transactional(readOnly = true)
    public BudgetValidationResult validarPresupuesto(UUID userId, UUID categoryId, UUID expenseId, BigDecimal montoNuevo, LocalDate fecha) {
        var budgetOpt = budgetService.findByUserIdAndCategoryIdAndMesAndAnio(userId, categoryId, fecha.getMonthValue(), fecha.getYear());
        if (budgetOpt.isEmpty()) {
            return new BudgetValidationResult(false, null, null, null);
        }
        BigDecimal limite = budgetOpt.get().getMontoMaximo();
        BigDecimal gastoActual = sumMontoByUserIdAndCategoryIdAndMonth(userId, categoryId, fecha.getYear(), fecha.getMonthValue());
        if (expenseId != null) {
            var existing = expenseRepository.findByIdAndUserId(expenseId, userId);
            if (existing.isPresent() && existing.get().getFecha().getMonthValue() == fecha.getMonthValue() && existing.get().getFecha().getYear() == fecha.getYear()) {
                gastoActual = gastoActual.subtract(existing.get().getMonto());
            }
        }
        BigDecimal totalConNuevo = gastoActual.add(montoNuevo);
        boolean excedido = totalConNuevo.compareTo(limite) > 0;
        return new BudgetValidationResult(excedido, limite, totalConNuevo, montoNuevo);
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
