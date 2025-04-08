package com.MobilePrepaidRecharge.app.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users_details")
@Data
public class User {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   
   private String fullName;
   private String email;
   
   @Column(unique = true)
   private String phone;
   
   private String status ;

   @Column(name = "current_plan")
   private String currentPlan;
   
   // Getters and setters
  
}