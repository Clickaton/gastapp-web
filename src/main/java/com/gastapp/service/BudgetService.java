package com.gastapp.service;

import com.gastapp.model.Budget;
import com.gastapp.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public List<Budget> findAllByUserId(UUID userId) {
        return budgetRepository.findByUserIdOrderByAnioDescMesDesc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Budget> findByIdAndUserId(UUID id, UUID userId) {
        return budgetRepository.findByIdAndUserId(id, userId);
    }

    @Transactional(readOnly = true)
    public Optional<Budget> findByUserIdAndCategoryIdAndMesAndAnio(UUID userId, UUID categoryId, int mes, int anio) {
        return budgetRepository.findByUserIdAndCategoryIdAndMesAndAnio(userId, categoryId, mes, anio);
    }

    @Transactional(readOnly = true)
    public List<Budget> findByUserIdAndMesAndAnio(UUID userId, int mes, int anio) {
        return budgetRepository.findByUserIdAndMesAndAnio(userId, mes, anio);
    }

    @Transactional
    public Budget save(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Transactional
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        if (!budgetRepository.existsByIdAndUserId(id, userId)) {
            return false;
        }
        budgetRepository.deleteById(id);
        return true;
    }
}
