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
    public void sendResetEmail(String name, String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Smart Baghichaa \u2014 Password Reset");
            message.setText(
                "Hello " + name + "!\n\n" +
                "You requested a password reset for your Smart Baghichaa account.\n\n" +
                "Click this link to reset your password:\n" +
                "http://localhost:8080/reset?token=" + token + "\n\n" +
                "This link expires in 30 minutes.\n\n" +
                "If you didn't request this, ignore this email.\n\n" +
                "\u2014 Smart Baghichaa Team \uD83C\uDF31"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Reset email failed for " + email + ": " + e.getMessage());
        }
    }

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

    @Async
    public void sendOtpEmail(String name, String email, String otp, String purpose) {
        try {
            String subject = "SIGNUP".equals(purpose)
                ? "Smart Baghichaa \u2014 Verify Your Email"
                : "Smart Baghichaa \u2014 Password Reset OTP";
            String body = "Hello " + (name != null ? name : "Gardener") + "!\n\n"
                + ("SIGNUP".equals(purpose)
                    ? "Use the OTP below to verify your email and complete registration:\n\n"
                    : "Use the OTP below to reset your Smart Baghichaa password:\n\n")
                + "  \u2192 OTP: " + otp + "\n\n"
                + "This OTP is valid for 10 minutes. Do not share it with anyone.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "\u2014 Smart Baghichaa Team \uD83C\uDF31";
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] OTP email failed for " + email + ": " + e.getMessage());
        }
    }

    @Async
    public void sendReminderEmail(String name, String email, String timeLabel, java.util.List<String> tasks) {
        try {
            StringBuilder taskList = new StringBuilder();
            for (String t : tasks) taskList.append("  \u2713 ").append(t).append("\n");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Smart Baghichaa \uD83C\uDF31 " + timeLabel + " Plant Care Reminder");
            message.setText(
                "Hello " + name + "!\n\n" +
                "It's time for your " + timeLabel.toLowerCase() + " plant care routine.\n\n" +
                "Today's tasks:\n" + taskList +
                "\nLog your plant's progress in the Growth Tracker to keep your AI analysis accurate.\n\n" +
                "Happy Gardening! \uD83C\uDF3F\n\n" +
                "\u2014 Smart Baghichaa Team\n" +
                "(To stop these emails, disable reminders from your Care section.)"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Reminder email failed for " + email + ": " + e.getMessage());
        }
    }

    @Async
    public void sendContactNotification(String senderName, String senderEmail, String subject, String messageText) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("tanujawala@gmail.com");
            message.setSubject("Smart Baghichaa Contact: " + subject);
            message.setText(
                "New contact message received on Smart Baghichaa\n\n" +
                "From : " + senderName + " <" + senderEmail + ">\n" +
                "Subject: " + subject + "\n\n" +
                "Message:\n" + messageText + "\n\n" +
                "\u2014 Smart Baghichaa Notification"
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Contact notification failed: " + e.getMessage());
        }
    }
}
