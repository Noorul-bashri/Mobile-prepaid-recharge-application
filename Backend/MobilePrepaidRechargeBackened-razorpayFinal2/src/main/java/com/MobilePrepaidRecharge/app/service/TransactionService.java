package com.MobilePrepaidRecharge.app.service;

import com.MobilePrepaidRecharge.app.model.Transaction;
import com.MobilePrepaidRecharge.app.model.User;
import com.MobilePrepaidRecharge.app.repository.TransactionRepository;
import com.MobilePrepaidRecharge.app.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    public Transaction saveTransaction(Transaction transaction) {
        Transaction savedTransaction = transactionRepository.save(transaction);
        if ("SUCCESS".equals(savedTransaction.getTransactionStatus())) {
            // Update user's current plan
            User user = savedTransaction.getUser();
            user.setCurrentPlan(savedTransaction.getPlan().getPlanName());
            userRepository.save(user);
            try {
                sendTransactionEmail(savedTransaction);
            } catch (MessagingException e) {
                logger.error("Failed to send transaction email to {} for transaction ID: {}. Error: {}", 
                    savedTransaction.getUser().getEmail(), savedTransaction.getId(), e.getMessage());
            }
        }
        return savedTransaction;
    }

    // Rest of the existing methods remain unchanged
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    private void sendTransactionEmail(Transaction transaction) throws MessagingException {
        if (transaction.getUser().getEmail() == null || transaction.getUser().getEmail().isEmpty()) {
            logger.info("User {} has no email set, skipping email notification", transaction.getUser().getPhone());
            return;
        }

        // Create MimeMessage for HTML email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(transaction.getUser().getEmail());
        helper.setSubject("🎉 Recharge Successful – Transaction Details");
        helper.setFrom("noorulbashri241@gmail.com"); // Replace with your sender email
        helper.setText(
            "<!DOCTYPE html>" +
            "<html>" +
            "<body style='font-family: Poppins, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9; color: #333;'>" +
            "<div style='max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 15px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); overflow: hidden;'>" +
            "<div style='background: linear-gradient(135deg, #6c63ff, #9d9ade); padding: 30px; text-align: center; color: #ffffff;'>" +
            "<h1 style='margin: 0; font-size: 28px; font-weight: 600;'>🎉 Recharge Successful!</h1>" +
            "<p style='margin: 10px 0 0; font-size: 16px;'>You’re All Set to Stay Connected!</p>" +
            "</div>" +
            "<div style='padding: 25px; text-align: center;'>" +
            "<h2 style='color: #6c63ff; font-size: 22px; margin-bottom: 15px;'>Dear " + transaction.getUser().getFullName() + ",</h2>" +
            "<p style='line-height: 1.6; font-size: 15px; color: #555;'>Your recharge was successful! Here are the transaction details:</p>" +
            "<div style='background: #f8f9fa; padding: 20px; border-radius: 10px; margin: 20px 0; text-align: left; border-left: 4px solid #6c63ff;'>" +
            "<p style='margin: 5px 0; font-size: 14px;'><span style='font-weight: 600; color: #333;'>Plan:</span> " + transaction.getPlan().getPlanName() + "</p>" +
            "<p style='margin: 5px 0; font-size: 14px;'><span style='font-weight: 600; color: #333;'>Amount:</span> ₹" + transaction.getAmount() + "</p>" +
            "<p style='margin: 5px 0; font-size: 14px;'><span style='font-weight: 600; color: #333;'>Payment Method:</span> " + transaction.getPaymentMethod() + "</p>" +
            "<p style='margin: 5px 0; font-size: 14px;'><span style='font-weight: 600; color: #333;'>Transaction Date:</span> " + transaction.getTransactionDate() + "</p>" +
            "<p style='margin: 5px 0; font-size: 14px;'><span style='font-weight: 600; color: #333;'>Transaction ID:</span> " + transaction.getId() + "</p>" +
            "</div>" +
            "<p style='line-height: 1.6; font-size: 15px; color: #555;'>Thank you for choosing Mobi-Comm! Enjoy seamless connectivity and exclusive benefits.</p>" +
            "<a href='http://127.0.0.1:5500/User/plans.html' style='display: inline-block; padding: 12px 25px; background: #6c63ff; color: #ffffff; text-decoration: none; border-radius: 25px; font-weight: 600; margin-top: 20px;'>Explore More Plans</a>" +
            "</div>" +
            "<div style='background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: #888;'>" +
            "<p style='margin: 0;'>Need assistance? Reach out to us at <a href='mailto:support@yourwebsite.com' style='color: #6c63ff; text-decoration: none;'>support@yourwebsite.com</a>.</p>" +
            "<p style='margin: 5px 0 0;'>© 2025 Mobi-Comm Service Pvt Ltd. All rights reserved.</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>",
            true // Enable HTML content
        );

        mailSender.send(message);
        logger.info("Email sent to {} for transaction ID: {}", transaction.getUser().getEmail(), transaction.getId());
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return transactionRepository.findAll()
                .stream()
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .limit(limit)
                .toList();
    }

    public Transaction findByRazorpayOrderId(String razorpayOrderId) {
        return transactionRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElse(null);
    }
}