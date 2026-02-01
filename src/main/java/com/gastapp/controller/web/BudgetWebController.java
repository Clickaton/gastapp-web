package com.gastapp.controller.web;

import com.gastapp.controller.web.dto.BudgetFormDto;
import com.gastapp.model.Budget;
import com.gastapp.model.Category;
import com.gastapp.service.BudgetService;
import com.gastapp.service.CategoryService;
import com.gastapp.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetWebController {

    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        LocalDate now = LocalDate.now();
        model.addAttribute("budgets", budgetService.findByUserIdAndMesAndAnio(userId, now.getMonthValue(), now.getYear()));
        model.addAttribute("month", now.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")));
        model.addAttribute("year", now.getYear());
        return "budgets/list";
    }

    @GetMapping("/new")
    public String formNew(@RequestParam(required = false) UUID categoryId, Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        LocalDate now = LocalDate.now();
        BudgetFormDto dto = new BudgetFormDto();
        dto.setMes(now.getMonthValue());
        dto.setAnio(now.getYear());
        if (categoryId != null) {
            dto.setCategoryId(categoryId);
        }
        model.addAttribute("budget", dto);
        model.addAttribute("categories", categoryService.findAllByUserId(userId));
        model.addAttribute("months", Arrays.stream(Month.values()).map(m -> Map.entry(m.getValue(), m.getDisplayName(TextStyle.FULL, new Locale("es")))).collect(Collectors.toList()));
        return "budgets/form";
    }

    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable UUID id, Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        return budgetService.findByIdAndUserId(id, userId)
            .map(b -> {
                model.addAttribute("budget", BudgetFormDto.from(b));
                model.addAttribute("categories", categoryService.findAllByUserId(userId));
                model.addAttribute("months", Arrays.stream(Month.values()).map(m -> Map.entry(m.getValue(), m.getDisplayName(TextStyle.FULL, new Locale("es")))).collect(Collectors.toList()));
                return "budgets/form";
            })
            .orElseGet(() -> {
                redirect.addFlashAttribute("error", "Presupuesto no encontrado.");
                return "redirect:/budgets";
            });
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("budget") BudgetFormDto form, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        List<Map.Entry<Integer, String>> monthsList = Arrays.stream(Month.values()).map(m -> Map.entry(m.getValue(), m.getDisplayName(TextStyle.FULL, new Locale("es")))).collect(Collectors.toList());
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllByUserId(userId));
            model.addAttribute("months", monthsList);
            return "budgets/form";
        }
        Category category = categoryService.findByIdAndUserId(form.getCategoryId(), userId)
            .orElse(null);
        if (category == null) {
            result.rejectValue("categoryId", "category.invalid", "Categoría no válida.");
            model.addAttribute("categories", categoryService.findAllByUserId(userId));
            model.addAttribute("months", monthsList);
            return "budgets/form";
        }
        var existing = budgetService.findByUserIdAndCategoryIdAndMesAndAnio(userId, form.getCategoryId(), form.getMes(), form.getAnio());
        Budget budget;
        if (form.getId() != null) {
            budget = budgetService.findByIdAndUserId(form.getId(), userId).orElse(null);
            if (budget == null) {
                redirect.addFlashAttribute("error", "Presupuesto no encontrado.");
                return "redirect:/budgets";
            }
            budget.setMontoMaximo(form.getMontoMaximo());
        } else {
            if (existing.isPresent()) {
                result.rejectValue("categoryId", "budget.duplicate", "Ya existe un presupuesto para esta categoría en el mes/año seleccionado.");
                model.addAttribute("categories", categoryService.findAllByUserId(userId));
                model.addAttribute("months", monthsList);
                return "budgets/form";
            }
            budget = Budget.builder()
                .id(UUID.randomUUID())
                .mes(form.getMes())
                .anio(form.getAnio())
                .montoMaximo(form.getMontoMaximo())
                .category(category)
                .user(category.getUser())
                .build();
        }
        budgetService.save(budget);
        redirect.addFlashAttribute("message", form.getId() != null ? "Presupuesto actualizado." : "Presupuesto creado.");
        return "redirect:/budgets";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        if (budgetService.deleteByIdAndUserId(id, userId)) {
            redirect.addFlashAttribute("message", "Presupuesto eliminado.");
        } else {
            redirect.addFlashAttribute("error", "Presupuesto no encontrado.");
        }
        return "redirect:/budgets";
    }
}
