package com.mk.jwtauth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SendRequestDTO {

    @NotBlank(message = "receiverId is required")
    private String receiverId;
}
