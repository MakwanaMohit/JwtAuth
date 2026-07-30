package com.mk.jwtauth.dto;

import com.mk.jwtauth.entity.FriendshipStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActionRequestDTO {

    @NotBlank(message = "senderId is required")
    private String senderId;

    @NotNull(message = "status is required")
    private FriendshipStatus status;
}
