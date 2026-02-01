package com.gastapp.controller.web;

import com.gastapp.controller.web.dto.CategoryFormDto;
import com.gastapp.model.Category;
import com.gastapp.model.User;
import com.gastapp.repository.ExpenseRepository;
import com.gastapp.service.CategoryService;
import com.gastapp.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryWebController {

    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;
    private final ExpenseRepository expenseRepository;

    @GetMapping
    public String list(Model model) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        var now = java.time.LocalDate.now();
        model.addAttribute("categoriesConPresupuesto", categoryService.findAllConPresupuestoByUserId(userId, now.getYear(), now.getMonthValue()));
        model.addAttribute("month", now.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es")));
        model.addAttribute("year", now.getYear());
        return "categories/list";
    }

    @GetMapping("/new")
    public String formNew(Model model) {
        model.addAttribute("category", new CategoryFormDto());
        return "categories/form";
    }

    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable UUID id, Model model, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        return categoryService.findByIdAndUserId(id, userId)
            .map(c -> {
                model.addAttribute("category", CategoryFormDto.from(c));
                return "categories/form";
            })
            .orElseGet(() -> {
                redirect.addFlashAttribute("error", "Categoría no encontrada.");
                return "redirect:/categories";
            });
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("category") CategoryFormDto form, BindingResult result,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "categories/form";
        }
        User user = currentUserService.getCurrentUser().orElseThrow();
        Category category = form.toCategory(user);
        categoryService.save(category);
        redirect.addFlashAttribute("message", form.getId() != null ? "Categoría actualizada." : "Categoría creada.");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        UUID userId = currentUserService.getCurrentUserIdOrThrow();
        if (categoryService.findByIdAndUserId(id, userId).isEmpty()) {
            redirect.addFlashAttribute("error", "Categoría no encontrada.");
            return "redirect:/categories";
        }
        if (expenseRepository.countByCategoryId(id) > 0) {
            redirect.addFlashAttribute("error", "No se puede eliminar: tiene gastos asociados.");
            return "redirect:/categories";
        }
        categoryService.deleteByIdAndUserId(id, userId);
        redirect.addFlashAttribute("message", "Categoría eliminada.");
        return "redirect:/categories";
    }
}
