package com.gastapp.service;

import com.gastapp.model.Account;
import com.gastapp.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final BalanceService balanceService;

    @Transactional(readOnly = true)
    public List<Account> findAllByUserId(UUID userId) {
        return accountRepository.findByUserIdOrderByNombreAsc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByIdAndUserId(UUID id, UUID userId) {
        return accountRepository.findByIdAndUserId(id, userId);
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal getSaldoActual(Account account) {
        return balanceService.calcularSaldoCuenta(account.getId());
    }

    @Transactional(readOnly = true)
    public java.util.List<BalanceService.CuentaConSaldo> listarCuentasConSaldos(UUID userId) {
        return balanceService.listarCuentasConSaldos(userId);
    }

    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Transactional
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        if (!accountRepository.existsByIdAndUserId(id, userId)) {
            return false;
        }
        accountRepository.deleteById(id);
        return true;
    }
}
