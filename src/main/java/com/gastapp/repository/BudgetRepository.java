package com.gastapp.repository;

import com.gastapp.model.Budget;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    @EntityGraph(attributePaths = {"category"})
    List<Budget> findByUserIdOrderByAnioDescMesDesc(UUID userId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId AND b.mes = :mes AND b.anio = :anio")
    Optional<Budget> findByUserIdAndCategoryIdAndMesAndAnio(
        @Param("userId") UUID userId,
        @Param("categoryId") UUID categoryId,
        @Param("mes") int mes,
        @Param("anio") int anio
    );

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.mes = :mes AND b.anio = :anio")
    List<Budget> findByUserIdAndMesAndAnio(
        @Param("userId") UUID userId,
        @Param("mes") int mes,
        @Param("anio") int anio
    );

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    void deleteByCategory_Id(UUID categoryId);
}
