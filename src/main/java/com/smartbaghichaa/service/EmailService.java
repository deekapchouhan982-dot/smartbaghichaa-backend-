package com.smartbaghichaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired private JavaMailSender mailSender;

    @Async
    public void sendWelcomeEmail(String name, String email, String city) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Welcome to Smart Baghichaa! \uD83C\uDF31");
            message.setText(
                "Hello " + name + "!\n\n" +
                "Welcome to Smart Baghichaa \u2014 your urban gardening companion.\n\n" +
                "Start by getting plant recommendations for your city " + city + "!\n\n" +
                "Happy Growing! \uD83C\uDF3F\n\n" +
                "\u2014 Smart Baghichaa Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Welcome email failed for " + email + ": " + e.getMessage());
        }
    }
}
