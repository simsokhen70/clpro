package com.example.clpro.service.interfaces;

import com.example.clpro.entities.dto.AuthDto;
import com.example.clpro.entities.request.AuthRequest;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.UnsupportedEncodingException;

@Service
public interface AuthService extends UserDetailsService {
    void saveUser(AuthRequest auth) throws MessagingException, UnsupportedEncodingException;

    AuthDto updateTelegramId(String username, String telegramId);

    void sendEmail(String email, String name, String msg) throws MessagingException, UnsupportedEncodingException;

    boolean verifyEmailToken(String token);

    void sendOtpViaEmail(String email) throws MessagingException, UnsupportedEncodingException;

    void verifiedEmailByOtp(String email);
}
