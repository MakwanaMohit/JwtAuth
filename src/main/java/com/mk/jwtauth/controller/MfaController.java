package com.mk.jwtauth.controller;

import com.mk.jwtauth.dto.LoginResponse;
import com.mk.jwtauth.dto.MessageResponse;
import com.mk.jwtauth.dto.MfaCodeRequest;
import com.mk.jwtauth.dto.MfaSetupResponse;
import com.mk.jwtauth.service.MfaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;

    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setup(Authentication auth) {

        String uri = mfaService.setup(auth.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MfaSetupResponse(uri));
    }

    @PostMapping("/enable")
    public ResponseEntity<MessageResponse> enable(
            Authentication auth,
            @RequestBody MfaCodeRequest request) {

        mfaService.enable(auth.getName(), request.getCode());

        return ResponseEntity.ok(
                new MessageResponse("MFA enabled successfully")
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<LoginResponse> verify(
            Authentication auth,
            @RequestBody MfaCodeRequest request,
            HttpServletResponse response) {

        LoginResponse resp =
                mfaService.verify(auth.getName(), request.getCode(), response);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/disable")
    public ResponseEntity<MessageResponse> disable(
            Authentication auth,
            @RequestBody(required = false) MfaCodeRequest request) {

        String code = (request != null) ? request.getCode() : null;
        mfaService.disable(auth.getName(), code);

        return ResponseEntity.ok(
                new MessageResponse("MFA disabled successfully")
        );
    }
}