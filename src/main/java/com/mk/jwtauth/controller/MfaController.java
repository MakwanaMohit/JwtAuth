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

@RestController
@RequestMapping("/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;

    // ✅ Setup
    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setup(Authentication auth) {

        String uri = mfaService.setup(auth.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MfaSetupResponse(uri));
    }

    // ✅ Enable
    @PostMapping("/enable")
    public ResponseEntity<MessageResponse> enable(
            Authentication auth,
            @RequestBody MfaCodeRequest request) {

        mfaService.enable(auth.getName(), request.getCode());

        return ResponseEntity.ok(
                new MessageResponse("MFA enabled successfully")
        );
    }

    // ✅ Verify
    @PostMapping("/verify")
    public ResponseEntity<LoginResponse> verify(
            Authentication auth,
            @RequestBody MfaCodeRequest request) {

        LoginResponse response =
                mfaService.verify(auth.getName(), request.getCode());

        return ResponseEntity.ok(response);
    }

    // ✅ Disable
    @PostMapping("/disable")
    public ResponseEntity<MessageResponse> disable(
            Authentication auth,
            @RequestBody MfaCodeRequest request) {

        mfaService.disable(auth.getName(), request.getCode());

        return ResponseEntity.ok(
                new MessageResponse("MFA disabled successfully")
        );
    }
}