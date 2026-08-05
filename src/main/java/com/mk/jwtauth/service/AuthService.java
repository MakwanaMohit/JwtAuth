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

import dev.samstevens.totp.code.CodeVerifier;
import com.mk.jwtauth.dto.ChangePasswordRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class AuthService {
    public static final String TOKEN_TYPE = "token-type";
    private final JWTUtilis jwtUtilis;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodeVerifier codeVerifier;

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
        return new LoginResponse(token, user.getId(), user.getUsername(), t, Boolean.TRUE.equals(user.getMfaEnabled()));
    }

    public LoginResponse refresh(String refreshToken) {
        return refresh(refreshToken, null);
    }

    public LoginResponse refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Refresh token is missing");
        }

        TokenData tokenData;
        try {
            tokenData = jwtUtilis.getTokenData(refreshToken);
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Refresh token expired or invalid");
        }

        if (tokenData.getTokenType() != TokenType.TOKEN_REFRESH) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid refresh token type");
        }

        User user = userRepository.findByUsername(tokenData.getUsername())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));

        String newAccessToken = jwtUtilis.getToken(user, TOKEN_LOGIN);

        if (response != null) {
            String newRefreshToken = jwtUtilis.getToken(user, TokenType.TOKEN_REFRESH);
            setRefreshTokenCookie(response, newRefreshToken);
        }

        return new LoginResponse(newAccessToken, user.getId(), user.getUsername(), TOKEN_LOGIN, Boolean.TRUE.equals(user.getMfaEnabled()));
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .maxAge(0)
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

    public LoginResponse changePassword(String username, String refreshToken, ChangePasswordRequest request, HttpServletResponse response) {
        // 1. Verify Refresh Token First
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Valid refresh token cookie required to change password");
        }

        TokenData tokenData;
        try {
            tokenData = jwtUtilis.getTokenData(refreshToken);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        if (tokenData.getTokenType() != TokenType.TOKEN_REFRESH || !username.equals(tokenData.getUsername())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token for current user");
        }

        // 2. Fetch User Entity
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 6 characters");
        }

        // 3. Verification Factor: MFA TOTP Code OR Current Password
        boolean verified = false;

        if (request.getMfaCode() != null && !request.getMfaCode().trim().isEmpty()) {
            if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "2FA is not enabled on your account");
            }
            if (!codeVerifier.isValidCode(user.getMfaSecret(), request.getMfaCode().trim())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid 2FA TOTP code");
            }
            verified = true;
        } else if (request.getCurrentPassword() != null && !request.getCurrentPassword().trim().isEmpty()) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
            }
            verified = true;
        }

        if (!verified) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either current password or 2FA code is required to change password");
        }

        // 4. Update Password
        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        userRepository.save(user);

        // 5. Issue new access token and renew refresh token cookie
        String newAccessToken = jwtUtilis.getToken(user, TOKEN_LOGIN);
        if (response != null) {
            String newRefreshToken = jwtUtilis.getToken(user, TokenType.TOKEN_REFRESH);
            setRefreshTokenCookie(response, newRefreshToken);
        }

        return new LoginResponse(newAccessToken, user.getId(), user.getUsername(), TOKEN_LOGIN, Boolean.TRUE.equals(user.getMfaEnabled()));
    }
}
