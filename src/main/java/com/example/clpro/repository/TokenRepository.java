package com.example.clpro.repository;

import com.example.clpro.entities.model.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends MongoRepository<VerificationToken, String> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByExpiryDateBefore(LocalDateTime now);
}
