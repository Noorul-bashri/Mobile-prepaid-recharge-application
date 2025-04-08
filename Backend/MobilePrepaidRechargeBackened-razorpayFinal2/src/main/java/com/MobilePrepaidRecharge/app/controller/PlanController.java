package com.MobilePrepaidRecharge.app.controller;

import com.MobilePrepaidRecharge.app.model.Plan;
import com.MobilePrepaidRecharge.app.repository.PlanRepository;
import com.MobilePrepaidRecharge.app.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanRepository planRepository;

    @GetMapping
    public ResponseEntity<List<Plan>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @PostMapping
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
        if (plan.getPlanName() == null || plan.getAmount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan name and amount are required");
        }
        return ResponseEntity.ok(planService.createPlan(plan));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updatePlan(@RequestBody Plan updatedPlan) {
        if (updatedPlan.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan id is required for update");
        }
        Plan existingPlan = planRepository.findById(updatedPlan.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        existingPlan.setPlanName(updatedPlan.getPlanName());
        existingPlan.setPlanCategory(updatedPlan.getPlanCategory());
        existingPlan.setAmount(updatedPlan.getAmount());
        existingPlan.setValidityDays(updatedPlan.getValidityDays());
        existingPlan.setDataVolume(updatedPlan.getDataVolume());
        existingPlan.setBenefits(updatedPlan.getBenefits());
        existingPlan.setPlanDescription(updatedPlan.getPlanDescription());
        if (updatedPlan.getStatus() != null) {
            existingPlan.setStatus(updatedPlan.getStatus());
        }
        planRepository.save(existingPlan);
        return ResponseEntity.ok("Plan updated successfully");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deletePlan(@RequestBody Map<String, String> request) {
        String planIdStr = request.get("id");
        if (planIdStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan id is required for deletion");
        }
        Long planId;
        try {
            planId = Long.parseLong(planIdStr);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan id value");
        }
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        plan.setStatus("Inactive");
        planRepository.save(plan);
        return ResponseEntity.ok("Plan marked as inactive successfully");
    }
}