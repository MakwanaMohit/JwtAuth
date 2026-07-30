package com.mk.jwtauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RemoveFriendDTO {

    @NotBlank(message = "userId is required")
    private String userId;
}