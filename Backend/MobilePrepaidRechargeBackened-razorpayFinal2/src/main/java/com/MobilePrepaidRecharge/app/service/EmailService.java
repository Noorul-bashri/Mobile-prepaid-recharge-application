package com.MobilePrepaidRecharge.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.MobilePrepaidRecharge.app.model.Subscription;
import com.MobilePrepaidRecharge.app.repository.SubscriptionRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSubscriptionEmail(String toEmail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Welcome to Mobi-Comm Newsletter!");
        helper.setFrom("noorulbashri241@gmail.com"); // Replace with your email
        helper.setText(
            "<!DOCTYPE html>" +
            "<html>" +
            "<body style='font-family: Poppins, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; color: #333;'>" +
            "<div style='max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 15px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); overflow: hidden;'>" +
            "<div style='background: linear-gradient(135deg, #6c63ff, #55afc1); padding: 30px; text-align: center; color: #ffffff;'>" +
            "<h1 style='margin: 0; font-size: 28px; font-weight: 600;'>Welcome to Mobi-Comm!</h1>" +
            "<p style='margin: 10px 0 0; font-size: 16px;'>You’re Now Part of Our Connected Family!</p>" +
            "</div>" +
            "<div style='padding: 25px; text-align: center;'>" +
            "<h2 style='color: #6c63ff; font-size: 22px; margin-bottom: 15px;'>Get Ready for an Amazing Journey!</h2>" +
            "<p style='line-height: 1.6; font-size: 15px; color: #555;'>Thank you for subscribing to the Mobi-Comm Newsletter! Expect a world of <strong>exclusive offers</strong>, <strong>lightning-fast updates</strong>, and <strong>insider tips</strong> to keep you connected like never before.</p>" +
            "<p style='line-height: 1.6; font-size: 15px; color: #555;'>Here’s what’s coming your way:</p>" +
            "<ul style='list-style: none; padding: 0; margin: 15px 0; font-size: 14px; color: #666;'>" +
            "<li style='margin-bottom: 10px;'>⚡ <strong>Latest Updates:</strong> Stay ahead with news on cutting-edge connectivity.</li>" +
            "</ul>" +
            "<a href='http://127.0.0.1:5500/User/ValidationPage.html' style='display: inline-block; padding: 12px 25px; background: #6c63ff; color: #ffffff; text-decoration: none; border-radius: 25px; font-weight: 600; margin-top: 20px; transition: background 0.3s;'>Explore Now</a>" +
            "</div>" +
            "<div style='background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #888;'>" +
            "<p style='margin: 0;'>Not loving the vibes? <a href='#' style='color: #6c63ff; text-decoration: none;'>Unsubscribe anytime</a> by reaching out to our support crew.</p>" +
            "<p style='margin: 5px 0 0;'>© 2025 Mobi-Comm Service Pvt Ltd. All rights reserved.</p>" +
            "</div>" +
            "</body>" +
            "</html>",
            true // Enable HTML content
        );

        mailSender.send(message);
    }
}

