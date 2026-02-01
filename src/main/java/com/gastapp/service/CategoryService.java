package com.gastapp.service;

import com.gastapp.model.Category;
import com.gastapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BudgetService budgetService;
    private final ExpenseService expenseService;
    private final com.gastapp.repository.BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public List<Category> findAllByUserId(UUID userId) {
        return categoryRepository.findByUserIdOrderByNombreAsc(userId);
    }

    @Transactional(readOnly = true)
    public List<CategoriaConPresupuesto> findAllConPresupuestoByUserId(UUID userId, int year, int month) {
        List<Category> categories = categoryRepository.findByUserIdOrderByNombreAsc(userId);
        return categories.stream()
            .map(cat -> {
                var budgetOpt = budgetService.findByUserIdAndCategoryIdAndMesAndAnio(userId, cat.getId(), month, year);
                BigDecimal montoPresupuestado = budgetOpt.map(b -> b.getMontoMaximo()).orElse(null);
                java.util.UUID budgetId = budgetOpt.map(b -> b.getId()).orElse(null);
                BigDecimal gastoActual = expenseService.sumMontoByUserIdAndCategoryIdAndMonth(userId, cat.getId(), year, month);
                return CategoriaConPresupuesto.builder()
                    .category(cat)
                    .montoPresupuestado(montoPresupuestado)
                    .gastoActual(gastoActual != null ? gastoActual : BigDecimal.ZERO)
                    .budgetId(budgetId)
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByIdAndUserId(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        if (!categoryRepository.existsByIdAndUserId(id, userId)) {
            return false;
        }
        budgetRepository.deleteByCategory_Id(id);
        categoryRepository.deleteById(id);
        return true;
    }
}
