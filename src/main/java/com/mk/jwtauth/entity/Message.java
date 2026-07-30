package com.mk.jwtauth.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    private ObjectId id;

    @Indexed
    private ObjectId conversationId;

    private ObjectId senderId;

    private String content;

    private LocalDateTime createdAt;
}