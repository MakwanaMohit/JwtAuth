package com.mk.jwtauth.entity;

import lombok.*;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Document("app-user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User implements UserDetails {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    @NonNull
    private String username;

    private String password;
    private String email;

    private Boolean mfaEnabled;
    private String mfaSecret;

    // Custom constructor with defaults
    public User(@NonNull String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.mfaEnabled = false;
        this.mfaSecret = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}