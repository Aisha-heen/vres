package com.vres.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// --- End required imports ---

import com.vres.config.JwtService;
import com.vres.config.UserDetailsServiceImpl;
import com.vres.dto.LoginRequest;
import com.vres.dto.LoginResponse;
import com.vres.dto.ProjectSummaryDto;
import com.vres.dto.ResetPasswordRequest;
import com.vres.entity.Department;
import com.vres.entity.ProjectUser;
import com.vres.entity.Users;
import com.vres.repository.DepartmentRepository;
import com.vres.repository.ProjectUserRepository;
import com.vres.repository.UsersRepository;

// --- Add required imports ---
import jakarta.persistence.EntityNotFoundException; // Ensure this is imported


@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UsersRepository usersRepository;
    @Autowired private ProjectUserRepository projectUserRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private SnsService snsService; // Keep if still used for activation
    @Autowired private OtpService otpService;
    @Autowired private JavaMailSender mailSender;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;
    @Autowired private UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- (login, vendorLogin, buildLoginResponse methods remain unchanged from previous version) ---
     public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Login attempt for userId: {}", loginRequest.getUserId());

        // --- STEP 1: Check if user exists BEFORE authenticating ---
        Users user = usersRepository.findByEmail(loginRequest.getUserId())
                .orElseThrow(() -> {
                    // User not found, throw specific exception
                    logger.warn("Login failed: User not found with email: {}", loginRequest.getUserId());
                    return new EntityNotFoundException("User not found with email: " + loginRequest.getUserId());
                });
        // --- END USER CHECK ---

        // --- STEP 2: Attempt authentication (checks password) ---
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUserId(), loginRequest.getPassword())
            );
            logger.info("Authentication successful for userId: {}", loginRequest.getUserId());
        } catch (AuthenticationException e) {
            // Catch authentication failure (likely bad password since user was found)
            logger.error("Authentication failed (bad credentials) for userId: {}", loginRequest.getUserId());
            // Throw BadCredentialsException specifically for wrong password
            throw new BadCredentialsException("Invalid credentials.");
        }
        // --- END AUTHENTICATION ---

        // --- STEP 3: User exists and password is correct, proceed ---
        if (!user.isIs_active()) {
            logger.warn("User {} is not active. Checking SNS confirmation status...", user.getEmail());
            // Your existing activation logic
            boolean isConfirmed = snsService.isSubscriptionConfirmed(user.getEmail());
            if (isConfirmed) {
                logger.info("SNS confirmation verified for user {}. Activating account.", user.getEmail());
                user.setIs_active(true);
                usersRepository.save(user);
            } else {
                logger.error("Account not active for user {}. Email confirmation pending.", user.getEmail());
                throw new IllegalStateException("Account not active. Please check your email for the confirmation link."); // Use IllegalStateException
            }
        }

        LoginResponse response = buildLoginResponse(user); // Assumes this method is correct

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        response.setJwtToken(jwtToken);

        logger.info("Login process completed successfully for userId: {}", loginRequest.getUserId());
        return response;
    }


    public LoginResponse vendorLogin(LoginRequest loginRequest) {
        logger.info("Vendor login attempt for userId: {}", loginRequest.getUserId());

         // --- STEP 1: Check if user exists BEFORE authenticating ---
         Users user = usersRepository.findByEmail(loginRequest.getUserId())
                .orElseThrow(() -> {
                    logger.warn("Vendor login failed: User not found with email: {}", loginRequest.getUserId());
                    return new EntityNotFoundException("User not found with email: " + loginRequest.getUserId());
                });
        // --- END USER CHECK ---

        // --- STEP 2: Attempt authentication (checks password) ---
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUserId(), loginRequest.getPassword())
            );
             logger.info("Vendor authentication successful for userId: {}", loginRequest.getUserId());
        } catch (AuthenticationException e) {
            logger.error("Vendor authentication failed (bad credentials) for userId: {}", loginRequest.getUserId());
            throw new BadCredentialsException("Invalid credentials.");
        }
         // --- END AUTHENTICATION ---

        List<ProjectUser> userAssignments = projectUserRepository.findAllByUserId(user.getId());
        boolean isVendor = userAssignments.stream()
            .anyMatch(assignment -> assignment.getRole() != null && "Vendor".equalsIgnoreCase(assignment.getRole().getName()));

        if (!isVendor) {
            logger.warn("Access denied. User {} is not a vendor.", user.getEmail());
            // Consider throwing a specific AccessDeniedException if using method security elsewhere
            throw new BadCredentialsException("Access denied. This login is for vendors only."); // Use BadCredentials to keep error consistent on frontend
        }

        // --- User is a vendor, password ok ---
        if (!user.isIs_active()) {
             // Handle inactive vendor if necessary
             logger.warn("Vendor {} is not active.", user.getEmail());
             throw new IllegalStateException("Vendor account is not active."); // Or appropriate exception
        }

        LoginResponse response = buildLoginResponse(user);

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        response.setJwtToken(jwtToken);

        logger.info("Vendor login process completed successfully for userId: {}", loginRequest.getUserId());
        return response;
    }


     private LoginResponse buildLoginResponse(Users user) {
        logger.debug("Building login response for user: {}", user.getEmail());
        List<ProjectUser> userAssignments = projectUserRepository.findAllByUserId(user.getId());

        // Note: Decided earlier that users without assignments might still log in (like PC/Admin)
        // If that logic needs changing, adjust here.

        List<ProjectSummaryDto> assignedProjects = userAssignments.stream()
                .filter(assignment -> assignment.getProject() != null && assignment.getRole() != null)
                .map(assignment -> {
                    Integer departmentId = departmentRepository.findByProjectIdAndUser(assignment.getProject().getId(), user.getId())
                            .map(Department::getId)
                            .orElse(null);

                    return new ProjectSummaryDto(
                            assignment.getProject().getId(),
                            assignment.getProject().getTitle(),
                            departmentId,
                            assignment.getRole().getName().toUpperCase() // Role from assignment
                    );
                })
                .collect(Collectors.toList());

        // Determine the primary role for the response DTO
        String primaryRole = null;
         if (!userAssignments.isEmpty() && userAssignments.get(0).getRole() != null) {
            // Use role from the first assignment found
             primaryRole = userAssignments.get(0).getRole().getName().toUpperCase();
             logger.debug("Primary role for {} set to: {}", user.getEmail(), primaryRole);
         } else {
             // This case should ideally not happen for logged-in users if roles are mandatory
             logger.warn("User {} has assignments but no role found in the first one. Setting role to null in response.", user.getEmail());
             // Consider fetching user's base role if available, or throwing an error if role is essential
         }

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setProjects(assignedProjects);
        response.setRole(primaryRole); // Set the determined role

        logger.debug("Login response built successfully for user: {}", user.getEmail());
        return response;
    }


    // --- UPDATED forgotPassword METHOD ---
    @Transactional
    public void forgotPassword(String email) {
        logger.info("Processing forgot password request for email: {}", email);
        logger.debug("Attempting to find user by email: {}", email);
        // Find the user by email or throw EntityNotFoundException if not found
        Users user = usersRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.warn("Forgot password request failed: User not found with email: {}", email);
                return new EntityNotFoundException("User not found with email: " + email);
            });

        // --- User found, proceed with OTP generation and sending ---
        String otp = otpService.generateOtp();
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10)); // Use configured validity
        usersRepository.save(user); // Save OTP and expiry time to user record
        sendOtpEmail(user.getEmail(), otp); // Send the OTP via email

        logger.info("OTP sent successfully for forgot password request to {}", email);
    }
    // --- END UPDATED forgotPassword METHOD ---


    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        logger.info("Reset password attempt for email: {}", request.getEmail());
        
        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("Reset password failed. User not found for email: {}", request.getEmail());
                    return new EntityNotFoundException("Invalid request. User not found with email: " + request.getEmail()); // More specific message
                });

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            logger.error("Invalid OTP provided for user: {}", request.getEmail());
            throw new BadCredentialsException("Invalid or incorrect OTP."); // Use BadCredentials for consistency?
        }

        if (user.getOtpExpiryTime() == null || user.getOtpExpiryTime().isBefore(LocalDateTime.now())) { // Check for null expiry
            logger.error("Expired OTP used for user: {}", request.getEmail());
            throw new IllegalStateException("OTP has expired.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null); // Clear OTP after successful use
        user.setOtpExpiryTime(null); // Clear expiry time
        usersRepository.save(user);

        logger.info("Password successfully reset for user: {}", request.getEmail());
    }


    private void sendOtpEmail(String email, String otp) {
        logger.debug("Sending OTP email to {}", email);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your VRES Password Reset Code");
            message.setText("Your One-Time Password (OTP) for resetting your password is: " + otp +
                              "\n\nThis code is valid for 10 minutes.");
            mailSender.send(message);
            logger.debug("OTP email dispatched successfully to {}", email);
        } catch (Exception e) {
             logger.error("Failed to send OTP email to {}", email, e);
             // Consider re-throwing a custom exception if email failure should stop the process
             // throw new RuntimeException("Failed to send OTP email.", e);
        }
    }
}