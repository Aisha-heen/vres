package com.vres.exception; // Or your actual exception package

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException; // Import BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.vres.dto.StructuredErrorResponse;

import jakarta.persistence.EntityNotFoundException;


@ControllerAdvice
public class GlobalExceptionHandler {

    // Define a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles EntityNotFoundException - used here for "User not found".
     * Returns 404 Not Found.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StructuredErrorResponse> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("Resource not found exception: {} for path: {}", ex.getMessage(), path); // Use logger
        StructuredErrorResponse error = new StructuredErrorResponse(
                "RESOURCE_NOT_FOUND",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND); // 404
    }

    /**
     * --- ADDED Handler for BadCredentialsException ---
     * Handles incorrect password or other auth failures marked as bad credentials.
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StructuredErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("Bad credentials exception: {} for path: {}", ex.getMessage(), path); // Use logger
        StructuredErrorResponse error = new StructuredErrorResponse(
                "INVALID_CREDENTIALS",
                ex.getMessage() // Use the message set in AuthService ("Invalid credentials.")
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED); // 401
    }


    /**
     * Handles IllegalStateException (like date validation errors, inactive accounts).
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<StructuredErrorResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("Illegal state exception: {} for path: {}", ex.getMessage(), path); // Use logger
        StructuredErrorResponse error = new StructuredErrorResponse(
                "ILLEGAL_STATE", // Or "VALIDATION_ERROR"
                ex.getMessage() // Include the specific message
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // 400
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StructuredErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        logger.warn("Illegal argument exception: {} for path: {}", ex.getMessage(), path); // Log validation errors
        StructuredErrorResponse error = new StructuredErrorResponse(
                "INVALID_INPUT", // Or "VALIDATION_ERROR"
                ex.getMessage() // Use the message from the exception (e.g., "Invalid phone number...")
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }
    /**
     * General fallback handler.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StructuredErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        // Log unexpected errors at a higher level (ERROR) with stack trace
        logger.error("Unhandled Exception caught for path: {}", path, ex);
        StructuredErrorResponse error = new StructuredErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support." // Generic message for safety
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }

}