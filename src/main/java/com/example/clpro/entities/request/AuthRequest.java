package com.example.clpro.entities.request;

import com.example.clpro.entities.model.Auth;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"username", "password", "email", "profile"})
public class AuthRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String profile;

    public Auth toEntity(String role) {
        Auth auth = new Auth();
        auth.setUsername(this.username);
        auth.setPassword(this.password);
        auth.setEmail(this.email);
        auth.setProfile(this.profile);
        auth.setTelegramId(null);
        auth.setRole(role);
        auth.setEnabled(false);
        auth.setOtp(null);
        auth.setOtpExpiration(null);
        auth.setVerificationToken(null);
        auth.setVerificationTokenExpiration(null);
        return auth;
    }
}
