package com.example.clpro.controller;

import com.example.clpro.config.jwt.JwtResponse;
import com.example.clpro.config.jwt.JwtTokenUtils;
import com.example.clpro.entities.dto.AuthDto;
import com.example.clpro.entities.model.Auth;
import com.example.clpro.entities.request.*;
import com.example.clpro.entities.response.ApiResponse;
import com.example.clpro.repository.UserRepository;
import com.example.clpro.service.interfaces.AuthService;
import com.example.clpro.service.interfaces.OtpService;
import com.example.clpro.service.interfaces.TelegramSendService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1/auth")
@CrossOrigin
public class AuthController {
    private final AuthService authService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtils tokenUtils;
    private final TelegramSendService telegramSendService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, OtpService otpService, AuthenticationManager authenticationManager, JwtTokenUtils tokenUtils, TelegramSendService telegramSendService, UserRepository userRepository) {
        this.authService = authService;
        this.otpService = otpService;
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.telegramSendService = telegramSendService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody AuthRequest authRequest) throws MessagingException, UnsupportedEncodingException {
        authService.saveUser(authRequest);
        return new ResponseEntity<>(new ApiResponse<>(
                "Registration successful. Please check your email to verify your account.",
                    null,
                    LocalDateTime.now()
        ),HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody LoginRequest authRequest) throws Exception {
        try {
            authenticate(authRequest.getUsername(), authRequest.getPassword());
        } catch (Exception e) {
//            e.printStackTrace();
            throw new IllegalArgumentException("Invalid username or password");
        }

        final UserDetails userDetails = authService.loadUserByUsername(authRequest.getUsername());
        final String token = tokenUtils.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(LocalDateTime.now(), authRequest.getUsername(), token));
    }

    @PostMapping("/send-otp-bot")
    public ResponseEntity<?> sendOtpViaBot(@RequestParam String username) {
        telegramSendService.sendOtp(username);
        return ResponseEntity.ok(new ApiResponse<>(
                "OTP via telegram bot sent successfully",
                null,
                LocalDateTime.now()
        ));
    }

    @PostMapping("/send-otp-email")
    public ResponseEntity<?> sendOtpViaEmail(@RequestParam String email) throws MessagingException, UnsupportedEncodingException {
        authService.sendOtpViaEmail(email);
        return ResponseEntity.ok(new ApiResponse<>(
                "OTP via email sent successfully",
                null,
                LocalDateTime.now()
        ));
    }


    @PatchMapping("/{username}/telegramId")
    public ResponseEntity<?> updateTelegramId(@PathVariable String username, @RequestBody TelegramIdRequest telegramIdRequest) {
        AuthDto updatedAuth = authService.updateTelegramId(username, telegramIdRequest.getTelegramId());
        return ResponseEntity.ok(new ApiResponse<>(
                "Telegram ID updated successfully",
                updatedAuth,
                LocalDateTime.now()
        ));
    }
    @PostMapping("/validate-otp")
    public ResponseEntity<?> validateOtp(@RequestBody OtpRequest otpRequest) {
        boolean isValid = otpService.validateOtp(otpRequest.getUsername(), otpRequest.getOtp());
        if (isValid) {
//            return ResponseEntity.ok("OTP validated successfully.");
            final UserDetails userDetails = authService.loadUserByUsername(otpRequest.getUsername());
            final String token = tokenUtils.generateToken(userDetails);
            return ResponseEntity.ok(new JwtResponse(LocalDateTime.now(), otpRequest.getUsername(), token));
        } else {
            throw new IllegalArgumentException("Invalid OTP code");
        }
    }

    @PostMapping("/validate-otp-email")
    public ResponseEntity<?> validateOtpEmail(@RequestBody OtpRequestEmail otpRequestEmail) {
        boolean isValid = otpService.validateOtpEmail(otpRequestEmail.getEmail(), otpRequestEmail.getOtp());
        if (isValid) {
            Auth auth = userRepository.findUserByEmail(otpRequestEmail.getEmail());
            if (auth != null) {
                final UserDetails userDetails = authService.loadUserByUsername(auth.getUsername());
                final String token = tokenUtils.generateToken(userDetails);
                return ResponseEntity.ok(new JwtResponse(LocalDateTime.now(), auth.getUsername(), token));
            } else {
                throw new IllegalArgumentException("User not found with given email");
            }

        } else {
            throw new IllegalArgumentException("Invalid OTP code");
        }
    }

    @PostMapping("/verified-otp")
    public void verifiedEmailByOtp(@RequestBody OtpRequestEmail otpRequestEmail) {
        boolean isValid = otpService.validateOtpEmail(otpRequestEmail.getEmail(), otpRequestEmail.getOtp());
        if (isValid) {
            Auth auth = userRepository.findUserByEmail(otpRequestEmail.getEmail());
            if (auth != null) {
                authService.verifiedEmailByOtp(auth.getEmail());
            } else {
                throw new IllegalArgumentException("User not found with given email");
            }

        } else {
            throw new IllegalArgumentException("Invalid OTP code");
        }
    }

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@RequestParam String email, @RequestParam String name, @RequestParam String msg) throws MessagingException, UnsupportedEncodingException {
        authService.sendEmail(email, name, msg);
        return ResponseEntity.ok(new ApiResponse<>(
                "Email sent successfully",
                null,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        boolean isVerified = authService.verifyEmailToken(token);
        if (isVerified) {
            response.sendRedirect("http://localhost:3000/email-verified");
        } else {
            response.sendRedirect("http://localhost:3000/invalid-token");
        }
    }




    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            System.out.println("Check USER_DISABLED: " + e);
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            System.out.println("Check Error: " + e);
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
