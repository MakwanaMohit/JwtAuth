package com.mk.jwtauth.controller;


import com.mk.jwtauth.dto.LoginRequest;
import com.mk.jwtauth.dto.LoginResponse;
import com.mk.jwtauth.dto.SignupRequest;
import com.mk.jwtauth.dto.SignupResponse;
import com.mk.jwtauth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginPost(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @GetMapping("/login")
    public ResponseEntity<LoginResponse> loginGet(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signupPost(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authService.signup(signupRequest));
    }

    @GetMapping("/signup")
    public ResponseEntity<SignupResponse> signupGet(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authService.signup(signupRequest));
    }

    @RequestMapping(value = "/refresh", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletRequest request) {

        String token = refreshTokenCookie;
        if (token == null || token.isBlank()) {
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("refreshToken".equals(c.getName())) {
                        token = c.getValue();
                        break;
                    }
                }
            }
        }

        return ResponseEntity.ok(authService.refresh(token));
    }
}
