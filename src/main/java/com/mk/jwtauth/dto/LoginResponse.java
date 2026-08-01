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
    public String username;
    public TokenType tokenType;
    public Boolean mfaEnabled;

    public LoginResponse(String token, ObjectId userid, String username, TokenType tokenType) {
        this.token = token;
        this.userid = userid;
        this.username = username;
        this.tokenType = tokenType;
        this.mfaEnabled = false;
    }
}
