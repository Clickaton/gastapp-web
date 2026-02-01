package com.gastapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "ix_expenses_user_id", columnList = "user_id"),
    @Index(name = "ix_expenses_category_id", columnList = "category_id"),
    @Index(name = "ix_expenses_account_id", columnList = "account_id"),
    @Index(name = "ix_expenses_fecha", columnList = "fecha")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends BaseAuditableEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Override
    public String toString() {
        return "Expense{id=" + id + ", monto=" + monto + ", fecha=" + fecha + ", user=" + (user != null ? user.getId() : null) + "}";
    }
}
