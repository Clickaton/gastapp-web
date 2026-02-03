package com.gastapp.service;

import com.gastapp.model.Income;
import com.gastapp.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;

    @Transactional(readOnly = true)
    public List<Income> findAllByUserId(UUID userId) {
        return incomeRepository.findByUserIdOrderByFechaDescWithAccount(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Income> findByIdAndUserId(UUID id, UUID userId) {
        return incomeRepository.findByIdAndUserId(id, userId);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumMontoByUserIdAndMonth(UUID userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return incomeRepository.sumMontoByUserIdAndFechaBetween(userId, start, end);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumMontoByAccountIdsAndMonth(List<UUID> accountIds, int year, int month) {
        if (accountIds == null || accountIds.isEmpty()) return BigDecimal.ZERO;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return incomeRepository.sumMontoByAccountIdsAndFechaBetween(accountIds, start, end);
    }

    @Transactional
    public Income save(Income income) {
        return incomeRepository.save(income);
    }

    @Transactional
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        if (!incomeRepository.existsByIdAndUserId(id, userId)) {
            return false;
        }
        incomeRepository.deleteById(id);
        return true;
    }
}
