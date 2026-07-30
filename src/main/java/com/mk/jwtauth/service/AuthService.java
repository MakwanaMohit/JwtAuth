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
        }
        return new LoginResponse(token, user.getId(),t);
    }

    public SignupResponse signup(SignupRequest signupRequest) {
        User user = userRepository.findByUsername(signupRequest.getUsername()).orElse(null);
        if (user != null) throw new IllegalArgumentException("Username is already in use");
        user = userRepository.save(new User(signupRequest.getUsername(),passwordEncoder.encode(signupRequest.getPassword()), signupRequest.getEmail()));
        return new SignupResponse(user.getId(),user.getUsername());
    }
}
