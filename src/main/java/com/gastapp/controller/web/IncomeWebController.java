package com.gastapp.controller.web;

import com.gastapp.controller.web.dto.IncomeFormDto;
import com.gastapp.model.Account;
import com.gastapp.model.Income;
import com.gastapp.service.AccountService;
import com.gastapp.service.CurrentUserService;
import com.gastapp.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeWebController {

    private final IncomeService incomeService;
    private final AccountService accountService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        model.addAttribute("incomes", incomeService.findAllByUserId(userId));
        return "incomes/list";
    }

    @GetMapping("/new")
    public String formNew(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        model.addAttribute("income", new IncomeFormDto());
        model.addAttribute("accounts", accountService.findAllByUserId(userId));
        return "incomes/form";
    }

    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable UUID id, Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        return incomeService.findByIdAndUserId(id, userId)
            .map(i -> {
                model.addAttribute("income", IncomeFormDto.from(i));
                model.addAttribute("accounts", accountService.findAllByUserId(userId));
                return "incomes/form";
            })
            .orElseGet(() -> {
                redirect.addFlashAttribute("error", "Ingreso no encontrado.");
                return "redirect:/incomes";
            });
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("income") IncomeFormDto form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        if (result.hasErrors()) {
            model.addAttribute("accounts", accountService.findAllByUserId(userId));
            return "incomes/form";
        }
        Account account = accountService.findByIdAndUserId(form.getAccountId(), userId)
            .orElse(null);
        if (account == null) {
            result.rejectValue("accountId", "account.invalid", "Cuenta no válida.");
            model.addAttribute("accounts", accountService.findAllByUserId(userId));
            return "incomes/form";
        }
        Income income = form.toIncome(account);
        incomeService.save(income);
        redirect.addFlashAttribute("message", form.getId() != null ? "Ingreso actualizado." : "Ingreso creado.");
        return "redirect:/incomes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        if (incomeService.deleteByIdAndUserId(id, userId)) {
            redirect.addFlashAttribute("message", "Ingreso eliminado.");
        } else {
            redirect.addFlashAttribute("error", "Ingreso no encontrado.");
        }
        return "redirect:/incomes";
    }
}
