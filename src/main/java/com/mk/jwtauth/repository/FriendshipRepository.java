package com.mk.jwtauth.repository;

import com.mk.jwtauth.entity.Friendship;
import com.mk.jwtauth.entity.FriendshipStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;

public interface FriendshipRepository extends MongoRepository<Friendship, ObjectId> {

    Optional<Friendship> findBySenderIdAndReceiverId(ObjectId senderId, ObjectId receiverId);

    Optional<Friendship> findBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
            ObjectId s1, ObjectId r1,
            ObjectId s2, ObjectId r2
    );

    List<Friendship> findByReceiverIdAndStatus(ObjectId receiverId, FriendshipStatus status);

    List<Friendship> findBySenderIdOrReceiverId(ObjectId senderId, ObjectId receiverId);

    List<Friendship> findBySenderId(ObjectId senderId);
}