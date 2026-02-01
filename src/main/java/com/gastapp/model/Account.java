package com.gastapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "ix_accounts_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseAuditableEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AccountType tipo;

    @Column(name = "saldo_inicial", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoInicial;

    @Column(name = "icono", length = 50)
    private String icono;

    @Column(name = "color", length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Builder.Default
    @Column(name = "is_shared", nullable = false)
    private boolean isShared = false;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "account_shared_users",
        joinColumns = @JoinColumn(name = "account_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> sharedUsers = new HashSet<>();

    @Override
    public String toString() {
        return "Account{id=" + id + ", nombre='" + nombre + "', tipo=" + tipo + ", user=" + (user != null ? user.getId() : null) + ", isShared=" + isShared + "}";
    }
}
