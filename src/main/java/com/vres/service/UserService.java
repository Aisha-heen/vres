package com.vres.service;

import java.time.LocalDateTime;
import com.vres.dto.UserDashboardDto; // Import the new DTO
import com.vres.dto.UserDashboardDto.UserRoleProjectLink; // Import the nested DTO
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern; // Import Pattern for regex
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vres.dto.CoordinatorDto;
import com.vres.dto.CoordinatorRegistrationRequest;
import com.vres.dto.GenericResponse;
import com.vres.dto.UserRegistrationRequest;
import com.vres.dto.UserResponse;
import com.vres.entity.ProjectUser;
import com.vres.entity.Projects;
import com.vres.entity.Roles;
import com.vres.entity.Users;
import com.vres.repository.ProjectUserRepository;
import com.vres.repository.ProjectsRepository;
import com.vres.repository.RolesRepository;
import com.vres.repository.UsersRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class); // Define Logger

    @Autowired private UsersRepository usersRepository;
    @Autowired private ProjectUserRepository projectUserRepository;
    @Autowired private RolesRepository rolesRepository;
    @Autowired private ProjectsRepository projectsRepository;
    @Autowired private SnsService snsService; // Keep SNS unless instructed otherwise
    @Autowired private PasswordEncoder passwordEncoder;

    // Define a simple regex pattern for 10 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    @Transactional
    public UserResponse registerCoordinator(CoordinatorRegistrationRequest request) {
        logger.info("Attempting to register coordinator with email: {}", request.getEmail());

        // 1. Check if email already exists
        usersRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            logger.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new IllegalStateException("A user with this email already exists.");
        });

        // --- 2. VALIDATE PHONE NUMBER ---
        String phone = request.getPhone();
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            logger.warn("Registration failed: Invalid phone number format for email {}", request.getEmail());
            // Use IllegalArgumentException for invalid input data
            throw new IllegalArgumentException("Invalid phone number format. Please enter exactly 10 digits.");
        }
        // --- END VALIDATION ---

        // 3. Create and save the new user
        Users newUser = new Users();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPhone(phone); // Use the validated phone number
        newUser.setPassword(passwordEncoder.encode("pass1234")); // Encrypt default password
        newUser.setIs_active(true); // Assuming coordinators are active immediately
        newUser.setCreatedAt(LocalDateTime.now());
        Users savedUser = usersRepository.save(newUser);
        logger.info("User created successfully with ID: {}", savedUser.getId());

        // 4. Subscribe email
        try {
            snsService.subscribeEmail(savedUser.getEmail());
            logger.info("Email {} subscribed successfully.", savedUser.getEmail());
        } catch (Exception e) {
             logger.error("Failed to subscribe email {} during coordinator registration.", savedUser.getEmail(), e);
        }

        // 5. Assign the Project Coordinator role
        Roles coordinatorRole = rolesRepository.findByName("Project Coordinator")
                .orElseThrow(() -> {
                     logger.error("Configuration error: Role 'Project Coordinator' not found.");
                     return new EntityNotFoundException("Role 'Project Coordinator' not found.");
                 });

        ProjectUser roleAssignment = new ProjectUser();
        roleAssignment.setUserId(savedUser.getId());
        roleAssignment.setRole(coordinatorRole);
        roleAssignment.setProject(null); // Coordinator role is not tied to a specific project initially
        roleAssignment.setCreatedAt(LocalDateTime.now());
        projectUserRepository.save(roleAssignment);
        logger.info("Project Coordinator role assigned to user ID: {}", savedUser.getId());

        // Use the helper method for consistency
        return convertToUserResponse(savedUser);
    }

    public List<CoordinatorDto> getProjectCoordinators() {
        logger.debug("Fetching all project coordinators.");
        Roles coordinatorRole = rolesRepository.findByName("Project Coordinator")
                .orElseThrow(() -> {
                     logger.error("Configuration error: Role 'Project Coordinator' not found.");
                     return new EntityNotFoundException("Role 'Project Coordinator' not found.");
                 });
        int coordinatorRoleId = coordinatorRole.getId();

        List<ProjectUser> coordinatorLinks = projectUserRepository.findAll().stream()
                // Ensure role is not null before checking ID
                .filter(pu -> pu.getRole() != null && pu.getRole().getId() == coordinatorRoleId)
                .collect(Collectors.toList());

        if (coordinatorLinks.isEmpty()) {
            logger.info("No users found with the Project Coordinator role assignment.");
            return new ArrayList<>();
        }

        List<Integer> coordinatorUserIds = coordinatorLinks.stream()
                .map(ProjectUser::getUserId)
                .distinct()
                .collect(Collectors.toList());
        logger.debug("Found {} distinct user IDs for coordinators.", coordinatorUserIds.size());

        List<Users> coordinators = usersRepository.findAllById(coordinatorUserIds);

        return coordinators.stream()
                .map(this::convertToCoordinatorDto)
                .collect(Collectors.toList());
    }

    private CoordinatorDto convertToCoordinatorDto(Users user) {
        CoordinatorDto dto = new CoordinatorDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        return dto;
    }

    @Transactional
    public GenericResponse onboardUser(UserRegistrationRequest request) {
         logger.info("Onboarding user {} to project ID {}", request.getEmail(), request.getProjectId());
        if (request.getProjectId() == null) {
            logger.error("Onboarding failed: Project ID is null for email {}", request.getEmail());
            throw new IllegalArgumentException("Project ID is required for this operation.");
        }

        // --- ADD PHONE VALIDATION ---
         String phone = request.getPhone();
         if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
             logger.warn("Onboarding failed: Invalid phone number format for email {}", request.getEmail());
             throw new IllegalArgumentException("Invalid phone number format. Please enter exactly 10 digits.");
         }
        // --- END VALIDATION ---

        // Find existing user or create a new one
        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                     logger.info("User {} not found, creating new user.", request.getEmail());
                     Users newUser = new Users();
                     newUser.setEmail(request.getEmail());
                     newUser.setName(request.getName());
                     newUser.setPhone(phone); // Use validated phone
                     newUser.setPassword(passwordEncoder.encode("pass1234")); // Default password
                     newUser.setIs_active(true); // Assuming active on creation
                     newUser.setCreatedAt(LocalDateTime.now());
                     Users savedNewUser = usersRepository.save(newUser);
                     // Subscribe new user's email
                     try {
                        snsService.subscribeEmail(savedNewUser.getEmail());
                        logger.info("New user email {} subscribed.", savedNewUser.getEmail());
                     } catch (Exception e) {
                         logger.error("Failed to subscribe email {} during user onboarding.", savedNewUser.getEmail(), e);
                     }
                     return savedNewUser;
                });

        // Check if user is already linked to this project
        Projects project = projectsRepository.findById(request.getProjectId())
            .orElseThrow(() -> {
                 logger.error("Onboarding failed: Project not found with ID {}", request.getProjectId());
                 return new EntityNotFoundException("Project not found with id: " + request.getProjectId());
            });

        boolean alreadyLinked = projectUserRepository.findAllByUserId(user.getId()).stream()
            .anyMatch(pu -> pu.getProject() != null && pu.getProject().getId() == request.getProjectId());

        if (alreadyLinked) {
             logger.warn("User {} is already assigned to project {}.", user.getEmail(), project.getId());
             return new GenericResponse("User is already assigned to this project.");
        }

        // Link user to the project (without a specific role initially)
        ProjectUser projectLink = new ProjectUser();
        projectLink.setUserId(user.getId());
        projectLink.setProject(project);
        projectLink.setRole(null); // Role assigned later
        projectLink.setVendorStatus(null);
        projectLink.setCreatedAt(LocalDateTime.now());
        projectUserRepository.save(projectLink);
        logger.info("User {} successfully added to project {}.", user.getEmail(), project.getId());

        return new GenericResponse("User has been successfully added to the project.");
    }

    /**
     * Retrieves users based on role and/or project, or all users if no filters are provided.
     */
    public List<UserResponse> getAllUsersByRole(String roleName, Integer projectId) {
        logger.debug("Fetching users with filters - roleName: {}, projectId: {}", roleName, projectId);
        // Case 1: No filters provided. Return ALL registered users using helper.
        if ((roleName == null || roleName.trim().isEmpty()) && projectId == null) {
            logger.debug("No filters applied, returning all users.");
            return getAllUsers();
        }

        // Case 2: Filters provided. Filter based on ProjectUser assignments.
        Stream<ProjectUser> assignmentsStream = projectUserRepository.findAll().stream();

        if (projectId != null) {
             logger.debug("Filtering by projectId: {}", projectId);
            assignmentsStream = assignmentsStream.filter(pu -> pu.getProject() != null && projectId.equals(pu.getProject().getId()));
        }

        if (roleName != null && !roleName.trim().isEmpty()) {
            logger.debug("Filtering by roleName: {}", roleName);
            // Find role outside the stream for efficiency
            Roles role = rolesRepository.findByName(roleName).orElse(null);
            if (role == null) {
                logger.warn("Role '{}' not found when filtering users. Returning empty list.", roleName);
                return Collections.emptyList(); // Role doesn't exist, no users can have it
            }
            // Use role ID for reliable filtering
            assignmentsStream = assignmentsStream.filter(pu -> pu.getRole() != null && role.getId() == pu.getRole().getId());
        }

        // Get distinct user IDs from the filtered assignments
        List<Integer> userIds = assignmentsStream
                .map(ProjectUser::getUserId)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            logger.debug("No user assignments matched the filters.");
            return Collections.emptyList();
        }
        logger.debug("Found {} distinct user IDs matching filters.", userIds.size());

        // Fetch the actual User entities for the found IDs
        List<Users> users = usersRepository.findAllById(userIds);

        // Convert User entities to UserResponse DTOs using the helper
        return users.stream()
                .map(this::convertToUserResponse) // Use helper method
                .collect(Collectors.toList());
    }

    /**
     * Dedicated method to fetch all users in the system.
     * (Added from "My file")
     */
    public List<UserResponse> getAllUsers() {
         logger.debug("Fetching all registered users.");
        return usersRepository.findAll().stream()
                .map(this::convertToUserResponse) // Use helper method
                .collect(Collectors.toList());
    }

    /**
     * Helper method to convert User Entity to UserResponse DTO.
     * (Added from "My file")
     */
    private UserResponse convertToUserResponse(Users user) {
        // Simple mapping, add more fields if UserResponse DTO changes
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    // --- (getUserById and updateUser methods remain unimplemented) ---
     public UserResponse getUserById(int userId) {
         logger.warn("getUserById method called but not implemented.");
         // Basic implementation example:
         // return usersRepository.findById(userId)
         //        .map(this::convertToUserResponse)
         //        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
         return null;
     }

     public void updateUser(int userId, UserRegistrationRequest request) {
         // Implement user update logic if needed (e.g., change name, phone)
         // Remember to handle potential email changes carefully if email is used as login ID
         logger.warn("updateUser method called but not implemented.");
         // Example structure:
         // Users user = usersRepository.findById(userId).orElseThrow(...);
         // if (request.getName() != null) user.setName(request.getName());
         // if (request.getPhone() != null) { /* validate phone */ user.setPhone(request.getPhone()); }
         // usersRepository.save(user);
     }
     /**
      * Retrieves all users and their associated roles and projects for the User Dashboard.
      * @return List of UserDashboardDto
      */
     public List<UserDashboardDto> getUserDashboardData() {
         logger.info("Fetching all users for the dashboard view.");

         // 1. Fetch all Users
         List<Users> allUsers = usersRepository.findAll();
         
         // 2. Fetch all ProjectUser assignments to reduce DB calls in the loop
         // Alternatively, use projectUserRepository.findAllByUserIdIn(userIds) if using Spring Data JPA method names
         List<ProjectUser> allAssignments = projectUserRepository.findAll(); 

         return allUsers.stream()
                 .map(user -> {
                     // Base user data mapping
                     UserDashboardDto dto = new UserDashboardDto(
                         user.getId(), 
                         user.getName(), 
                         user.getEmail(), 
                         user.getPhone(), 
                         user.isIs_active(), 
                         user.getCreatedAt(),
                         user.getGst(),
                         user.getAddress()
                     );
                     
                     // Filter assignments for the current user
                     List<UserRoleProjectLink> userLinks = allAssignments.stream()
                             .filter(pu -> pu.getUserId() == user.getId())
                             .map(pu -> {
                                 String roleName = pu.getRole() != null ? pu.getRole().getName() : "N/A (Project Link)";
                                 String projectName = pu.getProject() != null ? pu.getProject().getTitle() : "System-wide";
                                 return new UserRoleProjectLink(roleName, projectName);
                             })
                             // Use distinct to avoid showing the same role/project link multiple times
                             .distinct() 
                             .collect(Collectors.toList());

                     dto.setAssignments(userLinks);
                     return dto;
                 })
                 .collect(Collectors.toList());
     }
}