package com.MobilePrepaidRecharge.app.controller;

import com.MobilePrepaidRecharge.app.exception.InvalidLoginException;
import com.MobilePrepaidRecharge.app.model.Admin;
import com.MobilePrepaidRecharge.app.model.User;
import com.MobilePrepaidRecharge.app.repository.UserRepository;
import com.MobilePrepaidRecharge.app.security.JWTUtils;
import com.MobilePrepaidRecharge.app.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<Admin> register(@RequestBody AdminRegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPassword(request.getPassword());
        Admin registeredAdmin = adminService.registerAdmin(admin);
        registeredAdmin.setPassword(null);
        return ResponseEntity.ok(registeredAdmin);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            String token = JWTUtils.generateToken(auth.getName(), "ADMIN");
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException ex) {
            throw new InvalidLoginException("Invalid Login Credentials. Try again.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutAdmin(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Admin logged out successfully");
    }

    @PostMapping("/notify-expiring")
    public ResponseEntity<String> notifyExpiring(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no email set");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("⏰ Your Plan is Expiring Soon – Recharge Now!");
            helper.setFrom("noorulbashri241@gmail.com");
            helper.setText(
                    "<!DOCTYPE html>" +
                    "<html>" +
                    "<body style='font-family: Poppins, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; color: #333;'>" +
                    "<div style='max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 15px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); overflow: hidden;'>" +
                    "<div style='background: linear-gradient(135deg, #007bff, #00c4b4); padding: 30px; text-align: center; color: #ffffff;'>" +
                    "<h1 style='margin: 0; font-size: 28px; font-weight: 600;'>⏰ Plan Expiring Soon!</h1>" +
                    "<p style='margin: 10px 0 0; font-size: 16px;'>Don’t Let Your Connectivity Fade Away</p>" +
                    "</div>" +
                    "<div style='padding: 25px; text-align: center;'>" +
                    "<h2 style='color: #007bff; font-size: 22px; margin-bottom: 15px;'>Dear " + user.getFullName() + ",</h2>" +
                    "<p style='line-height: 1.6; font-size: 15px; color: #555;'>We wanted to remind you that your current plan is <strong>expiring soon</strong>. To keep enjoying uninterrupted services with Mobi-Comm, it’s time to recharge!</p>" +
                    "<p style='line-height: 1.6; font-size: 15px; color: #555;'>Stay connected with our lightning-fast network and exclusive offers.</p>" +
                    "<a href='http://127.0.0.1:5500/User/plans.html' style='display: inline-block; padding: 12px 25px; background: #007bff; color: #ffffff; text-decoration: none; border-radius: 25px; font-weight: 600; margin-top: 20px;'>Recharge Now</a>" +
                    "</div>" +
                    "<div style='background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #888;'>" +
                    "<p style='margin: 0;'>Need help? Contact our support team at <a href='mailto:support@yourwebsite.com' style='color: #007bff; text-decoration: none;'>noorulbashri241@gmail.com</a>.</p>" +
                    "<p style='margin: 5px 0 0;'>© 2025 Mobi-Comm Service Pvt Ltd. All rights reserved.</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>",
                    true // Enable HTML content
                );
            mailSender.send(message);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (MessagingException e) {
            // Log the error (assuming a logger is available; add a logger if not already present)
            System.err.println("Failed to send notification email to " + user.getEmail() + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send notification email");
        }
    }

    // DTO classes remain unchanged
    public static class AdminRegisterRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String token;
        public AuthResponse(String token) { this.token = token; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}