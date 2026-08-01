package com.mk.jwtauth.utilis;

import com.mk.jwtauth.dto.TokenData;
import com.mk.jwtauth.entity.User;
import com.mk.jwtauth.service.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static com.mk.jwtauth.service.TokenType.TOKEN_TEMPORARY;
import static com.mk.jwtauth.service.TokenType.TOKEN_TYPE_KEY;

@Component
public class JWTUtilis {
    @Value("${jwt.secretkey}")
    private String secretkey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretkey.getBytes(StandardCharsets.UTF_8));
    }

    public String getToken(User user, TokenType type) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("email", user.getEmail());
        claims.put(TOKEN_TYPE_KEY, type);

        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        claims.put("authorities", roles);

        long now = System.currentTimeMillis();
        long expiration;
        if (type == TOKEN_TEMPORARY) {
            expiration = now + (5 * 60 * 1000);        // TEMP token → 5 min
        } else if (type == TokenType.TOKEN_REFRESH) {
            expiration = now + (7L * 24 * 60 * 60 * 1000); // REFRESH token → 7 days
        } else {
            expiration = now + (15 * 60 * 1000);       // LOGIN token → 15 min
        }

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .signWith(getSecretKey())
                .compact();
    }
    public TokenData getTokenData(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        String tokenTypeStr = claims.get(TOKEN_TYPE_KEY, String.class);
        TokenType tokenType = TokenType.valueOf(tokenTypeStr);

        List<?> rawList = claims.get("authorities", List.class);

        List<GrantedAuthority> authorities = rawList.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new TokenData(username, tokenType, authorities);
    }
}
