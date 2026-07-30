package com.mk.jwtauth.repository;

import com.mk.jwtauth.entity.Message;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, ObjectId> {

    List<Message> findByConversationIdOrderByCreatedAtDesc(ObjectId conversationId);
}