package com.mk.jwtauth.dto;

import com.mk.jwtauth.service.TokenType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    public String token;
    public ObjectId userid;
    public TokenType tokenType;
}
