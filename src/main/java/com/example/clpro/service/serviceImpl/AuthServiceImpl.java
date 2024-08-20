package com.example.clpro.service.serviceImpl;

import com.example.clpro.entities.dto.AuthDto;
import com.example.clpro.entities.model.Auth;
import com.example.clpro.entities.model.VerificationToken;
import com.example.clpro.entities.request.AuthRequest;
import com.example.clpro.exception.CustomErrorResponse;
import com.example.clpro.exception.InternalServerErrorException;
import com.example.clpro.exception.NotFoundExceptionClass;
import com.example.clpro.repository.AuthRepository;
import com.example.clpro.repository.TokenRepository;
import com.example.clpro.repository.UserRepository;
import com.example.clpro.service.interfaces.AuthService;
import com.example.clpro.service.interfaces.EmailService;
import com.example.clpro.service.interfaces.OtpService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;

    public AuthServiceImpl(AuthRepository authRepository, UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, EmailService emailService, OtpService otpService) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpService = otpService;
    }
    @Override
    public void saveUser(AuthRequest authRequest) throws MessagingException, UnsupportedEncodingException {
        if (authRequest == null) {
            throw new IllegalArgumentException("AuthRequest cannot be null");
        } else {
            Boolean username = userRepository.existsByUsername(authRequest.getUsername());
            Boolean email = userRepository.existsByEmail(authRequest.getEmail());
            if (username) {
                throw new InternalServerErrorException(
                        "Username already exists"
                );
            } else if(email) {
                throw new InternalServerErrorException(
                        "Email already exists"
                );
            } else {
                authRequest.setPassword(passwordEncoder.encode(authRequest.getPassword()));
                authRepository.save(authRequest.toEntity("user"));
                String verificationToken = UUID.randomUUID().toString();
                saveVerificationToken(authRequest.getUsername(), verificationToken);

                String otp = otpService.generateOtpEmail(authRequest.getEmail());
                String verificationUrl = "http://localhost:8080/api/v1/auth/verify-email?token=" + verificationToken;
                emailService.sendVerificationEmail(authRequest.getEmail(), verificationUrl, otp);
            }
        }

    }

    @Override
    public AuthDto updateTelegramId(String username, String telegramId) {
        Auth auth = userRepository.findUserByUsername(username);
        if (auth == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        auth.setTelegramId(telegramId);
        return authRepository.save(auth).toDto();
    }

    @Override
    public void sendEmail(String email, String name, String msg) throws MessagingException, UnsupportedEncodingException {
        emailService.sendRegistrationInvitation(email, name, msg);
    }

    @Override
    public boolean verifyEmailToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isPresent()) {
            VerificationToken verificationToken = optionalToken.get();
            Auth auth = verificationToken.getAuth();
            auth.setEnabled(true);
            authRepository.save(auth);
            tokenRepository.delete(verificationToken);
            return true;
        }
        return false;
    }

    @Override
    public void sendOtpViaEmail(String email) throws MessagingException, UnsupportedEncodingException {
        Auth auth = userRepository.findUserByEmail(email);
        if (auth != null) {
            String otp = otpService.generateOtpEmail(email);
            emailService.sendOtpToEmail(email, otp);
        } else {
            throw new NotFoundExceptionClass("User not found with " + email);
        }
    }

    @Override
    public void verifiedEmailByOtp(String email) {
        Auth auth = userRepository.findUserByEmail(email);
        auth.setEnabled(true);
        authRepository.save(auth);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = authRepository.findByUsername(username);
        System.out.println(userDetails);
        return userDetails;
    }

    public void saveVerificationToken(String username, String token) {
        Auth auth = authRepository.findByUsernameReturnAuth(username);
        VerificationToken verificationToken = new VerificationToken(token, auth);
        tokenRepository.save(verificationToken);
    }
}
