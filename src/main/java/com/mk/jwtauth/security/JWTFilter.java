package com.mk.jwtauth.security;

import com.mk.jwtauth.dto.TokenData;
import com.mk.jwtauth.entity.User;
import com.mk.jwtauth.repository.UserRepository;
import com.mk.jwtauth.utilis.JWTUtilis;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtilis jwtUtilis;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request,response);
            return;
        }
        String token = requestTokenHeader.replace("Bearer ", "");
        TokenData data = jwtUtilis.getTokenData(token);
        String username = data.getUsername();

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            JwtAuthenticationToken auth =
                    new JwtAuthenticationToken(
                            data.getUsername(),
                            null,
                            data.getAuthorities(),
                            data.getTokenType()
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request,response);
    }
}
