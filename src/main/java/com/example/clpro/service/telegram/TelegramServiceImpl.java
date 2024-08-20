package com.example.clpro.service.telegram;

import com.example.clpro.entities.model.Auth;
import com.example.clpro.repository.AuthRepository;
import com.example.clpro.repository.UserRepository;
import com.example.clpro.service.interfaces.AuthService;
import com.example.clpro.service.interfaces.EmailService;
import com.example.clpro.service.interfaces.OtpService;
import com.example.clpro.service.serviceImpl.OtpServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramServiceImpl extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramServiceImpl.class);

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final EmailService emailService;
    private  final OtpService otpService;


    public static final String botToken = "6903286543:AAH0uM9h7DJNrIesoqICBnqvzJaMh8aMBU0";
    public static final String botUsername = "OrginalKhenBot";

    public TelegramServiceImpl(UserRepository userRepository, AuthRepository authRepository, EmailService emailService, OtpService otpService) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.emailService = emailService;
        this.otpService = otpService;
    }


    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    long chatId;

    @Override
    public void onUpdateReceived(Update update) {
        try {
            logger.info("work");
            if (update.hasMessage() && update.getMessage().hasText()) {
                chatId = update.getMessage().getChatId();
                String messageText = update.getMessage().getText();

                if ("/start".equals(messageText)) {
                    sendMessage(chatId, "Welcome bong bong!! Please enter your app's username");
                } else {
                    Pattern pattern = Pattern.compile("username: (\\w+)");
                    Matcher matcher = pattern.matcher(messageText);

                    if (matcher.find()) {
                        String username = matcher.group(1);
                        if (userRepository.existsByUsername(username)) {
                            Auth auth = userRepository.findUserByUsername(username);
                            if (auth == null) {
                                sendMessage(chatId, "User not found: " + username);
                            } else {
                                if (auth.getTelegramId() != null) {
                                    sendMessage(chatId, "You have already registered with our Gigi bot.");
                                } else {
                                    String otp = otpService.generateEmailToken(auth.getEmail());
                                    emailService.sendOtpToEmail(auth.getEmail(), otp);
                                    sendMessage(chatId, "OTP has been sent to your registered email. Please enter the OTP. \nAnd following the format [otp: otp_code].");
                                }

                            }
                        } else {
                            sendMessage(chatId, "Username does not exist: " + username);
                        }
                    } else {
                        Pattern otpPattern = Pattern.compile("otp: (.+)");
                        Matcher otpMatcher = otpPattern.matcher(messageText);
                        System.out.println(otpPattern);
                        if (otpMatcher.find()) {
                            String otp = otpMatcher.group(1);
                            String email = otpService.decodeEmailToken(otp);
                            Auth auth = userRepository.findUserByEmail(email);
                            if (auth != null && otpService.validateEmailToken(otp, email)) {
                                auth.setTelegramId(String.valueOf(chatId));
                                authRepository.save(auth);
                                sendDetailedNotification(chatId, auth.getUsername());
                            } else {
                                sendMessage(chatId, "Invalid or Expired OTP.");
                            }
                        } else {
                            sendMessage(chatId, "Invalid format. Please provide a message with this [otp: otp_code]");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in onUpdateReceived", e);
        }
//            else if (update.hasCallbackQuery()) {
//                CallbackQuery callbackQuery = update.getCallbackQuery();
//                String data = callbackQuery.getData();
//                long chatId = callbackQuery.getMessage().getChatId();
//
//                if ("thet_silong".equals(data)) {
//                    sendResponseMessage(chatId, "Ah Long ah lop \uD83D\uDC95");
//                } else if ("mo".equals(data)) {
//                    sendResponseMessage(chatId, "Ah Mo ah lop \uD83D\uDC95");
//                } else if ("both".equals(data)) {
//                    sendResponseMessage(chatId, "Ah Long Ah Mo are Gays \uD83D\uDC95");
//                }
//            }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        try {
            message.setChatId(chatId);
            message.setText(text);
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message", e);
        }
    }

    private void sendDetailedNotification(long chatId, String username) {
        String detailedMessage = "🎉 *Telegram Updated!* 🎉\n\n" +
                "Hi *" + username + "*,\n\n" +
                "✅ Your Telegram ID has been successfully updated.\n\n" +
                "🔔 *Notifications*: You will now receive updates about your deployment system through this bot.\n\n" +
                "⚙️ *Features*:\n" +
                "1. Stay updated on your deployment status.\n" +
                "2. Get important alerts directly on Telegram.\n\n" +
                "📢 *Note*: Make sure to interact with the bot regularly to stay informed.\n\n" +
                "Thank you for using our service! 😊";
        sendMessage(chatId, detailedMessage);
    }
}
