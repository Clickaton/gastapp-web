package com.gastapp.controller;

import com.gastapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * En desarrollo: devuelve el primer usuario de la BD (usuario de prueba).
 * En producción: debe leer el usuario actual desde Spring Security (Authentication).
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public UUID getCurrentUserId(Optional<String> headerUserId) {
        if (headerUserId.isPresent()) {
            try {
                return UUID.fromString(headerUserId.get());
            } catch (IllegalArgumentException ignored) {
                // header inválido, seguir con fallback
            }
        }
        return userRepository.findAll().stream()
            .findFirst()
            .map(u -> u.getId())
            .orElse(null);
    }
}
