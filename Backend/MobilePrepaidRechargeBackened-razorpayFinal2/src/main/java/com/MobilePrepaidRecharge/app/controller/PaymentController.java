package com.MobilePrepaidRecharge.app.controller;

import com.MobilePrepaidRecharge.app.model.Plan;
import com.MobilePrepaidRecharge.app.model.Transaction;
import com.MobilePrepaidRecharge.app.model.User;
import com.MobilePrepaidRecharge.app.repository.PlanRepository;
import com.MobilePrepaidRecharge.app.repository.TransactionRepository;
import com.MobilePrepaidRecharge.app.repository.UserRepository;
import com.MobilePrepaidRecharge.app.service.RazorpayService;
import com.MobilePrepaidRecharge.app.service.TransactionService;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private TransactionService transactionService;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @PostMapping("/createOrder")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, String> request) throws RazorpayException {
        String planIdStr = request.get("planId");
        String phone = request.get("phone");
        if (planIdStr == null || phone == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "planId and phone are required");
        }

        Long planId;
        try {
            planId = Long.parseLong(planIdStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid planId format: {}", planIdStr);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid planId format");
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found with ID: " + planId));
        int amount = (int) (plan.getAmount() * 100);
        String currency = "INR";
        String receipt = "receipt#" + System.currentTimeMillis();
        Map<String, Object> order = razorpayService.createOrder(amount, currency, receipt);

        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setStatus("Active");
            return userRepository.save(newUser);
        });

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setPlan(plan);
        transaction.setAmount(plan.getAmount());
        transaction.setPaymentMethod("Razorpay");
        transaction.setTransactionStatus("PENDING");
        transaction.setRazorpayOrderId((String) order.get("id"));
        transaction = transactionRepository.save(transaction);

        Map<String, String> response = new HashMap<>();
        response.put("orderId", (String) order.get("id"));
        response.put("amount", String.valueOf(amount));
        response.put("currency", currency);
        response.put("transactionId", transaction.getId().toString());
        logger.info("Order created successfully: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verifyPayment")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody Map<String, String> request) throws RazorpayException {
        String razorpayOrderId = request.get("razorpay_order_id");
        String razorpayPaymentId = request.get("razorpay_payment_id");
        String razorpaySignature = request.get("razorpay_signature");
        if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All payment fields are required");
        }

        Transaction transaction = transactionRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found for order ID: " + razorpayOrderId));

        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", razorpayOrderId);
        attributes.put("razorpay_payment_id", razorpayPaymentId);
        attributes.put("razorpay_signature", razorpaySignature);

        boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);
        Map<String, String> response = new HashMap<>();
        if (isValid) {
            transaction.setTransactionStatus("SUCCESS");
            transaction.setRazorpayPaymentId(razorpayPaymentId);
            transactionService.saveTransaction(transaction);
            logger.info("Payment verified successfully for order ID: {}", razorpayOrderId);
            response.put("status", "success");
            response.put("message", "Payment verified successfully");
            response.put("redirect", "paymentSuccess.html");
            return ResponseEntity.ok(response);
        } else {
            transaction.setTransactionStatus("FAILED");
            transactionService.saveTransaction(transaction);
            logger.warn("Invalid payment signature for order ID: {}", razorpayOrderId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment signature");
        }
    }
}