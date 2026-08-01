package com.mk.jwtauth.repository;

import com.mk.jwtauth.entity.Conversation;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, ObjectId> {

    @Query("{ 'participants': { '$all': [?0, ?1] } }")
    Optional<Conversation> findByParticipantsContainingAndParticipantsContaining(
            ObjectId u1, ObjectId u2
    );

    List<Conversation> findByParticipantsContaining(ObjectId userId);
}
