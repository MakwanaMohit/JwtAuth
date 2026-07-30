package com.mk.jwtauth.controller;

import com.mk.jwtauth.dto.*;
import com.mk.jwtauth.service.FriendshipService;
import com.mk.jwtauth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService service;
    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDetails>>> getAllUsers() {

        return ResponseEntity.ok(
                ApiResponse.<List<UserDetails>>builder()
                        .success(true)
                        .message("Users fetched successfully")
                        .data(userService.getAllUsers())
                        .build()
        );
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> send(@Valid @RequestBody SendRequestDTO dto) {

        service.sendRequest(dto.getReceiverId());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Friend request sent successfully")
                        .build()
        );
    }

    @PostMapping("/request/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@Valid @RequestBody SendRequestDTO dto) {

        service.cancelRequest(dto.getReceiverId());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Friend request cancelled successfully")
                        .build()
        );
    }
    @GetMapping("/requests/sent")
    public ResponseEntity<ApiResponse<List<SentRequestDTO>>> sentRequests() {

        return ResponseEntity.ok(
                ApiResponse.<List<SentRequestDTO>>builder()
                        .success(true)
                        .message("Sent requests fetched successfully")
                        .data(service.getSentRequests())
                        .build()
        );
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FriendRequest>>> requests() {

        return ResponseEntity.ok(
                ApiResponse.<List<FriendRequest>>builder()
                        .success(true)
                        .message("Pending requests fetched successfully")
                        .data(service.getPendingRequests())
                        .build()
        );
    }


    @PostMapping("/request/action")
    public ResponseEntity<ApiResponse<Void>> action(@Valid @RequestBody ActionRequestDTO dto) {

        service.handleRequest(dto.getSenderId(), dto.getStatus());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Request action performed successfully")
                        .build()
        );
    }
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<UserDetails>>> getFriends() {

        return ResponseEntity.ok(
                ApiResponse.<List<UserDetails>>builder()
                        .success(true)
                        .message("Friends fetched successfully")
                        .data(service.getFriends())
                        .build()
        );
    }

    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<Void>> remove(@Valid @RequestBody RemoveFriendDTO dto) {

        service.removeFriend(dto.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Friend removed successfully")
                        .build()
        );
    }
}