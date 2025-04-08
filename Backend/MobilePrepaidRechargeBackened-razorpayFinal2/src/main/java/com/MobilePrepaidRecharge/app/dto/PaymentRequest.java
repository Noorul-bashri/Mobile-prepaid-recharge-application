package com.MobilePrepaidRecharge.app.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long userId;          // The user making the recharge
    private Long planId;          // The plan selected
    private Double amount;        // Payment amount
    private String paymentMethod; // "UPI", "CreditCard", or "NetBanking"
}