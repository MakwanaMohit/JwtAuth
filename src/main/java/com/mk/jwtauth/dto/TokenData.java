package com.mk.jwtauth.dto;
import com.mk.jwtauth.service.TokenType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenData {
    private String username;
    private TokenType tokenType;
    private List<GrantedAuthority> authorities;
}