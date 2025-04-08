package com.MobilePrepaidRecharge.app.controller;

import com.MobilePrepaidRecharge.app.model.User;
import com.MobilePrepaidRecharge.app.security.JWTUtils;
import com.MobilePrepaidRecharge.app.service.TwilioService;
import com.MobilePrepaidRecharge.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
public class UserAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TwilioService twilioService;

    private final Random random = new Random();
    private final Map<String, String> otpStore = new java.util.HashMap<>();

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getPhone() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and phone are required");
        }
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }
        if (userService.findByPhone(request.getPhone()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already registered");
        }
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus("Active");
        User savedUser = userService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        if (request.getPhone() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }
        userService.findByPhone(request.getPhone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found. Please register."));
        String otp = String.format("%06d", random.nextInt(999999));
        otpStore.put(request.getPhone(), otp);
        twilioService.sendOtp(request.getPhone(), otp);
        return ResponseEntity.ok("OTP sent to your mobile number.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody OtpRequest request) {
        if (request.getPhone() == null || request.getOtp() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone and OTP are required");
        }
        String storedOtp = otpStore.get(request.getPhone());
        if (storedOtp == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No OTP generated for this number.");
        }
        if (!storedOtp.equals(request.getOtp())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP.");
        }
        User user = userService.findByPhone(request.getPhone())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found after OTP verification."));
        otpStore.remove(request.getPhone());
        String token = JWTUtils.generateToken(request.getPhone(), "USER");
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/check-phone/{phone}")
    public ResponseEntity<String> checkPhone(@PathVariable String phone) {
        userService.findByPhone(phone)
                .ifPresentOrElse(
                    user -> {},
                    () -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This number is not registered with Mobi-Comm."); }
                );
        return ResponseEntity.ok("User validated");
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required.");
        }
        User user = userService.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        String email = body.get("email");
        String fullName = body.get("fullName");
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email);
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        userService.saveUser(user);
        return ResponseEntity.ok("Profile updated successfully.");
    }

    // DTO classes remain unchanged
    static class RegisterRequest {
        private String fullName;
        private String email;
        private String phone;
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    static class LoginRequest {
        private String phone;
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    static class OtpRequest {
        private String phone;
        private String otp;
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }

    static class AuthResponse {
        private String token;
        public AuthResponse(String token) { this.token = token; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}