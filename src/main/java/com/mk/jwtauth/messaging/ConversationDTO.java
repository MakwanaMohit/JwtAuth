package com.mk.jwtauth.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationDTO {

    private String conversationId;
    private String userId;
    private String username;

    private String lastMessage;
    private boolean canSend;
}
