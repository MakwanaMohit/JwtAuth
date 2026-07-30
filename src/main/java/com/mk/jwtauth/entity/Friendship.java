package com.mk.jwtauth.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import org.bson.types.ObjectId;

@Document(collection = "friendships")
@CompoundIndex(
        name = "user_pair_idx",
        def = "{'senderId': 1, 'receiverId': 1}",
        unique = true
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    private ObjectId id;

    private ObjectId senderId;
    private ObjectId receiverId;

    private String senderUsername;
    private String receiverUsername;

    private FriendshipStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}