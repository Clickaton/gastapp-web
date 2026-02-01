package com.gastapp.service;

import com.gastapp.model.Account;
import com.gastapp.repository.AccountRepository;
import com.gastapp.repository.ExpenseRepository;
import com.gastapp.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para cálculo de saldos y balance financiero.
 * - Saldo de cuenta: Saldo Inicial + Suma(Ingresos) - Suma(Gastos)
 * - Balance neto mensual: Total Ingresos Mes - Total Gastos Mes
 */
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final AccountRepository accountRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    /**
     * Calcula el saldo actual de una cuenta: Saldo Inicial + Ingresos - Gastos (hasta hoy).
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoCuenta(UUID accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return BigDecimal.ZERO;
        }
        LocalDate hasta = LocalDate.now();
        BigDecimal ingresos = incomeRepository.sumMontoByAccountIdAndFechaBefore(accountId, hasta);
        BigDecimal gastos = expenseRepository.sumMontoByAccountIdAndFechaBefore(accountId, hasta);
        return account.getSaldoInicial().add(ingresos).subtract(gastos);
    }

    /**
     * Balance neto del mes actual: Total Ingresos Mes - Total Gastos Mes.
     */
    @Transactional(readOnly = true)
    public BigDecimal balanceNetoMensual(UUID userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal ingresos = incomeRepository.sumMontoByUserIdAndFechaBetween(userId, start, end);
        BigDecimal gastos = expenseRepository.sumMontoByUserIdAndFechaBetween(userId, start, end);
        return ingresos.subtract(gastos);
    }

    /**
     * DTO simple para mostrar cuenta con su saldo calculado.
     */
    public record CuentaConSaldo(Account account, BigDecimal saldoActual) {}

    /**
     * Lista de cuentas del usuario con sus saldos actuales.
     */
    @Transactional(readOnly = true)
    public List<CuentaConSaldo> listarCuentasConSaldos(UUID userId) {
        List<Account> accounts = accountRepository.findAccountsForUser(userId);
        return accounts.stream()
            .map(a -> new CuentaConSaldo(a, calcularSaldoCuenta(a.getId())))
            .collect(Collectors.toList());
    }
}
