// TransactionRepository.java
package com.MobilePrepaidRecharge.app.repository;

import com.MobilePrepaidRecharge.app.model.Transaction;
import com.MobilePrepaidRecharge.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);
    Transaction findTopByUserOrderByTransactionDateDesc(User user);
    List<Transaction> findByUserId(Long userId);
}