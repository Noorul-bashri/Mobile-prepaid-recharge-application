package com.MobilePrepaidRecharge.app.model;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "subscription")
@Data
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscribed_email", unique = true, nullable = false)
    private String subscribedEmail;
}