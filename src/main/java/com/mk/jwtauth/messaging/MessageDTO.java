package com.mk.jwtauth.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageDTO {

    private String senderId;
    private String content;
    private String createdAt;
}
