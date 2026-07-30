package com.mk.jwtauth.repository;

import com.mk.jwtauth.entity.Conversation;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, ObjectId> {

    Optional<Conversation> findByParticipantsContainingAndParticipantsContaining(
            ObjectId u1, ObjectId u2
    );

    List<Conversation> findByParticipantsContaining(ObjectId userId);
}
