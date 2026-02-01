package com.gastapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budgets", indexes = {
    @Index(name = "ix_budgets_user_id", columnList = "user_id"),
    @Index(name = "ix_budgets_category_id", columnList = "category_id"),
    @Index(name = "ix_budgets_mes_anio", columnList = "mes, anio")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget extends BaseAuditableEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "monto_maximo", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoMaximo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Override
    public String toString() {
        return "Budget{id=" + id + ", mes=" + mes + ", anio=" + anio + ", category=" + (category != null ? category.getId() : null) + "}";
    }
}
