package com.MobilePrepaidRecharge.app.repository;

import com.MobilePrepaidRecharge.app.model.Plan;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, Long>{
	Plan findByPlanName(String planName);
	Optional<Plan> findByAmount(double amount);
	@Query("SELECT p FROM Plan p WHERE ABS(p.amount - :amount) < 0.001")
	Optional<Plan> findByAmountApprox(@Param("amount") double amount);



}