package com.gastapp.repository;

import com.gastapp.model.Income;
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
public interface IncomeRepository extends JpaRepository<Income, UUID> {

    @EntityGraph(attributePaths = "account")
    @Query("SELECT i FROM Income i WHERE i.user.id = :userId ORDER BY i.fecha DESC")
    List<Income> findByUserIdOrderByFechaDescWithAccount(@Param("userId") UUID userId);

    Optional<Income> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Income i WHERE i.user.id = :userId AND i.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumMontoByUserIdAndFechaBetween(
        @Param("userId") UUID userId,
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
    );

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Income i WHERE i.account.id IN :accountIds AND i.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumMontoByAccountIdsAndFechaBetween(
        @Param("accountIds") List<UUID> accountIds,
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
    );

    @Query("SELECT COALESCE(SUM(i.monto), 0) FROM Income i WHERE i.account.id = :accountId AND i.fecha <= :hasta")
    BigDecimal sumMontoByAccountIdAndFechaBefore(
        @Param("accountId") UUID accountId,
        @Param("hasta") LocalDate hasta
    );
}
