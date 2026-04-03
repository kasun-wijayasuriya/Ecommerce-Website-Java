package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "login_attempts")
@CompoundIndex(
        name = "ttl_index",
        def = "{'createdAt': 1}",
        expireAfterSeconds = 900
)
public class LoginAttempt {

    @Id
    private String id;

    @Indexed
    private String email;

    private int attemptCount;

    private boolean locked;

    private LocalDateTime createdAt;

    private LocalDateTime lockedUntil;
}
