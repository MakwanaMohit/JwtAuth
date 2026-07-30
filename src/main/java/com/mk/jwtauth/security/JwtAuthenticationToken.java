package com.mk.jwtauth.security;

import com.mk.jwtauth.service.TokenType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final TokenType tokenType;

    public JwtAuthenticationToken(Object principal,
                                  Object credentials,
                                  Collection<? extends GrantedAuthority> authorities,
                                  TokenType tokenType) {
        super(principal, credentials, authorities);
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }
}