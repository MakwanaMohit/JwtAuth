package com.mk.jwtauth.service;

import com.mk.jwtauth.dto.FriendRequest;
import com.mk.jwtauth.dto.SentRequestDTO;
import com.mk.jwtauth.dto.UserDetails;
import com.mk.jwtauth.entity.Friendship;
import com.mk.jwtauth.entity.FriendshipStatus;
import com.mk.jwtauth.entity.User;
import com.mk.jwtauth.repository.FriendshipRepository;
import com.mk.jwtauth.repository.UserRepository;
import com.mk.jwtauth.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository repo;
    private final UserRepository userRepo;
    private final AuthUtil authUtil;

    // 🔹 Send Request
    public void sendRequest(String receiverIdStr) {

        User sender = authUtil.getCurrentUser();
        ObjectId senderId = sender.getId();
        ObjectId receiverId = new ObjectId(receiverIdStr);

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send request to yourself");
        }

        User receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        var existing = repo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
                senderId, receiverId,
                receiverId, senderId
        );

        if (existing.isPresent()) {
            Friendship f = existing.get();

            switch (f.getStatus()) {
                case APPROVED -> { return; }
                case BLOCKED -> throw new RuntimeException("User is blocked");
                case ASKED -> throw new RuntimeException("Request already pending");
                case REJECTED -> {
                    f.setSenderId(senderId);
                    f.setReceiverId(receiverId);
                    f.setSenderUsername(sender.getUsername());
                    f.setReceiverUsername(receiver.getUsername());
                    f.setStatus(FriendshipStatus.ASKED);
                    f.setUpdatedAt(LocalDateTime.now());
                    repo.save(f);
                    return;
                }
            }
        }

        Friendship f = Friendship.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .senderUsername(sender.getUsername())
                .receiverUsername(receiver.getUsername())
                .status(FriendshipStatus.ASKED)
                .createdAt(LocalDateTime.now())
                .build();

        repo.save(f);
    }

    // 🔹 Cancel Request
    public void cancelRequest(String receiverIdStr) {

        User sender = authUtil.getCurrentUser();
        ObjectId receiverId = new ObjectId(receiverIdStr);

        Friendship f = repo.findBySenderIdAndReceiverId(sender.getId(), receiverId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (f.getStatus() == FriendshipStatus.ASKED ||
                f.getStatus() == FriendshipStatus.REJECTED) {
            repo.delete(f);
        }
    }

    // 🔹 Get Requests
    public List<FriendRequest> getPendingRequests() {

        User me = authUtil.getCurrentUser();

        return repo.findByReceiverIdAndStatus(me.getId(), FriendshipStatus.ASKED)
                .stream()
                .map(f -> FriendRequest.builder()
                        .userId(f.getSenderId().toHexString())
                        .username(f.getSenderUsername())
                        .status(f.getStatus().name())
                        .build())
                .toList();
    }

    // 🔹 Handle Request
    public void handleRequest(String senderIdStr, FriendshipStatus action) {

        User me = authUtil.getCurrentUser();
        ObjectId senderId = new ObjectId(senderIdStr);

        Friendship f = repo.findBySenderIdAndReceiverId(senderId, me.getId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        switch (action) {
            case APPROVED -> f.setStatus(FriendshipStatus.APPROVED);
            case REJECTED -> f.setStatus(FriendshipStatus.REJECTED);
            case BLOCKED -> f.setStatus(FriendshipStatus.BLOCKED);
            default -> throw new IllegalArgumentException("Invalid action");
        }

        f.setUpdatedAt(LocalDateTime.now());
        repo.save(f);
    }

    public List<SentRequestDTO> getSentRequests() {

        User me = authUtil.getCurrentUser();

        return repo.findBySenderId(me.getId())
                .stream()
                .map(f -> SentRequestDTO.builder()
                        .userId(f.getReceiverId().toHexString())
                        .username(f.getReceiverUsername())
                        .status(f.getStatus().name())
                        .build())
                .toList();
    }

    public boolean canSendMessage(ObjectId user1, ObjectId user2) {

        var friendshipOpt = repo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
                user1, user2,
                user2, user1
        );

        if (friendshipOpt.isEmpty()) {
            return false;
        }

        Friendship f = friendshipOpt.get();

        return f.getStatus() == FriendshipStatus.APPROVED;
    }

    public List<UserDetails> getFriends() {

        User me = authUtil.getCurrentUser();

        return repo.findBySenderIdOrReceiverId(me.getId(), me.getId())
                .stream()
                .filter(f -> f.getStatus() == FriendshipStatus.APPROVED)
                .map(f -> {
                    boolean isSender = f.getSenderId().equals(me.getId());

                    return UserDetails.builder()
                            .userId(isSender
                                    ? f.getReceiverId().toHexString()
                                    : f.getSenderId().toHexString())
                            .username(isSender
                                    ? f.getReceiverUsername()
                                    : f.getSenderUsername())
                            .build();
                })
                .toList();
    }

    // 🔹 Remove Friend
    public void removeFriend(String otherUserIdStr) {

        User me = authUtil.getCurrentUser();
        ObjectId otherId = new ObjectId(otherUserIdStr);

        Friendship f = repo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
                me.getId(), otherId,
                otherId, me.getId()
        ).orElseThrow(() -> new RuntimeException("Friendship not found"));

        if (f.getStatus() == FriendshipStatus.APPROVED) {
            repo.delete(f);
        }
    }
}