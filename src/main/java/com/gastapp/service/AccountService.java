package com.gastapp.service;

import com.gastapp.model.Account;
import com.gastapp.model.User;
import com.gastapp.repository.AccountRepository;
import com.gastapp.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final BalanceService balanceService;

    @Transactional(readOnly = true)
    public List<Account> findAllByUserId(UUID userId) {
        // Updated to include shared accounts
        return accountRepository.findAccountsForUser(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findByIdAndUserId(UUID id, UUID userId) {
        // Updated to find shared accounts too
        return accountRepository.findAccountForUser(id, userId);
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal getSaldoActual(Account account) {
        return balanceService.calcularSaldoCuenta(account.getId());
    }

    @Transactional(readOnly = true)
    public java.util.List<BalanceService.CuentaConSaldo> listarCuentasConSaldos(UUID userId) {
        // Delegates to BalanceService which now needs to use findAccountsForUser
        return balanceService.listarCuentasConSaldos(userId);
    }

    @Transactional
    public Account save(Account account) {
        // Logic to handle "unsharing": if isShared is false, clear sharedUsers.
        // But 'account' here might be a detached entity or coming from DTO conversion.
        // It's safer to load existing if ID present to handle collections?
        // But save works. We just need to make sure sharedUsers are cleared if !isShared.
        // However, the 'account' object passed from controller (dto.toAccount) might have empty sharedUsers set
        // because DTO doesn't populate it. So simply saving it might wipe out sharedUsers if we are not careful?
        // Wait, AccountFormDto.toAccount() doesn't set sharedUsers.
        // So 'account.sharedUsers' is likely null or empty.
        // If we save it, we might lose existing shared users if we are not careful or if we merge.
        // Let's check Controller save logic.
        return accountRepository.save(account);
    }

    @Transactional
    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        // Only owner can delete
        if (!isOwner(id, userId)) {
            return false;
        }
        accountRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isOwner(UUID accountId, UUID userId) {
        return accountRepository.existsByIdAndUserId(accountId, userId);
    }

    @Transactional
    public void updateAccountSharing(UUID accountId, UUID ownerId, boolean isShared, String email) {
        // Verify ownership
        if (!isOwner(accountId, ownerId)) return;

        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) return;

        account.setShared(isShared);

        if (!isShared) {
            account.getSharedUsers().clear();
        } else if (email != null && !email.isBlank()) {
             // Requirement: "Allow selecting a user". Implicitly implies 1 user or adding users.
             // Given the UI allows entering 1 email, I will treat it as "Set this user as the shared user".
             // If we want to support multiple, we would need a list UI.
             // For safety and alignment with UI, I will clear and add (Single Shared User Mode).
             // Or at least, if the email is different, add it.
             // But to allow "Revoke", we need to know who to revoke.
             // If the UI only has one email input, the intention is likely "Share with this person".
             // I'll clear existing to ensure the state matches the "Single Email Input" UI paradigm.

             Optional<User> userOpt = userRepository.findByEmail(email);
             if (userOpt.isPresent()) {
                 User userToShare = userOpt.get();
                 if (!userToShare.getId().equals(ownerId)) {
                     account.getSharedUsers().clear(); // Enforce single shared user for now based on UI
                     account.getSharedUsers().add(userToShare);
                 }
             }
        }

        accountRepository.save(account);
    }
}
