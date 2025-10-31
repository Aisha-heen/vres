package com.vres.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vres.dto.CoordinatorDto;
import com.vres.dto.CoordinatorRegistrationRequest;
import com.vres.dto.GenericResponse;
import com.vres.dto.UserDashboardDto;
import com.vres.dto.UserRegistrationRequest;
import com.vres.dto.UserResponse;
import com.vres.service.UserService;

@RestController
@RequestMapping("/vres/users")
public class UserController {

    @Autowired
    private UserService userService;

    // --- NEW DEDICATED ENDPOINT for Coordinator Registration ---
    @PostMapping("/register-coordinator")
    public ResponseEntity<UserResponse> registerCoordinator(@RequestBody CoordinatorRegistrationRequest request) {
        UserResponse newUser = userService.registerCoordinator(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // This is the existing endpoint for adding a general user to a project
    @PostMapping
    public ResponseEntity<GenericResponse> onboardUser(@RequestBody UserRegistrationRequest request) {
        GenericResponse response = userService.onboardUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // --- Other existing endpoints ---
    
    /**
     * Retrieves a list of users.
     * - If no @RequestParam is provided (GET /vres/users), returns all registered users.
     * - If 'role' and/or 'projectId' are provided, filters the user list based on the criteria.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsersByRole(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer projectId) {
        // The service method now handles returning all users when role and projectId are null.
        List<UserResponse> users = userService.getAllUsersByRole(role, projectId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable int userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<GenericResponse> updateUser(@PathVariable int userId, @RequestBody UserRegistrationRequest request) {
        userService.updateUser(userId, request);
        return ResponseEntity.ok(new GenericResponse("User updated successfully"));
    }

    @GetMapping("/coordinators")
    public ResponseEntity<List<CoordinatorDto>> getAllProjectCoordinators() {
        List<CoordinatorDto> coordinators = userService.getProjectCoordinators();
        return ResponseEntity.ok(coordinators);
    }
    /**
     * NEW ENDPOINT: Retrieves comprehensive data for the User Dashboard (Admin view).
     * GET /vres/users/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<List<UserDashboardDto>> getUserDashboardData() {
        List<UserDashboardDto> users = userService.getUserDashboardData();
        return ResponseEntity.ok(users);
    }
}