// TwilioService.java
package com.MobilePrepaidRecharge.app.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.MobilePrepaidRecharge.app.config.TwilioConfig;

@Service
public class TwilioService {

    @Autowired
    private TwilioConfig twilioConfig;

    public void sendOtp(String toPhoneNumber, String otp) {
        String formattedPhoneNumber = "+91" + toPhoneNumber; // Assuming Indian numbers, adjust as needed
        Message message = Message.creator(
                new PhoneNumber(formattedPhoneNumber),
                new PhoneNumber(twilioConfig.getTwilioPhoneNumber()),
                "Your Mobi-Comm OTP is: " + otp + ". Valid for 5 minutes.")
            .create();
    }
}