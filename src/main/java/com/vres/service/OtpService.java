package com.vres.service;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    public String generateOtp() {
        logger.info("Generating new OTP...");

        Random random = new Random();
        int otpNumber = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpNumber);

        logger.debug("Generated OTP: {}", otp); // Keep debug level for security reasons (avoid logging OTP in production info)
        return otp;
    }
}