package com.mk.jwtauth.dto;

import lombok.Data;

@Data
public class MfaCodeRequest {
    private String code;
}