package com.gastapp.controller.web;

import com.gastapp.controller.web.dto.AccountFormDto;
import com.gastapp.model.Account;
import com.gastapp.service.AccountService;
import com.gastapp.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;
import java.util.Optional;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountWebController {

    private final AccountService accountService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        model.addAttribute("cuentasConSaldos", accountService.listarCuentasConSaldos(userId));
        return "accounts/list";
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
                // Don't setShared here directly, handled by updateAccountSharing logic
                // But we must save the basic fields first.
                // existing.setShared(temp.isShared()); // updateAccountSharing handles this
                accountToSave = existing;
            } else {
                redirect.addFlashAttribute("error", "Cuenta no encontrada.");
                return "redirect:/accounts";
            }
        } else {
            accountToSave = form.toAccount(currentUserService.getCurrentUserOrThrow());
            // isShared is set in toAccount, but we want centralized logic
        }

        Account savedAccount = accountService.save(accountToSave);

        // Handle Sharing Logic centralized
        accountService.updateAccountSharing(savedAccount.getId(), userId, form.isShared(), form.getSharedUserEmail());

        redirect.addFlashAttribute("message", form.getId() != null ? "Cuenta actualizada." : "Cuenta creada.");
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
