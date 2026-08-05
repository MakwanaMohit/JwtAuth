package com.mk.jwtauth.service;

import com.mk.jwtauth.dto.LoginResponse;
import com.mk.jwtauth.entity.User;
import com.mk.jwtauth.repository.UserRepository;
import com.mk.jwtauth.utilis.JWTUtilis;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import static com.mk.jwtauth.service.TokenType.TOKEN_LOGIN;

@Service
@RequiredArgsConstructor
public class MfaService {

    @Value("${spring.application.name}")
    private String appName;

    private final UserRepository userRepository;
    private final JWTUtilis jwtUtilis;
    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    public String setup(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String secret = secretGenerator.generate();

        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        userRepository.save(user);

        QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer(appName)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        return data.getUri();
    }

    public void enable(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!codeVerifier.isValidCode(user.getMfaSecret(), code)) {
            throw new IllegalArgumentException("Invalid TOTP code");
        }

        user.setMfaEnabled(true);
        userRepository.save(user);
    }

    public LoginResponse verify(String username, String code) {
        return verify(username, code, null);
    }

    public LoginResponse verify(String username, String code, HttpServletResponse response) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
            throw new IllegalStateException("MFA not enabled");
        }

        if (!codeVerifier.isValidCode(user.getMfaSecret(), code)) {
            throw new IllegalArgumentException("Invalid TOTP code");
        }

        String token = jwtUtilis.getToken(user, TOKEN_LOGIN);

        if (response != null) {
            String refreshToken = jwtUtilis.getToken(user, TokenType.TOKEN_REFRESH);
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/auth")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return new LoginResponse(token, user.getId(), user.getUsername(), TOKEN_LOGIN, true);
    }

    public void disable(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (code != null && !code.isBlank()) {
            if (!codeVerifier.isValidCode(user.getMfaSecret(), code)) {
                throw new IllegalArgumentException("Invalid TOTP code");
            }
        }

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
    }
}