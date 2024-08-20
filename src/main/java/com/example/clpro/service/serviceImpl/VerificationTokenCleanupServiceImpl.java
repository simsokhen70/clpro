package com.example.clpro.service.serviceImpl;

import com.example.clpro.repository.TokenRepository;
import com.example.clpro.service.interfaces.VerificationTokenCleanupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationTokenCleanupServiceImpl implements VerificationTokenCleanupService {
    private final TokenRepository tokenRepository;

    public VerificationTokenCleanupServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(fixedRate = 60000)  // Runs every 60 seconds
    @Override
    public void cleanupExpiredTokens() {
        System.out.println("cleanupExpiredTokens works");
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteByExpiryDateBefore(now);
    }
}
