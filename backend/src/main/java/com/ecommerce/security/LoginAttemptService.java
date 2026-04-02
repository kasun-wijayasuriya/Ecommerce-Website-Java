package com.ecommerce.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_SECONDS = 900; // 15 minutes

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockUntil = new ConcurrentHashMap<>();

    public boolean isLocked(String key) {
        Instant until = lockUntil.get(key);

        if (until == null) {
            return false;
        }

        if (Instant.now().isAfter(until)) {
            lockUntil.remove(key);
            attempts.remove(key);
            return false;
        }

        return true;
    }

    public void loginSucceeded(String key) {
        attempts.remove(key);
        lockUntil.remove(key);
    }

    public void loginFailed(String key) {
        int count = attempts.getOrDefault(key, 0) + 1;
        attempts.put(key, count);

        if (count >= MAX_ATTEMPTS) {
            lockUntil.put(key, Instant.now().plusSeconds(LOCK_TIME_SECONDS));
        }
    }

    public int remainingAttempts(String key) {
        return Math.max(0, MAX_ATTEMPTS - attempts.getOrDefault(key, 0));
    }

}
