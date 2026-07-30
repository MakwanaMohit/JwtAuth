package com.mk.jwtauth.controller;


import com.mk.jwtauth.dto.LoginRequest;
import com.mk.jwtauth.dto.LoginResponse;
import com.mk.jwtauth.dto.SignupRequest;
import com.mk.jwtauth.dto.SignupResponse;
import com.mk.jwtauth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
    @GetMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(authService.signup(signupRequest));
    }

}
