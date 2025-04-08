package com.MobilePrepaidRecharge.app.controller;

import com.MobilePrepaidRecharge.app.model.Transaction;
import com.MobilePrepaidRecharge.app.repository.TransactionRepository;
import com.MobilePrepaidRecharge.app.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions(@RequestParam(defaultValue = "10") int limit) {
        List<Transaction> transactions = transactionService.getRecentTransactions(limit);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<Transaction>> getExpiringTransactions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        List<Transaction> expiring = allTransactions.stream()
            .filter(t -> {
                if (t.getPlan() == null || t.getPlan().getValidityDays() == null) return false;
                LocalDateTime expiry = t.getTransactionDate().plusDays(t.getPlan().getValidityDays());
                return expiry.isAfter(now) && expiry.isBefore(threeDaysLater);
            })
            .toList();
        return ResponseEntity.ok(expiring);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return ResponseEntity.ok(transactions);
    }
}