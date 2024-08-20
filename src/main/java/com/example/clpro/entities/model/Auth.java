package com.example.clpro.entities.model;

import com.example.clpro.entities.dto.AuthDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class Auth implements UserDetails {
    @Id
    private String id;
    private String username;
    private String password;
    private String profile;
    private String email;
    private String telegramId;
    private String role;
    private boolean enabled;
    private String otp;
    private LocalDateTime otpExpiration;
    private String verificationToken;
    private LocalDateTime verificationTokenExpiration;

    public AuthDto toDto() {
        return new AuthDto(this.id, this.username, this.profile, this.email, this.telegramId, this.role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
