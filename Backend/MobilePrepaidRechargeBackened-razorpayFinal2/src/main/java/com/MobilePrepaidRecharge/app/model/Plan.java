
package com.MobilePrepaidRecharge.app.model;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "plan_details")
@Data
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String planName;
    private String planCategory;
    private Double amount;
    private Integer validityDays;
    private String dataVolume;
    private String benefits;
    private String planDescription;
    
    // New field for status with a default of Active
    private String status = "Active";
}
