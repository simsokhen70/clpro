package com.example.clpro.service.interfaces;

public interface OtpService {

    String generateOtp(String username);

    String generateOtpEmail(String email);

    boolean validateOtp(String username, String otp);
    boolean validateOtpEmail(String email, String otp);

    String generateEmailToken(String email);

    String decodeEmailToken(String token);

    boolean validateEmailToken(String token, String email);
}
