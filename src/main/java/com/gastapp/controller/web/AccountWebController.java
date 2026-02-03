package com.gastapp.controller.web;

import com.gastapp.controller.web.dto.AccountFormDto;
import com.gastapp.model.Account;
import com.gastapp.repository.ExpenseRepository;
import com.gastapp.service.AccountService;
import com.gastapp.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountWebController {

    private final AccountService accountService;
    private final CurrentUserService currentUserService;
    private final ExpenseRepository expenseRepository;

    @GetMapping
    public String list(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        model.addAttribute("cuentasConSaldos", accountService.listarCuentasConSaldos(userId));
        return "accounts/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        return accountService.findByIdAndUserId(id, userId)
            .map(a -> {
                model.addAttribute("account", a);
                // Calculate Total Expenses
                BigDecimal totalGastos = expenseRepository.sumMontoByAccountIdAndFechaBefore(id, LocalDate.now());
                model.addAttribute("totalGastos", totalGastos);

                // Calculate Split if shared
                // Total participants = 1 (owner) + sharedUsers.size()
                // Check if account is actually shared (flag true or sharedUsers > 0)
                // The requirement says "if and only if account is shared (more than 1 member)".
                // We use isShared flag and/or sharedUsers list.
                // Note: isShared flag might be true but sharedUsers empty if removed.
                // Safer to check size + 1 > 1.
                int participantCount = 1 + a.getSharedUsers().size();
                if (a.isShared() && participantCount > 1) {
                    BigDecimal split = totalGastos.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);
                    model.addAttribute("montoPorParticipante", split);
                    model.addAttribute("participantCount", participantCount);
                }

                return "accounts/view";
            })
            .orElseGet(() -> {
                redirect.addFlashAttribute("error", "Cuenta no encontrada.");
                return "redirect:/accounts";
            });
    }

    @GetMapping("/new")
    public String formNew(Model model) {
        model.addAttribute("account", new AccountFormDto());
        model.addAttribute("accountTypes", com.gastapp.model.AccountType.values());
        return "accounts/form";
    }

    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable UUID id, Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        return accountService.findByIdAndUserId(id, userId)
            .map(a -> {
                if (!a.getUser().getId().equals(userId)) {
                    redirect.addFlashAttribute("error", "Solo el propietario puede editar la cuenta.");
                    return "redirect:/accounts";
                }
                model.addAttribute("account", AccountFormDto.from(a));
                model.addAttribute("accountTypes", com.gastapp.model.AccountType.values());
                return "accounts/form";
            })
            .orElseGet(() -> {
                redirect.addFlashAttribute("error", "Cuenta no encontrada.");
                return "redirect:/accounts";
            });
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("account") AccountFormDto form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        if (result.hasErrors()) {
            model.addAttribute("accountTypes", com.gastapp.model.AccountType.values());
            return "accounts/form";
        }

        Account accountToSave;
        if (form.getId() != null) {
            Optional<Account> existingOpt = accountService.findByIdAndUserId(form.getId(), userId);
            if (existingOpt.isPresent()) {
                Account existing = existingOpt.get();
                if (!existing.getUser().getId().equals(userId)) {
                     redirect.addFlashAttribute("error", "No tenés permiso para editar esta cuenta.");
                     return "redirect:/accounts";
                }
                Account temp = form.toAccount(existing.getUser());
                existing.setNombre(temp.getNombre());
                existing.setTipo(temp.getTipo());
                existing.setSaldoInicial(temp.getSaldoInicial());
                existing.setIcono(temp.getIcono());
                existing.setColor(temp.getColor());
                // Shared status managed via updateAccountSharing
                accountToSave = existing;
            } else {
                redirect.addFlashAttribute("error", "Cuenta no encontrada.");
                return "redirect:/accounts";
            }
        } else {
            accountToSave = form.toAccount(currentUserService.getCurrentUserOrThrow());
        }

        Account savedAccount = accountService.save(accountToSave);

        // Handle Sharing Logic centralized
        try {
            accountService.updateAccountSharing(savedAccount.getId(), userId, form.isShared(), form.getSharedUserEmail());
            redirect.addFlashAttribute("message", form.getId() != null ? "Cuenta actualizada." : "Cuenta creada.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", "Cuenta guardada, pero hubo un error al compartir: " + e.getMessage());
        }

        return "redirect:/accounts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        if (accountService.deleteByIdAndUserId(id, userId)) {
            redirect.addFlashAttribute("message", "Cuenta eliminada.");
        } else {
            redirect.addFlashAttribute("error", "Cuenta no encontrada.");
        }
        return "redirect:/accounts";
    }
}
