package com.financewave.auth.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.financewave.auth.entity.OtpToken;
import com.financewave.auth.repository.OtpTokenRepository;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository repo;

    @Autowired
    private JavaMailSender mailSender;

    // ✅ GENERATE + SEND OTP
    public void sendOtp(String email) {

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        OtpToken token = repo.findByEmail(email)
                .orElse(new OtpToken());

        token.setEmail(email);
        token.setOtp(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        repo.save(token);

        // ✅ EMAIL SEND
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("FinanceWave OTP");
        message.setText("Your OTP is: " + otp + " (valid 5 mins)");

        mailSender.send(message);
    }

    // ✅ VERIFY OTP
    public boolean verifyOtp(String email, String otp) {

        OtpToken token = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        return token.getOtp().equals(otp)
                && token.getExpiryTime().isAfter(LocalDateTime.now());
    }
}