package com.mk.jwtauth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendRequest {
    private String userId;
    private String username;
    private String status;
}