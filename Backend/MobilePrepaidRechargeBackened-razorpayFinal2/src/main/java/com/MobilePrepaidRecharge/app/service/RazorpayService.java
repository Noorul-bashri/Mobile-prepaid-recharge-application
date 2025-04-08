package com.MobilePrepaidRecharge.app.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RazorpayService {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayService.class);

    private RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing RazorpayClient with keyId: {} and keySecret: [REDACTED]", keyId);
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            logger.error("Failed to initialize RazorpayClient: {}", e.getMessage());
            throw new IllegalStateException("Failed to initialize RazorpayClient", e);
        }
    }

    public Map<String, Object> createOrder(int amount, String currency, String receipt) throws RazorpayException {
        logger.info("Creating order with amount: {}, currency: {}, receipt: {}", amount, currency, receipt);

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive and in paise");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (receipt == null || receipt.trim().isEmpty()) {
            throw new IllegalArgumentException("Receipt is required");
        }

        // Create a JSONObject with the order details
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1); // Using integer for capture

        logger.debug("Order request: {}", orderRequest.toString());
        
        // Create the order using RazorpayClient
        Order order = razorpayClient.orders.create(orderRequest);
        logger.info("Order created successfully: {}", order);

        // Convert the order object to a Map if needed
        JSONObject orderJson = new JSONObject(order.toString());
        Map<String, Object> orderMap = new HashMap<>();
        for (String key : orderJson.keySet()) {
            orderMap.put(key, orderJson.get(key));
        }

        return orderMap;
    }
}
