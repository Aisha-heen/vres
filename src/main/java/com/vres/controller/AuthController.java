package com.vres.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// --- NEW IMPORT ---
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vres.dto.ForgotPasswordRequest;
import com.vres.dto.GenericResponse;
import com.vres.dto.LoginRequest;
import com.vres.dto.LoginResponse;
import com.vres.dto.ResetPasswordRequest;
import com.vres.service.AuthService;

@RestController
@RequestMapping("/vres/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            // This is the specific exception for bad email/password
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (RuntimeException e) {
            // This catches your other errors, like "Account not active"
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
    
    @PostMapping("/vendor-login")
    public ResponseEntity<?> vendorLogin(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.vendorLogin(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (RuntimeException e) {
            // This catches "Access denied. This login is for vendors only."
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail()); 
        return ResponseEntity.ok(new GenericResponse("If an account with that email exists, a password reset OTP has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request); 
            return ResponseEntity.ok(new GenericResponse("Your password has been reset successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}