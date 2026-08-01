package com.mk.jwtauth.service;

import com.mk.jwtauth.dto.LoginRequest;
import com.mk.jwtauth.dto.LoginResponse;
import com.mk.jwtauth.dto.SignupRequest;
import com.mk.jwtauth.dto.SignupResponse;
import com.mk.jwtauth.entity.User;
import com.mk.jwtauth.repository.UserRepository;
import com.mk.jwtauth.utilis.JWTUtilis;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import com.mk.jwtauth.dto.TokenData;
import static com.mk.jwtauth.service.TokenType.TOKEN_LOGIN;
import static com.mk.jwtauth.service.TokenType.TOKEN_TEMPORARY;

@Service
@RequiredArgsConstructor
public class AuthService {
    public static final String TOKEN_TYPE = "token-type";
    private final JWTUtilis jwtUtilis;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(@NonNull LoginRequest loginRequest) {
        return login(loginRequest, null);
    }

    public LoginResponse login(@NonNull LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        User user = (User) authentication.getPrincipal();
        String token;
        TokenType t = TOKEN_LOGIN;
        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            token = jwtUtilis.getToken(user, TOKEN_TEMPORARY);
            t = TOKEN_TEMPORARY;
        } else {
            token = jwtUtilis.getToken(user, TOKEN_LOGIN);
            if (response != null) {
                String refreshToken = jwtUtilis.getToken(user, TokenType.TOKEN_REFRESH);
                setRefreshTokenCookie(response, refreshToken);
            }
        }
        return new LoginResponse(token, user.getId(), t);
    }

    public LoginResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is missing");
        }

        TokenData tokenData = jwtUtilis.getTokenData(refreshToken);
        if (tokenData.getTokenType() != TokenType.TOKEN_REFRESH) {
            throw new IllegalArgumentException("Invalid refresh token type");
        }

        User user = userRepository.findByUsername(tokenData.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtUtilis.getToken(user, TOKEN_LOGIN);
        return new LoginResponse(newAccessToken, user.getId(), TOKEN_LOGIN);
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public SignupResponse signup(SignupRequest signupRequest) {
        User user = userRepository.findByUsername(signupRequest.getUsername()).orElse(null);
        if (user != null) throw new IllegalArgumentException("Username is already in use");
        user = userRepository.save(new User(signupRequest.getUsername(),passwordEncoder.encode(signupRequest.getPassword()), signupRequest.getEmail()));
        return new SignupResponse(user.getId(),user.getUsername());
    }
}
