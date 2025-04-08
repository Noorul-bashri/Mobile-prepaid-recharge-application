package com.MobilePrepaidRecharge.app.controller;

import com.MobilePrepaidRecharge.app.model.Subscription;
import com.MobilePrepaidRecharge.app.repository.SubscriptionRepository;
import com.MobilePrepaidRecharge.app.service.EmailService;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class SubscriptionController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody SubscriptionRequest request) throws MessagingException {
        if (request.getEmail() == null || !request.getEmail().matches("\\S+@\\S+\\.\\S+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email address");
        }
        if (subscriptionRepository.existsBySubscribedEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already subscribed!");
        }
        Subscription subscription = new Subscription();
        subscription.setSubscribedEmail(request.getEmail());
        subscriptionRepository.save(subscription);
        emailService.sendSubscriptionEmail(request.getEmail());
        return ResponseEntity.ok("Subscription successful! You will receive the latest updates through your subscribed email.");
    }
}

class SubscriptionRequest {
    private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}