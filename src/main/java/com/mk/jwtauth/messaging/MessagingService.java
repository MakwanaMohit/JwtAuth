package com.mk.jwtauth.messaging;

import com.mk.jwtauth.entity.*;
import com.mk.jwtauth.repository.ConversationRepository;
import com.mk.jwtauth.repository.FriendshipRepository;
import com.mk.jwtauth.repository.MessageRepository;
import com.mk.jwtauth.repository.UserRepository;
import com.mk.jwtauth.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final ConversationRepository convoRepo;
    private final MessageRepository messageRepo;
    private final FriendshipRepository friendshipRepo;
    private final AuthUtil authUtil;
    private final UserRepository userRepo;

    // 🔹 Check send permission
    private boolean canSend(ObjectId u1, ObjectId u2) {

        var fOpt = friendshipRepo.findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
                u1, u2, u2, u1
        );

        if (fOpt.isEmpty()) return false;

        Friendship f = fOpt.get();

        return f.getStatus() == FriendshipStatus.APPROVED;
    }

    public void sendMessage(SendMessageDTO dto) {

        User sender = authUtil.getCurrentUser();
        ObjectId senderId = sender.getId();
        ObjectId receiverId = new ObjectId(dto.getReceiverId());

        if (!canSend(senderId, receiverId)) {
            throw new IllegalStateException("You cannot send message to this user");
        }

        Conversation convo = convoRepo
                .findByParticipantsContainingAndParticipantsContaining(senderId, receiverId)
                .orElseGet(() -> convoRepo.save(
                        Conversation.builder()
                                .participants(List.of(senderId, receiverId))
                                .build()
                ));

        Message msg = Message.builder()
                .conversationId(convo.getId())
                .senderId(senderId)
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        messageRepo.save(msg);

        convo.setLastMessage(dto.getContent());
        convo.setLastMessageTime(LocalDateTime.now());
        convoRepo.save(convo);
    }

    public List<ConversationDTO> getConversations() {

        User me = authUtil.getCurrentUser();
        ObjectId myId = me.getId();

        return convoRepo.findByParticipantsContaining(myId)
                .stream()
                .map(c -> {

                    ObjectId otherId = c.getParticipants()
                            .stream()
                            .filter(id -> !id.equals(myId))
                            .findFirst()
                            .orElseThrow();

                    User other = userRepo.findById(otherId).orElseThrow();

                    boolean canSend = canSend(myId, otherId);

                    return ConversationDTO.builder()
                            .conversationId(c.getId().toHexString())
                            .userId(otherId.toHexString())
                            .username(other.getUsername())
                            .lastMessage(c.getLastMessage())
                            .canSend(canSend)
                            .build();
                })
                .toList();
    }

    public List<MessageDTO> getMessages(String conversationId) {

        ObjectId convoId = new ObjectId(conversationId);

        return messageRepo.findByConversationIdOrderByCreatedAtDesc(convoId)
                .stream()
                .map(m -> MessageDTO.builder()
                        .senderId(m.getSenderId().toHexString())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt().toString())
                        .build())
                .toList();
    }
}
