package com.gastapp.config;

import com.gastapp.model.*;
import com.gastapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Carga datos de prueba en desarrollo (H2).
 */
@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        User user = User.builder()
            .id(UUID.randomUUID())
            .email("test@gastapp.com")
            .passwordHash(passwordEncoder.encode("test123"))
            .nombre("Usuario Prueba")
            .build();
        user = userRepository.save(user);
        log.info("Usuario de prueba creado: {}", user.getEmail());

        Category cat = Category.builder()
            .id(UUID.randomUUID())
            .nombre("Comida")
            .icono("bi-egg-fill")
            .color("#4CAF50")
            .user(user)
            .build();
        cat = categoryRepository.save(cat);

        Account cuentaBanco = Account.builder()
            .id(UUID.randomUUID())
            .nombre("Banco")
            .tipo(AccountType.BANCO)
            .saldoInicial(BigDecimal.ZERO)
            .icono("bi-bank")
            .color("#0d6efd")
            .user(user)
            .build();
        cuentaBanco = accountRepository.save(cuentaBanco);

        Income ingreso = Income.builder()
            .id(UUID.randomUUID())
            .monto(new BigDecimal("1000"))
            .descripcion("Sueldo")
            .fecha(LocalDate.now())
            .account(cuentaBanco)
            .user(user)
            .build();
        incomeRepository.save(ingreso);

        Budget presupuesto = Budget.builder()
            .id(UUID.randomUUID())
            .mes(LocalDate.now().getMonthValue())
            .anio(LocalDate.now().getYear())
            .montoMaximo(new BigDecimal("500"))
            .category(cat)
            .user(user)
            .build();
        budgetRepository.save(presupuesto);

        List<Expense> gastos = List.of(
            Expense.builder().id(UUID.randomUUID()).monto(new BigDecimal("150.50")).descripcion("Almuerzo").fecha(LocalDate.now()).category(cat).account(cuentaBanco).user(user).build(),
            Expense.builder().id(UUID.randomUUID()).monto(new BigDecimal("320.00")).descripcion("Supermercado").fecha(LocalDate.now().minusDays(1)).category(cat).account(cuentaBanco).user(user).build(),
            Expense.builder().id(UUID.randomUUID()).monto(new BigDecimal("45.00")).descripcion("Café").fecha(LocalDate.now().minusDays(2)).category(cat).account(cuentaBanco).user(user).build()
        );
        expenseRepository.saveAll(gastos);
        log.info("Datos de prueba creados: usuario, categoría, cuenta, ingreso 1000$, presupuesto 500$, gastos {} total", gastos.size());
    }
}
