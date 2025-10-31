package com.vres.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date; 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.WriterException;
import com.vres.dto.ApproverPairDto;
import com.vres.dto.BeneficiaryDto;
import com.vres.dto.ProjectDetailsCreationRequest;
import com.vres.dto.ProjectInitiationRequest;
import com.vres.dto.ProjectResponse;
import com.vres.dto.ProjectVoucherDto;
import com.vres.dto.UnassignedUserDto;
import com.vres.dto.UserResponse;
import com.vres.dto.VendorAssignmentDto;
import com.vres.dto.VoucherCreationRequest;
import com.vres.entity.Beneficiaries;
import com.vres.entity.Department;
import com.vres.entity.ProjectUser;
import com.vres.entity.Projects;
import com.vres.entity.Roles;
import com.vres.entity.Users;
import com.vres.entity.Vouchers;
import com.vres.generator.CodeGeneratorService;
import com.vres.generator.QRCodeGenerator;
import com.vres.repository.BeneficiariesRepository;
import com.vres.repository.DepartmentRepository;
import com.vres.repository.ProjectUserRepository;
import com.vres.repository.ProjectsRepository;
import com.vres.repository.RolesRepository;
import com.vres.repository.UsersRepository;
import com.vres.repository.VouchersRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired private EmailService emailService; // <-- ADDED for email notifications
    @Autowired private ProjectsRepository projectsRepository;
    @Autowired private UsersRepository usersRepository;
    @Autowired private BeneficiariesRepository beneficiariesRepository;
    @Autowired private RolesRepository rolesRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private ProjectUserRepository projectUserRepository;
    @Autowired private VouchersRepository vouchersRepository;
    @Autowired private CodeGeneratorService codeGeneratorService;
    @Autowired private S3Service s3Service;
    @Autowired private BrevoSmsService brevoSmsService;

    /**
     * Retrieves projects where the user is assigned as the Project Coordinator.
     */
    public List<ProjectResponse> getProjectsByCoordinator(int userId) {
        logger.info("Fetching projects for coordinator user ID: {}", userId);
        List<Projects> projects = projectsRepository.findProjectsByCoordinatorId(userId);
        logger.debug("Found {} projects for coordinator ID {}", projects.size(), userId);

        return projects.stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves users assigned as Vendors for a specific project.
     */
    public List<UserResponse> getVendorsForProject(int projectId) {
        Roles vendorRole = rolesRepository.findByName("Vendor")
                .orElseThrow(() -> new EntityNotFoundException("Role 'Vendor' not found."));
        
        List<ProjectUser> vendorLinks = projectUserRepository.findByProjectId(projectId).stream()
                .filter(pu -> pu.getRole() != null && pu.getRole().getId() == vendorRole.getId())
                .collect(Collectors.toList());

        if (vendorLinks.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Integer> vendorUserIds = vendorLinks.stream()
                .map(ProjectUser::getUserId)
                .collect(Collectors.toList());

        List<Users> vendors = usersRepository.findAllById(vendorUserIds);
        
        return vendors.stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Initiates a new project with a 'Draft' status and links the coordinator user.
     * Includes logic to send an email to the Project Coordinator.
     */
    @Transactional
    public ProjectResponse initiateProject(ProjectInitiationRequest request) {
        logger.info("Initiating project with title: {}", request.getTitle());

        Users coordinatorUser = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("Coordinator user not found with email: {}", request.getEmail());
                    return new EntityNotFoundException("Coordinator user not found with email: " + request.getEmail());
                });

        // Check for existing project title
        Optional<Projects> existingProjectByTitle = projectsRepository.findByTitle(request.getTitle());
        if(existingProjectByTitle.isPresent()) {
            logger.error("Project initiation failed: Title already exists - {}", request.getTitle());
            throw new EntityNotFoundException("Project with this Title already exists.");
        }

        // Check if user has the coordinator role
        Roles coordinatorRole = rolesRepository.findByName("Project Coordinator")
                .orElseThrow(() -> {
                    logger.error("Configuration error: Role 'Project Coordinator' not found.");
                    return new EntityNotFoundException("Role 'Project Coordinator' not found.");
                });
        
        boolean isCoordinator = projectUserRepository.findAllByUserId(coordinatorUser.getId()).stream()
            .anyMatch(pu -> pu.getRole() != null && pu.getRole().equals(coordinatorRole));

        if (!isCoordinator) {
             logger.error("Project initiation failed: User {} is not a Project Coordinator.", request.getEmail());
             throw new IllegalStateException("User " + request.getEmail() + " is not registered as a Project Coordinator.");
        }


        // Create and save the new Project
        Projects newProject = new Projects();
        newProject.setTitle(request.getTitle());
        newProject.setStatus("Draft");
        Projects savedProject = projectsRepository.save(newProject);
        logger.info("Project '{}' created with ID: {}", savedProject.getTitle(), savedProject.getId());

        // Create ProjectUser link
        ProjectUser newAssignment = new ProjectUser();
        newAssignment.setUserId(coordinatorUser.getId());
        newAssignment.setProject(savedProject);
        newAssignment.setRole(coordinatorRole);
        newAssignment.setCreatedAt(LocalDateTime.now());
        projectUserRepository.save(newAssignment);
        logger.info("Project Coordinator {} assigned to project ID: {}", coordinatorUser.getEmail(), savedProject.getId());

        // Send email to coordinator (Added from the second file)
        try {
            emailService.sendCoordinatorAssignmentEmail(coordinatorUser, savedProject);
        } catch (Exception e) {
            logger.error("Failed to send coordinator assignment email to {}: {}", coordinatorUser.getEmail(), e.getMessage());
            // Do not fail the transaction, just log the error
        }

        return convertToProjectResponse(savedProject);
    }
    
    /**
     * Gets a list of all users, typically for assigning them to project roles.
     */
    public List<UnassignedUserDto> getUnassignedProjectUsers() {
        List<Users> users = usersRepository.findAll(); 
        logger.debug("Fetching all users for assignment dropdown.");
        
        return users.stream()
                .map(user -> new UnassignedUserDto(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Defines/Updates the project details, including dates, departments (Maker/Checker), Issuers, and Vendors.
     * Triggers role assignment and potential notification emails through the helper method.
     */
    @Transactional
    public void defineProjectDetails(int projectId, ProjectDetailsCreationRequest request) {
        logger.info("Defining/Updating details for project ID: {}", projectId);

        // --- 1. Load the Project ---
        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> {
                     logger.error("Project not found with ID: {}", projectId);
                     return new EntityNotFoundException("Project not found with id: " + projectId);
                });

        // --- 2. Update Basic Project Details (Name, Description, Dates) ---
        boolean projectDetailsChanged = false;

        // Update Name if provided and different
        if (request.getProjectName() != null && !request.getProjectName().isBlank() && !request.getProjectName().equals(project.getTitle())) {
            // Check for title conflict before updating
            Optional<Projects> existingProject = projectsRepository.findByTitle(request.getProjectName());
            if (existingProject.isPresent() && existingProject.get().getId() != projectId) {
                logger.error("Failed to update details: Project title '{}' already exists for project ID {}", request.getProjectName(), existingProject.get().getId());
                throw new EntityNotFoundException("Project with this Title already exists");
            }
            project.setTitle(request.getProjectName());
            logger.debug("Updating project title to '{}'", request.getProjectName());
            projectDetailsChanged = true;
        }

        // Update Description if provided
        if (request.getProjectDescription() != null && !request.getProjectDescription().equals(project.getDescription())) {
            project.setDescription(request.getProjectDescription());
            logger.debug("Updating project description");
            projectDetailsChanged = true;
        }

        // Validate and update dates if provided
        LocalDate startDate = request.getStartDate();
        LocalDate registrationEndDate = request.getRegistrationEndDate();
        LocalDate currentStartDate = (project.getStart_date() != null) ? project.getStart_date().toLocalDate() : null;
        LocalDate currentEndDate = (project.getEnd_date() != null) ? project.getEnd_date().toLocalDate() : null;

        LocalDate finalStartDate = (startDate != null) ? startDate : currentStartDate;
        LocalDate finalEndDate = (registrationEndDate != null) ? registrationEndDate : currentEndDate;

        // Ensure dates are not null and end date is not before start date
        if (finalStartDate == null || finalEndDate == null) {
            logger.error("Project details update failed: Start date or registration end date is null/missing for project ID {}", projectId);
            throw new IllegalArgumentException("Start date and registration end date must be provided.");
        }
        if (finalEndDate.isBefore(finalStartDate)) {
            logger.error("Project details update failed: Registration end date {} is before start date {} for project ID {}", finalEndDate, finalStartDate, projectId);
            throw new IllegalArgumentException("Registration end date cannot be before the project start date.");
        }

        // Update dates if they have changed
        if (currentStartDate == null || !finalStartDate.equals(currentStartDate)) { // Improved check for initial null
            project.setStart_date(Date.valueOf(finalStartDate));
            logger.debug("Updating project start date to {}", finalStartDate);
            projectDetailsChanged = true;
        }
        if (currentEndDate == null || !finalEndDate.equals(currentEndDate)) { // Improved check for initial null
            project.setEnd_date(Date.valueOf(finalEndDate));
            logger.debug("Updating project registration end date to {}", finalEndDate);
            projectDetailsChanged = true;
        }

        // Update status only if details are being defined/updated and it's not already 'In Progress'
        if (projectDetailsChanged && !"In Progress".equals(project.getStatus())) {
             project.setStatus("In Progress");
             logger.debug("Updating project status to 'In Progress'");
             projectDetailsChanged = true; // Ensure save happens if only status changes
        }

        // Save basic project details changes if anything was modified
        if (projectDetailsChanged) {
            projectsRepository.save(project);
            logger.info("Basic project details updated for project ID: {}", projectId);
        } else {
             logger.info("No changes detected for basic project details (name, description, dates) for project ID: {}", projectId);
        }

        // --- 3. ADD New Departments (Makers & Checkers) ---
        if (request.getApprovers() != null && !request.getApprovers().isEmpty()) {
            logger.info("Processing {} NEW approver pairs to ADD for project ID: {}", request.getApprovers().size(), projectId);
            Roles makerRole = rolesRepository.findByName("Maker").orElseThrow(() -> new EntityNotFoundException("Role Maker not found"));
            Roles checkerRole = rolesRepository.findByName("Checker").orElseThrow(() -> new EntityNotFoundException("Role Checker not found"));

            List<Department> newDepartments = new ArrayList<>();
            for (ApproverPairDto pair : request.getApprovers()) {
                logger.debug("Creating department and assigning Maker ID {} and Checker ID {}", pair.getMakerId(), pair.getCheckerId());
                Department dept = new Department();
                dept.setProject(project); 
                dept.setMakerId(pair.getMakerId());
                dept.setCheckerId(pair.getCheckerId());
                newDepartments.add(dept);
                
                // Assign/Update roles via ProjectUser table and send notifications
                updateUserRoleInProject(projectId, pair.getMakerId(), makerRole.getId());
                updateUserRoleInProject(projectId, pair.getCheckerId(), checkerRole.getId());
            }
            if (!newDepartments.isEmpty()) {
                departmentRepository.saveAll(newDepartments);
                logger.info("Saved {} NEW departments for project ID: {}", newDepartments.size(), projectId);
            }
        } else {
             logger.info("No new approver pairs provided in request. Existing departments remain unchanged for project ID: {}", projectId);
        }

        // --- 4. ADD/UPDATE Issuers ---
        if (request.getIssuerIds() != null && !request.getIssuerIds().isEmpty()) {
             Roles issuerRole = rolesRepository.findByName("Issuer").orElseThrow(() -> new EntityNotFoundException("Role Issuer not found"));
             logger.info("Processing {} issuer assignments for project ID: {}", request.getIssuerIds().size(), projectId);
            for (Integer issuerId : request.getIssuerIds()) {
                 logger.debug("Assigning/Updating Issuer role for user ID {}", issuerId);
                updateUserRoleInProject(projectId, issuerId, issuerRole.getId());
            }
        } else {
             logger.info("No issuer IDs list provided in request. Existing issuers remain unchanged for project ID: {}", projectId);
        }


        // --- 5. ADD/UPDATE Vendors ---
        if (request.getVendors() != null && !request.getVendors().isEmpty()) {
            Roles vendorRole = rolesRepository.findByName("Vendor").orElseThrow(() -> new EntityNotFoundException("Role Vendor not found"));
            logger.info("Processing {} vendor assignments for project ID: {}", request.getVendors().size(), projectId);
            
            List<Integer> vendorUserIds = request.getVendors().stream().map(VendorAssignmentDto::getUserId).collect(Collectors.toList());
            Map<Integer, Users> userMap = usersRepository.findAllById(vendorUserIds).stream()
                     .collect(Collectors.toMap(Users::getId, Function.identity()));
            
            for (VendorAssignmentDto vendorDto : request.getVendors()) {
                 logger.debug("Assigning/Updating Vendor role for user ID {} with GST {}", vendorDto.getUserId(), vendorDto.getGst());
                // Assign role and set status (updateUserRoleInProject handles create/update and notification)
                updateUserRoleInProject(projectId, vendorDto.getUserId(), vendorRole.getId(), 1); // 1 = active/approved

                // Update GST/Address on Users entity
                Users userToUpdate = userMap.get(vendorDto.getUserId());
                if (userToUpdate != null) {
                    userToUpdate.setGst(vendorDto.getGst());
                    userToUpdate.setAddress(vendorDto.getAddress());
                    usersRepository.save(userToUpdate);
                     logger.debug("Updated GST/Address for vendor user ID {}", userToUpdate.getId());
                } else {
                     logger.warn("Vendor user ID {} specified for assignment not found in database.", vendorDto.getUserId());
                }
            }
        } else {
             logger.info("No vendors list provided in request. Existing vendors remain unchanged for project ID: {}", projectId);
        }
        
        logger.info("Finished defining/updating details for project ID: {}", projectId);
    }
    
    // Overloaded private method for roles that don't need vendor status
    private void updateUserRoleInProject(int projectId, int userId, int roleId) {
        updateUserRoleInProject(projectId, userId, roleId, null);
    }

    /**
     * Helper method to assign or update a user's role and vendor status within a project.
     * Includes logic to send an assignment email notification if a new assignment or role change occurs.
     */
    private void updateUserRoleInProject(int projectId, int userId, int roleId, Integer vendorStatus) {
        logger.debug("Updating/Creating role assignment for user ID {}, project ID {}, role ID {}", userId, projectId, roleId);
        
        Optional<ProjectUser> linkOptional = projectUserRepository.findAllByUserId(userId).stream()
                .filter(pu -> pu.getProject() != null && pu.getProject().getId() == projectId)
                .findFirst();

        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> {
                     logger.error("Project not found with ID {} while creating role assignment.", projectId);
                     return new EntityNotFoundException("Project not found with id: " + projectId);
                });
        
        Roles role = rolesRepository.findById(roleId)
                .orElseThrow(() -> {
                     logger.error("Role not found with ID: {}", roleId);
                     return new EntityNotFoundException("Role not found with id: " + roleId);
                });

        boolean isNewAssignmentOrRoleChange = false; // Flag to track if we should send a notification

        if (linkOptional.isPresent()) {
            // --- UPDATE EXISTING LINK ---
            ProjectUser linkToUpdate = linkOptional.get();
            // Check if the role is actually changing
            if (linkToUpdate.getRole() == null || linkToUpdate.getRole().getId() != roleId) {
                logger.info("Updating existing role assignment ID {} for user {} in project {}", linkToUpdate.getId(), userId, projectId);
                linkToUpdate.setRole(role);
                isNewAssignmentOrRoleChange = true; // Treat role change as a new assignment
            } else {
                logger.info("Role for user {} in project {} is already set. No role change.", userId, projectId);
            }
            
            if (vendorStatus != null) {
                linkToUpdate.setVendorStatus(vendorStatus);
            }
            projectUserRepository.save(linkToUpdate); 
            logger.debug("Role assignment updated.");
            
        } else {
            // --- CREATE NEW LINK ---
             logger.info("Creating new role assignment for user {} in project {}", userId, projectId);
            
            ProjectUser newLink = new ProjectUser();
            newLink.setUserId(userId);
            newLink.setProject(project);
            newLink.setRole(role);
            if (vendorStatus != null) {
                newLink.setVendorStatus(vendorStatus);
            }
            
            // Set the createdAt timestamp for the new record
            newLink.setCreatedAt(LocalDateTime.now());  
            
            projectUserRepository.save(newLink); 
            isNewAssignmentOrRoleChange = true; // This is a new assignment
            logger.debug("New role assignment created.");
        }

        // --- SEND NOTIFICATION ON NEW ASSIGNMENT/ROLE CHANGE (Logic from second file) ---
        if (isNewAssignmentOrRoleChange) {
            Users user = usersRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.error("Could not find user with ID {} to send assignment email.", userId);
                return; // Can't send email if user not found
            }
            
            try {
                String roleName = role.getName();
                logger.info("Sending assignment email to {} for role {}", user.getEmail(), roleName);
                if ("Maker".equalsIgnoreCase(roleName)) {
                    emailService.sendMakerAssignmentEmail(user, project);
                }
                else if ("Checker".equalsIgnoreCase(roleName) || "Issuer".equalsIgnoreCase(roleName) || "Vendor".equalsIgnoreCase(roleName)) {
                    emailService.sendGenericAssignmentEmail(user, project, roleName);
                }
                // Project Coordinator is handled in initiateProject()
            } catch (Exception e) {
                 logger.error("Failed to send assignment email to {}: {}", user.getEmail(), e.getMessage());
                 // Do not fail the transaction
            }
        }
    }
    
    /**
     * Retrieves all projects in the system.
     */
    public List<ProjectResponse> getAllProjects() {
        return projectsRepository.findAll().stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Processes the uploaded Excel files to import new beneficiaries for a project.
     * Includes validation against the registration end date.
     */
    @Transactional
    public void processBeneficiaryUpload(int projectId, int departmentId, MultipartFile[] files) {
        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        // Validation logic for registration end date
        if (project.getEnd_date() == null) {
            throw new IllegalStateException("Project registration end date is not set. Cannot upload beneficiaries.");
        }
        
        LocalDate today = LocalDate.now();
        LocalDate registrationEndDate = project.getEnd_date().toLocalDate(); 
        
        if (today.isAfter(registrationEndDate)) {
            throw new IllegalStateException("Cannot upload beneficiaries. The registration period for this project ended on " + registrationEndDate.toString() + ".");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + departmentId));
        
        if (files == null || files.length == 0) throw new IllegalStateException("No files uploaded.");
        
        List<Beneficiaries> allBeneficiariesToSave = new ArrayList<>();
        int rowNum = 0; // For logging row numbers
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            logger.info("Processing file: {}", file.getOriginalFilename());
            try (InputStream inputStream = file.getInputStream()) {
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                rowNum = 0; // Reset for each sheet/file
                for (Row row : sheet) {
                    rowNum = row.getRowNum();
                    if (rowNum == 0) continue; // Skip header row
                    
                    String name = getCellValueAsString(row.getCell(0));
                    String phone = getCellValueAsString(row.getCell(1));
                    
                    if (name != null && !name.trim().isEmpty() && phone != null && !phone.trim().isEmpty()) {
                        Beneficiaries newBeneficiary = new Beneficiaries();
                        newBeneficiary.setName(name);
                        newBeneficiary.setPhone(phone); 
                        newBeneficiary.setIs_approved(false);
                        newBeneficiary.setProject(project);
                        newBeneficiary.setDepartment(department);
                        allBeneficiariesToSave.add(newBeneficiary);
                    } else {
                         logger.warn("Skipping row {} in file {}: Missing name or phone.", rowNum + 1, file.getOriginalFilename());
                    }
                }
                 logger.info("Processed {} rows from file: {}", rowNum, file.getOriginalFilename());
            } catch (IOException e) {
                 logger.error("Failed to parse Excel file {} at row {}", file.getOriginalFilename(), rowNum + 1, e);
                throw new RuntimeException("Failed to parse Excel file " + file.getOriginalFilename(), e);
            }
        }
        if (!allBeneficiariesToSave.isEmpty()){
             logger.info("Saving {} beneficiaries from uploaded files for project ID {}", allBeneficiariesToSave.size(), projectId);
             beneficiariesRepository.saveAll(allBeneficiariesToSave);
        } else {
             logger.warn("No valid beneficiaries found in uploaded files for project ID {}", projectId);
        }
    }

    /**
     * Helper method to safely read cell values as String, handling various cell types.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        DataFormatter formatter = new DataFormatter();
        String cellValue = formatter.formatCellValue(cell).trim();

        // Extra check: If original cell was string and starts with '+', ensure it's kept.
        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().startsWith("+")) {
             return cell.getStringCellValue().trim();
        }

        // Handle potential formulas
        if (cell.getCellType() == CellType.FORMULA) {
            try {
                 return formatter.formatCellValue(cell, cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator()).trim();
            } catch (Exception e) {
                 logger.error("Error evaluating formula cell: {}", e.getMessage()); 
                 return null;
            }
        }
        return cellValue;
    }

    public List<BeneficiaryDto> getApprovedBeneficiariesForProject(int projectId, Integer departmentId) {
        List<Beneficiaries> beneficiaries = beneficiariesRepository.findApprovedBeneficiariesWithoutVoucher(projectId, departmentId);
        return beneficiaries.stream().map(this::convertBeneficiaryToDto).collect(Collectors.toList());
    }

    public List<BeneficiaryDto> getBeneficiariesForProjectAndDepartment(int projectId, String status, Integer departmentId) {
        Boolean isApproved = "active".equalsIgnoreCase(status) ? true : ("pending_approval".equalsIgnoreCase(status) ? false : null);
        List<Beneficiaries> beneficiaries = beneficiariesRepository.findByProjectAndFilters(projectId, isApproved, departmentId);
        return beneficiaries.stream().map(this::convertBeneficiaryToDto).collect(Collectors.toList());
    }

    public List<BeneficiaryDto> getBeneficiariesForProject(int projectId, String status) {
        Boolean isApproved = "active".equalsIgnoreCase(status) ? true : ("pending_approval".equalsIgnoreCase(status) ? false : null);
        // Using the more flexible findByProjectAndFilters to support both department-filtered and non-filtered queries
        List<Beneficiaries> beneficiaries = beneficiariesRepository.findByProjectAndFilters(projectId, isApproved, null); 
        return beneficiaries.stream().map(this::convertBeneficiaryToDto).collect(Collectors.toList());
    }

    public Optional<Projects> getProject(int projectId) {
        return projectsRepository.findById(projectId);
    }

    /**
     * Creates vouchers for approved beneficiaries after the registration period has ended.
     */
    @Transactional
    public void createVouchersForProject(int projectId, VoucherCreationRequest request) {
         logger.info("Attempting to create vouchers for project ID: {}", projectId);
        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        // --- Validation 1: Registration End Date Check ---
        if (project.getEnd_date() == null) {
             logger.error("Voucher creation failed: Project registration end date is not set for project ID {}", projectId);
            throw new IllegalStateException("Project registration end date is not set. Cannot create vouchers yet.");
        }
        LocalDate today = LocalDate.now();
        LocalDate registrationEndDate = project.getEnd_date().toLocalDate();
        if (today.isBefore(registrationEndDate)) {
             logger.error("Voucher creation failed: Registration period has not ended yet for project ID {}. Ends on {}", projectId, registrationEndDate);
            throw new IllegalStateException("Cannot create vouchers yet. The registration period ends on " + registrationEndDate.toString() + ".");
        }
        
        // --- Validation 2: Voucher Validity Dates Check ---
        LocalDate requestedStartDate = request.getValidityStart();
        LocalDate requestedEndDate = request.getValidityEnd();

        if (requestedStartDate == null || requestedEndDate == null) {
             logger.error("Voucher creation failed: Voucher validity start or end date is null for project ID {}", projectId);
             throw new IllegalArgumentException("Voucher validity start date and end date cannot be empty.");
        }
        if (requestedEndDate.isBefore(requestedStartDate)) {
            logger.error("Voucher creation failed: Validity end date {} is before start date {} for project ID {}", requestedEndDate, requestedStartDate, projectId);
            throw new IllegalArgumentException("Voucher validity end date cannot be before the validity start date.");
        }
        
        // Load beneficiaries
        List<Beneficiaries> beneficiaries = beneficiariesRepository.findAllById(request.getBeneficiaryIds());
        if (beneficiaries.size() != request.getBeneficiaryIds().size()) {
             logger.error("Voucher creation failed: One or more beneficiary IDs provided were not found.");
            throw new EntityNotFoundException("One or more beneficiaries could not be found.");
        }
        logger.info("Found {} beneficiaries to issue vouchers for project ID {}", beneficiaries.size(), projectId);

        // Update project VOUCHER details
        project.setVoucher_points(request.getVoucherPoints());
        Date validityStartDate = Date.valueOf(requestedStartDate); 
        Date validityEndDate = Date.valueOf(requestedEndDate);   
        project.setVoucher_valid_from(validityStartDate);
        project.setVoucher_valid_till(validityEndDate);
        projectsRepository.save(project);
        logger.info("Updated voucher details (points, validity) for project ID {}", projectId);

        // Update vendor status if vendors are provided
        if (request.getVendors() != null && !request.getVendors().isEmpty()) {
             logger.info("Activating {} vendors for voucher redemption in project ID {}", request.getVendors().size(), projectId);
            List<ProjectUser> vendorMappings = projectUserRepository.findByProjectIdAndUserIdIn(projectId, request.getVendors());
            for (ProjectUser mapping : vendorMappings) {
                mapping.setVendorStatus(1);
            }
            projectUserRepository.saveAll(vendorMappings);
        }

        int successCount = 0;
        int failureCount = 0;
        // Iterate through beneficiaries to create vouchers and send SMS
        for (Beneficiaries beneficiary : beneficiaries) {
            
            String phoneNumber = beneficiary.getPhone();
             // Simple phone number check to prevent sending SMS to invalid numbers
            if (phoneNumber == null || !phoneNumber.startsWith("+")) {
                 logger.warn("Skipping SMS for beneficiary ID {}: Invalid or missing phone number format. Found: {}", beneficiary.getId(), phoneNumber);
            }

            String uniqueVoucherCode = null;
            try {
                 // Generate unique voucher code
                 do {
                     uniqueVoucherCode = codeGeneratorService.generateUniqueCode();
                 } while (vouchersRepository.findByStringCode(uniqueVoucherCode).isPresent());
                 logger.debug("Generated unique code {} for beneficiary ID {}", uniqueVoucherCode, beneficiary.getId());

                 // Generate QR code and upload
                 byte[] qrCodeBytes = QRCodeGenerator.generateQRCodeImage(uniqueVoucherCode, 300, 300);
                 String qrLink = s3Service.uploadQRCode(qrCodeBytes, uniqueVoucherCode);
                 logger.debug("QR code generated and uploaded for {}: {}", uniqueVoucherCode, qrLink);

                 // Create and save voucher
                 Vouchers newVoucher = new Vouchers();
                 newVoucher.setProject(project);
                 newVoucher.setBeneficiary(beneficiary);
                 newVoucher.setStatus("ISSUED");
                 newVoucher.setStringCode(uniqueVoucherCode);
                 newVoucher.setQrCodeLink(qrLink);
                 // Add issuedAt timestamp (from the first file's logic)
                 newVoucher.setIssuedAt(LocalDate.now()); 
                 vouchersRepository.save(newVoucher);
                 logger.info("Voucher {} created successfully for beneficiary ID {}", uniqueVoucherCode, beneficiary.getId());

                 // Send SMS only if phone number was valid (starting with '+')
                 if (phoneNumber != null && phoneNumber.startsWith("+")) {
                     brevoSmsService.sendVoucherSms(
                         phoneNumber,
                         uniqueVoucherCode,
                         qrLink,
                         validityStartDate,
                         validityEndDate,
                         request.getVoucherPoints(),
                         project.getTitle()
                     );
                 } else {
                      logger.warn("Skipped sending SMS for voucher {} due to invalid phone number.", uniqueVoucherCode);
                 }
                 successCount++;

            } catch (WriterException | IOException e) {
                 logger.error("Failed to generate/upload QR code for voucher {} (Beneficiary ID {}): {}", uniqueVoucherCode != null ? uniqueVoucherCode : "N/A", beneficiary.getId(), e.getMessage());
                 failureCount++;
            } catch (Exception e) {
                 logger.error("An unexpected error occurred while creating voucher or sending SMS for beneficiary ID {}: {}", beneficiary.getId(), e.getMessage(), e);
                 failureCount++;
            }
        }
         logger.info("Voucher creation process completed for project ID {}. Success: {}, Failures: {}", projectId, successCount, failureCount);
    }
    
    /**
     * Retrieves all vouchers for a given project and maps them to DTOs.
     */
    public List<ProjectVoucherDto> getVouchersForProject(int projectId) {
        if (!projectsRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }

        List<Vouchers> vouchers = vouchersRepository.findByProjectId(projectId);

        return vouchers.stream().map(voucher -> {
            ProjectVoucherDto dto = new ProjectVoucherDto();
            
            // Map Voucher details
            dto.setQrCodeLink(voucher.getQrCodeLink());
            dto.setVoucherStatus(voucher.getStatus());
            dto.setStringCode(voucher.getStringCode());
            dto.setIssuedAt(voucher.getIssuedAt());

            // Map Beneficiary details, handling potential data integrity issues (from second file)
            try {
                 Beneficiaries beneficiary = voucher.getBeneficiary(); 
                 if (beneficiary != null) {
                     dto.setBeneficiaryName(beneficiary.getName());
                     dto.setBeneficiaryPhone( beneficiary.getPhone());
                     dto.setBeneficiaryIsApproved(beneficiary.isIs_approved());
                 } else {
                     dto.setBeneficiaryName("Beneficiary Not Assigned");
                 }
            } catch (EntityNotFoundException e) {
                 logger.warn("Data integrity issue: Voucher ID {} references a missing Beneficiary. {}", 
                             voucher.getId(), e.getMessage());
                 dto.setBeneficiaryName("Missing Beneficiary Data");
                 dto.setBeneficiaryPhone("N/A");
                 dto.setBeneficiaryIsApproved(false);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Counts the number of vouchers for a project grouped by their status.
     * Includes cleaner status grouping (from second file).
     */
    public Map<String, Long> getVoucherStatusCounts(int projectId) {
        List<ProjectVoucherDto> vouchers = this.getVouchersForProject(projectId);
        Map<String, Long> statusCount = vouchers.stream()
                .collect(Collectors.groupingBy(
                        dto -> {
                            String status = dto.getVoucherStatus();
                            if (status == null) {
                                return "UNKNOWN";
                            }
                            return status.toUpperCase();
                        },
                        Collectors.counting()
                ));
        logger.info("Voucher status count calculated for project {}: {}", projectId, statusCount);
        return statusCount;
    }


    /**
     * Helper method to convert the Projects entity to ProjectResponse DTO.
     */
    private ProjectResponse convertToProjectResponse(Projects project) {
        ProjectResponse res = new ProjectResponse();
        res.setProjectId(project.getId());
        res.setTitle(project.getTitle());
        res.setStatus(project.getStatus());
        
        // Find the Project Coordinator ID
        projectUserRepository.findByProjectId(project.getId()).stream()
            .filter(mapping -> mapping.getRole() != null && "Project Coordinator".equals(mapping.getRole().getName()))
            .findFirst()
            .ifPresent(mapping -> res.setCoordinatorId(mapping.getUserId()));
            
        return res;
    }

    /**
     * Helper method to convert the Beneficiaries entity to BeneficiaryDto.
     */
    private BeneficiaryDto convertBeneficiaryToDto(Beneficiaries beneficiary) {
        BeneficiaryDto dto = new BeneficiaryDto();
        dto.setBeneficiaryId(beneficiary.getId());
        dto.setName(beneficiary.getName());
        dto.setPhone(beneficiary.getPhone());
        dto.setStatus(beneficiary.isIs_approved() ? "active" : "pending_approval");
        if (beneficiary.getDepartment() != null) {
            dto.setDepartmentId(beneficiary.getDepartment().getId());
        }
        return dto;
    }
}