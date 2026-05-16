package com.financewave.auth.service;

import com.financewave.auth.entity.OtpToken;
import com.financewave.auth.repository.OtpTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository repo;

    @Autowired
    private JavaMailSender mailSender;

    // =========================
    // SEND OTP (WITH RESEND LIMIT)
    // =========================
    public void sendOtp(String email) {

        OtpToken token = repo.findByEmail(email)
                .orElse(new OtpToken());

        // ⛔ RESEND LIMIT CHECK
        if (token.getLastSentAt() != null &&
            token.getLastSentAt().isAfter(LocalDateTime.now().minusMinutes(10))) {

            if (token.getResendCount() >= 3) {
                throw new RuntimeException("Too many OTP requests. Try after 10 minutes.");
            }
        } else {
            // reset window
            token.setResendCount(0);
        }

        // 🔢 Generate OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        token.setEmail(email);
        token.setOtp(otp);
        token.setAttempts(0);
        token.setBlocked(false);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        // ✅ RESEND TRACK
        token.setResendCount(token.getResendCount() + 1);
        token.setLastSentAt(LocalDateTime.now());

        repo.save(token);

        // 📧 SEND EMAIL
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("FinanceWave OTP");
        message.setText("Your OTP is: " + otp);

        mailSender.send(message);
    }

    // =========================
    // VERIFY OTP
    // =========================
    public boolean verifyOtp(String email, String otp) {

        OtpToken token = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        // 🔒 BLOCK CHECK
        if (token.isBlocked()) {
            throw new RuntimeException("OTP blocked. Request new OTP.");
        }

        // ⏰ EXPIRY CHECK
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        // ❌ WRONG OTP
        if (!token.getOtp().equals(otp)) {

            token.setAttempts(token.getAttempts() + 1);

            if (token.getAttempts() >= 3) {
                token.setBlocked(true);
            }

            repo.save(token);

            throw new RuntimeException("Invalid OTP");
        }

        // ✅ SUCCESS
        token.setAttempts(0);
        repo.save(token);

        return true;
    }
}