package com.mk.jwtauth.service;

import com.mk.jwtauth.dto.UserDetails;
import com.mk.jwtauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDetails> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> UserDetails.builder()
                        .userId(user.getId().toHexString())
                        .username(user.getUsername())
                        .build())
                .toList();
    }
}
