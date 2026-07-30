package com.mk.jwtauth.messaging;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageDTO {

    @NotBlank
    private String receiverId;

    @NotBlank
    private String content;
}
