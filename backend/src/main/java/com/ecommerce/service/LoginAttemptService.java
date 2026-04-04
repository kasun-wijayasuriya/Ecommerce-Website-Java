package com.ecommerce.service;

import com.ecommerce.model.LoginAttempt;
import com.ecommerce.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    private final LoginAttemptRepository loginAttemptRepository;

    public void loginFailed(String email) {
        Optional<LoginAttempt> existing = loginAttemptRepository.findByEmail(email);

        if (existing.isPresent()) {
            LoginAttempt attempt = existing.get();

            if (attempt.isLocked() && attempt.getLockedUntil() != null
                    && LocalDateTime.now().isBefore(attempt.getLockedUntil())) {
                return;
            }

            attempt.setAttemptCount(attempt.getAttemptCount() + 1);

            if (attempt.getAttemptCount() >= MAX_ATTEMPTS) {
                attempt.setLocked(true);
                attempt.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            }

            loginAttemptRepository.save(attempt);
        } else {
            LoginAttempt newAttempt = LoginAttempt.builder()
                    .email(email)
                    .attemptCount(1)
                    .locked(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            loginAttemptRepository.save(newAttempt);
        }
    }

    public void loginSucceeded(String email) {
        loginAttemptRepository.findByEmail(email)
                .ifPresent(loginAttemptRepository::delete);
    }

    public boolean isLocked(String email) {
        Optional<LoginAttempt> existing = loginAttemptRepository.findByEmail(email);

        if (existing.isEmpty()) {
            return false;
        }

        LoginAttempt attempt = existing.get();

        if (!attempt.isLocked()) {
            return false;
        }

        if (attempt.getLockedUntil() != null && LocalDateTime.now().isBefore(attempt.getLockedUntil())) {
            return true;
        }

        if (attempt.getLockedUntil() != null && LocalDateTime.now().isAfter(attempt.getLockedUntil())) {
            attempt.setLocked(false);
            attempt.setAttemptCount(0);
            attempt.setLockedUntil(null);
            loginAttemptRepository.save(attempt);
            return false;
        }

        return false;
    }
}
