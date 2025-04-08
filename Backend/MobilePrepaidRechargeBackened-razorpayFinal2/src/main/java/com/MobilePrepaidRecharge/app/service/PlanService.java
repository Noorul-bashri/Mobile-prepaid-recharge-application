package com.MobilePrepaidRecharge.app.service;

import com.MobilePrepaidRecharge.app.model.Plan;
import com.MobilePrepaidRecharge.app.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;
    
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }
    
    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }
    
    public Plan updatePlan(Plan plan) {
        return planRepository.save(plan);
    }
    
    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }
}
