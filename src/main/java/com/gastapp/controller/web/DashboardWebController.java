package com.gastapp.controller.web;

import com.gastapp.model.Account;
import com.gastapp.model.Expense;
import com.gastapp.service.AccountService;
import com.gastapp.service.BalanceService;
import com.gastapp.service.CurrentUserService;
import com.gastapp.service.ExpenseService;
import com.gastapp.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardWebController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final BalanceService balanceService;
    private final CurrentUserService currentUserService;
    private final AccountService accountService;

    @GetMapping
    public String dashboard(@RequestParam(required = false) UUID accountId, Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // 1. Get all accessible accounts (Owned + Shared)
        List<Account> allAccounts = accountService.findAllByUserId(userId);
        List<BalanceService.CuentaConSaldo> cuentasConSaldos = balanceService.listarCuentasConSaldos(userId);

        // 2. Determine target accounts
        UUID finalAccountId = accountId; // Effectively final for lambda
        List<UUID> targetAccountIds;
        if (accountId != null) {
            // Validate user has access
            boolean hasAccess = allAccounts.stream().anyMatch(a -> a.getId().equals(finalAccountId));
            if (hasAccess) {
                targetAccountIds = List.of(accountId);
            } else {
                // Fallback or Error? Let's fallback to all
                targetAccountIds = allAccounts.stream().map(Account::getId).collect(Collectors.toList());
                accountId = null; // Reset selection
            }
        } else {
            targetAccountIds = allAccounts.stream().map(Account::getId).collect(Collectors.toList());
        }

        // 3. Calculate Totals based on Target Accounts
        BigDecimal ingresosMes = incomeService.sumMontoByAccountIdsAndMonth(targetAccountIds, year, month);
        BigDecimal gastosMes = expenseService.sumMontoByAccountIdsAndMonth(targetAccountIds, year, month);
        BigDecimal balanceNeto = balanceService.balanceNetoMensualByAccountIds(targetAccountIds, year, month);

        // 4. Fetch Recent Expenses based on Target Accounts
        List<Expense> ultimos;
        if (targetAccountIds.isEmpty()) {
            ultimos = Collections.emptyList();
        } else {
            ultimos = expenseService.findByAccountIds(targetAccountIds).stream().limit(10).toList();
        }

        model.addAttribute("ingresosMes", ingresosMes);
        model.addAttribute("gastosMes", gastosMes);
        model.addAttribute("balanceNeto", balanceNeto);
        model.addAttribute("year", year);
        model.addAttribute("month", now.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")));
        model.addAttribute("ultimosGastos", ultimos);
        model.addAttribute("cuentasConSaldos", cuentasConSaldos);

        // For Selector
        model.addAttribute("allAccounts", allAccounts);
        model.addAttribute("selectedAccountId", accountId);

        return "dashboard/index";
    }
}
