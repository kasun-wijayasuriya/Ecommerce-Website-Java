package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.LockedException;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final PasswordValidatorService passwordValidatorService;
    private final HtmlSanitizerService htmlSanitizerService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        org.passay.RuleResult result = passwordValidatorService.validate(request.getPassword());
        if (!result.isValid()) {
            throw new BadRequestException(String.join(", ", passwordValidatorService.getErrors(result)));
        }

        User user = User.builder()
                .firstName(htmlSanitizerService.sanitize(request.getFirstName()))
                .lastName(htmlSanitizerService.sanitize(request.getLastName()))
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .roles(Set.of(User.Role.USER))
                .active(true)
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return AuthResponse.of(token, UserResponse.fromUser(user));
    }

    public AuthResponse login(LoginRequest request) {
        if (loginAttemptService.isLocked(request.getEmail())) {
            throw new LockedException("Account is temporarily locked. Try again later.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            loginAttemptService.loginSucceeded(request.getEmail());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadRequestException("User not found"));

            String token = jwtTokenProvider.generateToken(authentication);
            return AuthResponse.of(token, UserResponse.fromUser(user));
        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(request.getEmail());
            throw e;
        }
    }
}
