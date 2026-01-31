package com.gastapp.controller.web;

import com.gastapp.model.Expense;
import com.gastapp.service.CurrentUserService;
import com.gastapp.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardWebController {

    private final ExpenseService expenseService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String dashboard(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        LocalDate now = LocalDate.now();
        BigDecimal totalMes = expenseService.sumMontoByUserIdAndMonth(userId, now.getYear(), now.getMonthValue());
        List<Expense> ultimos = expenseService.findAllByUserId(userId).stream().limit(10).toList();

        model.addAttribute("totalMes", totalMes);
        model.addAttribute("year", now.getYear());
        model.addAttribute("month", now.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")));
        model.addAttribute("ultimosGastos", ultimos);
        return "dashboard/index";
    }
}
