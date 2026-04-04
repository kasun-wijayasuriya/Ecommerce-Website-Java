package com.ecommerce.repository;

import com.ecommerce.model.LoginAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends MongoRepository<LoginAttempt, String> {
    Optional<LoginAttempt> findByEmail(String email);
    void deleteByLockedUntilBefore(LocalDateTime now);
}
