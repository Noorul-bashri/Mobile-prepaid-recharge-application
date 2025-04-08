package com.MobilePrepaidRecharge.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_details")
@Data
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;
    
    private Double amount;
    private String paymentMethod; // e.g., Razorpay
    private String transactionStatus; // e.g., PENDING, SUCCESS, FAILED
    
    // Razorpay-specific fields
    private String razorpayOrderId;
    private String razorpayPaymentId;
    
    private LocalDateTime transactionDate = LocalDateTime.now();
}