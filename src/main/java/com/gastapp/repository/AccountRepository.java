package com.gastapp.repository;

import com.gastapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserIdOrderByNombreAsc(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN a.sharedUsers u WHERE a.user.id = :userId OR (a.isShared = true AND u.id = :userId) ORDER BY a.nombre ASC")
    List<Account> findAccountsForUser(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN FETCH a.user LEFT JOIN FETCH a.sharedUsers u WHERE a.id = :id AND (a.user.id = :userId OR (a.isShared = true AND u.id = :userId))")
    Optional<Account> findAccountForUser(@Param("id") UUID id, @Param("userId") UUID userId);
}
