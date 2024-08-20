package com.example.clpro.entities.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "verification_tokens")
public class VerificationToken {
    @Id
    private String id;
    private String token;
    private Auth auth;
    private LocalDateTime expiryDate;
    public VerificationToken(String token, Auth auth) {
        this.token = token;
        this.auth = auth;
        this.expiryDate = LocalDateTime.now().plusDays(1);
    }
}
