// UserService.java
package com.MobilePrepaidRecharge.app.service;

import com.MobilePrepaidRecharge.app.model.Transaction;
import com.MobilePrepaidRecharge.app.model.User;
import com.MobilePrepaidRecharge.app.repository.TransactionRepository;
import com.MobilePrepaidRecharge.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    public User registerUser(User user) {
        return userRepository.save(user);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            Transaction latestTx = transactionRepository.findTopByUserOrderByTransactionDateDesc(user);
            user.setCurrentPlan(latestTx != null && "SUCCESS".equals(latestTx.getTransactionStatus()) 
                ? latestTx.getPlan().getPlanName() 
                : "No active plan");
        });
        return users;
    }
    
}