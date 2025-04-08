package com.MobilePrepaidRecharge.app.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "admins_data")
@Data
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    // Stored as BCrypt hash
    private String password;

    private String role = "ADMIN";
    // Getters and setters
   
}
   
