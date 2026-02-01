package com.gastapp.repository;

import com.gastapp.model.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByUserIdOrderByFechaDesc(UUID userId);

    @EntityGraph(attributePaths = {"category", "account"})
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId ORDER BY e.fecha DESC")
    List<Expense> findByUserIdOrderByFechaDescWithCategory(@Param("userId") UUID userId);

    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    long countByCategoryId(UUID categoryId);

    /**
     * Suma de gastos del usuario en un rango de fechas (ej. mes actual).
     */
    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Expense e WHERE e.user.id = :userId AND e.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumMontoByUserIdAndFechaBetween(
        @Param("userId") UUID userId,
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
    );

    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Expense e WHERE e.account.id = :accountId AND e.fecha <= :hasta")
    BigDecimal sumMontoByAccountIdAndFechaBefore(
        @Param("accountId") UUID accountId,
        @Param("hasta") LocalDate hasta
    );

    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Expense e WHERE e.user.id = :userId AND e.category.id = :categoryId AND e.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumMontoByUserIdAndCategoryIdAndFechaBetween(
        @Param("userId") UUID userId,
        @Param("categoryId") UUID categoryId,
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
    );
}
