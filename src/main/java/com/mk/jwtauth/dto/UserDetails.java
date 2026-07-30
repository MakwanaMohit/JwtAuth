package com.mk.jwtauth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetails {
    private String userId;
    private String username;
}