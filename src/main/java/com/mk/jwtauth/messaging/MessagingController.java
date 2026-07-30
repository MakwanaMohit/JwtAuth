package com.mk.jwtauth.messaging;


import com.mk.jwtauth.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService service;

    // 🔹 Send Message
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(@Valid @RequestBody SendMessageDTO dto) {

        service.sendMessage(dto);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Message sent successfully")
                        .build()
        );
    }

    // 🔹 Get Conversations
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> conversations() {

        return ResponseEntity.ok(
                ApiResponse.<List<ConversationDTO>>builder()
                        .success(true)
                        .message("Conversations fetched successfully")
                        .data(service.getConversations())
                        .build()
        );
    }

    // 🔹 Get Messages
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> messages(
            @RequestBody String conversationId) {

        return ResponseEntity.ok(
                ApiResponse.<List<MessageDTO>>builder()
                        .success(true)
                        .message("Messages fetched successfully")
                        .data(service.getMessages(conversationId))
                        .build()
        );
    }

}