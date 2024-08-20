package com.example.clpro.service.interfaces;

import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService {
    void sendRegistrationInvitation(String email, String name, String msg) throws MessagingException, UnsupportedEncodingException;
    void sendVerificationEmail(String email,String verificationUrl, String otp) throws MessagingException, UnsupportedEncodingException;

    void sendOtpToEmail(String email, String otp) throws MessagingException, UnsupportedEncodingException;
}
