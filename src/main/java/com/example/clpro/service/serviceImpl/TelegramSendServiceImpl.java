package com.example.clpro.service.serviceImpl;

import com.example.clpro.entities.model.Auth;
import com.example.clpro.repository.UserRepository;
import com.example.clpro.service.interfaces.TelegramSendService;
import com.example.clpro.service.telegram.TelegramServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramSendServiceImpl implements TelegramSendService {
    private final UserRepository userRepository;
    private final TelegramServiceImpl telegramBot;
    private final OtpServiceImpl otpService;

    public TelegramSendServiceImpl(UserRepository userRepository, TelegramServiceImpl telegramBot, OtpServiceImpl otpService) {
        this.userRepository = userRepository;
        this.telegramBot = telegramBot;
        this.otpService = otpService;
    }


    @Override
    public String sendOtp(String username) {
        Auth auth = userRepository.findUserByUsername(username);
        if (auth != null && auth.getTelegramId() != null) {
            String otp = otpService.generateOtp(username);
            String text = "\uD83D\uDD0E *OTP Code Alert* \uD83D\uDD0E\n\n";
            text += "Hello, " + auth.getUsername() + "! Your OTP code is: " + otp + "\n\n";
            text += "This code will expire in 2 minutes. Please use it to verify your identity.";

            long chatId = Long.parseLong(auth.getTelegramId());
            telegramBot.sendMessage(chatId, text);

            return "\uD83D\uDCE1 OTP code sent successfully to " + auth.getUsername() + ".";
        } else {
            return "User not found or Telegram ID not set for user: " + username;
        }
    }

}
