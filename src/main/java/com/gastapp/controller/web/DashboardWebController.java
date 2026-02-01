package com.gastapp.controller.web;

import com.gastapp.model.Expense;
import com.gastapp.service.BalanceService;
import com.gastapp.service.CurrentUserService;
import com.gastapp.service.ExpenseService;
import com.gastapp.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardWebController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final BalanceService balanceService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String dashboard(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        BigDecimal ingresosMes = incomeService.sumMontoByUserIdAndMonth(userId, year, month);
        BigDecimal gastosMes = expenseService.sumMontoByUserIdAndMonth(userId, year, month);
        BigDecimal balanceNeto = balanceService.balanceNetoMensual(userId, year, month);
        List<Expense> ultimos = expenseService.findAllByUserId(userId).stream().limit(10).toList();
        List<BalanceService.CuentaConSaldo> cuentasConSaldos = balanceService.listarCuentasConSaldos(userId);

        model.addAttribute("ingresosMes", ingresosMes);
        model.addAttribute("gastosMes", gastosMes);
        model.addAttribute("balanceNeto", balanceNeto);
        model.addAttribute("year", year);
        model.addAttribute("month", now.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")));
        model.addAttribute("ultimosGastos", ultimos);
        model.addAttribute("cuentasConSaldos", cuentasConSaldos);
        return "dashboard/index";
    }
}
