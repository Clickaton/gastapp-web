package com.gastapp.controller.web;

import com.gastapp.controller.web.dto.ExpenseFormDto;
import com.gastapp.model.Account;
import com.gastapp.model.Category;
import com.gastapp.model.Expense;
import com.gastapp.service.AccountService;
import com.gastapp.service.BudgetValidationResult;
import com.gastapp.service.CategoryService;
import com.gastapp.service.CurrentUserService;
import com.gastapp.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseWebController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        model.addAttribute("expenses", expenseService.findAllByUserId(userId));
        return "expenses/list";
    }

    @GetMapping("/new")
    public String formNew(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        model.addAttribute("expense", new ExpenseFormDto());
        model.addAttribute("categories", categoryService.findAllByUserId(userId));
        model.addAttribute("accounts", accountService.findAllByUserId(userId));
        return "expenses/form";
    }

    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable UUID id, Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        return expenseService.findByIdAndUserId(id, userId)
            .map(e -> {
                model.addAttribute("expense", ExpenseFormDto.from(e));
                model.addAttribute("categories", categoryService.findAllByUserId(userId));
                model.addAttribute("accounts", accountService.findAllByUserId(userId));
                return "expenses/form";
            })
            .orElseGet(() -> {
                redirect.addFlashAttribute("error", "Gasto no encontrado.");
                return "redirect:/expenses";
            });
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("expense") ExpenseFormDto form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllByUserId(userId));
            model.addAttribute("accounts", accountService.findAllByUserId(userId));
            return "expenses/form";
        }
        Category category = categoryService.findByIdAndUserId(form.getCategoryId(), userId)
            .orElse(null);
        if (category == null) {
            result.rejectValue("categoryId", "category.invalid", "Categoría no válida.");
            model.addAttribute("categories", categoryService.findAllByUserId(userId));
            model.addAttribute("accounts", accountService.findAllByUserId(userId));
            return "expenses/form";
        }
        Account account = accountService.findByIdAndUserId(form.getAccountId(), userId)
            .orElse(null);
        if (account == null) {
            result.rejectValue("accountId", "account.invalid", "Cuenta no válida.");
            model.addAttribute("categories", categoryService.findAllByUserId(userId));
            model.addAttribute("accounts", accountService.findAllByUserId(userId));
            return "expenses/form";
        }
        BudgetValidationResult budgetCheck = expenseService.validarPresupuesto(
            userId, form.getCategoryId(), form.getId(), form.getMonto(), form.getFecha());
        if (budgetCheck != null && budgetCheck.presupuestoExcedido()) {
            redirect.addFlashAttribute("warning", budgetCheck.getMensajeAviso());
        }
        Expense expense = form.toExpense(category, account);
        expenseService.save(expense);
        redirect.addFlashAttribute("message", form.getId() != null ? "Gasto actualizado." : "Gasto creado.");
        return "redirect:/expenses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        if (expenseService.deleteByIdAndUserId(id, userId)) {
            redirect.addFlashAttribute("message", "Gasto eliminado.");
        } else {
            redirect.addFlashAttribute("error", "Gasto no encontrado.");
        }
        return "redirect:/expenses";
    }
}
