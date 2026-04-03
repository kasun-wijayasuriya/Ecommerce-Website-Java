package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String[] nameParts = name != null ? name.split(" ") : new String[]{"", ""};
            // Generate random password for OAuth users (never used for login)
            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
            
            User newUser = User.builder()
                    .email(email)
                    .firstName(nameParts[0])
                    .lastName(nameParts.length > 1 ? nameParts[1] : "")
                    .profileImage(picture)
                    .password(randomPassword)
                    .roles(Set.of(User.Role.USER))
                    .active(true)
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtTokenProvider.generateToken(user.getEmail());

        // Check if request came from localhost (development)
        String referer = request.getHeader("Referer");
        boolean isLocalhost = referer != null && referer.contains("localhost");

        ResponseCookie jwtCookie = ResponseCookie.from("jwt-token", token)
                .httpOnly(true)
                .secure(!isLocalhost) // Only secure for HTTPS (production)
                .path("/")
                .maxAge(jwtExpirationMs / 1000)
                .sameSite("Lax") // Allow OAuth redirect flow
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        
        // Redirect to frontend OAuth success callback
        String frontendUrl = allowedOrigins.split(",")[0];
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth-success");
    }
}
