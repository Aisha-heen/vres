package com.vres.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors; // Import Collectors

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Prefer org.springframework version

import com.vres.dto.UserResponse; // Import UserResponse
import com.vres.entity.Beneficiaries;
import com.vres.entity.Projects;
import com.vres.entity.Users; // Import Users
import com.vres.repository.BeneficiariesRepository;
import com.vres.repository.ProjectsRepository;
import com.vres.repository.UsersRepository; // Import UsersRepository

import jakarta.persistence.EntityNotFoundException;

@Service
public class BeneficiaryService {

    private static final Logger logger = LoggerFactory.getLogger(BeneficiaryService.class);

    @Autowired
    private BeneficiariesRepository beneficiariesRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private UserService userService; // Injected UserService

    @Autowired
    private UsersRepository usersRepository; // Injected UsersRepository

    @Autowired
    private EmailService emailService; // Injected EmailService


    /**
     * Updates beneficiary approval status after validating project registration end date.
     * If the status is "active" (approved) and no other beneficiaries remain pending,
     * this triggers an email notification to all assigned 'Issuer' users.
     * * @param ids List of beneficiary IDs to update
     * @param status New status ("active" for approved, anything else for unapproved/pending)
     * @return Number of beneficiaries updated
     */
    @Transactional
    public int updateBeneficiaryStatus(List<Integer> ids, String status) {
        if (ids == null || ids.isEmpty()) {
            logger.warn("No beneficiary IDs provided for update.");
            return 0;
        }

        logger.info("Attempting to update status to '{}' for {} beneficiaries.", status, ids.size());

        List<Beneficiaries> beneficiariesToUpdate = beneficiariesRepository.findAllById(ids);
        if (beneficiariesToUpdate.isEmpty()) {
            logger.error("No beneficiaries found for IDs: {}", ids);
            throw new EntityNotFoundException("No beneficiaries found for the provided IDs.");
        }

        // --- Core Validation Checks ---
        Beneficiaries firstBeneficiary = beneficiariesToUpdate.get(0);
        Projects project = firstBeneficiary.getProject();

        if (project == null) {
            logger.error("Beneficiary {} is not linked to any project.", firstBeneficiary.getId());
            throw new IllegalStateException("Beneficiary with ID " + firstBeneficiary.getId() + " is not associated with a project.");
        }
        
        int projectId = project.getId(); // Get project ID for later use

        if (project.getEnd_date() == null) {
            logger.error("Project {} does not have an end date set.", project.getId());
            throw new IllegalStateException("Project registration end date is not set. Cannot approve beneficiaries yet.");
        }

        LocalDate today = LocalDate.now();
        LocalDate registrationEndDate = project.getEnd_date().toLocalDate();

        // ⚠️ Logic check: Prevent status update before registration closes
        if (today.isBefore(registrationEndDate)) {
            logger.warn("Attempt to update status before project registration end date {}.", registrationEndDate);
            throw new IllegalStateException("Cannot update beneficiaries yet. The registration period ends on " + registrationEndDate + ".");
        }
        // --- End Validation Checks ---


        boolean isApproved = "active".equalsIgnoreCase(status);
        for (Beneficiaries beneficiary : beneficiariesToUpdate) {
            // Check to ensure all IDs belong to the same project (important for transaction consistency)
            if (beneficiary.getProject() == null || beneficiary.getProject().getId() != projectId) {
                logger.error("Cannot update beneficiaries from different projects in one request. Beneficiary ID {} is not in project ID {}.", beneficiary.getId(), projectId);
                throw new IllegalStateException("Cannot update beneficiaries from different projects in one request.");
            }
            beneficiary.setIs_approved(isApproved);
        }

        beneficiariesRepository.saveAll(beneficiariesToUpdate);
        logger.info("Updated {} beneficiaries for project ID {} to status '{}'.", beneficiariesToUpdate.size(), projectId, status);


        // --- ISSUER NOTIFICATION LOGIC ---
        if (isApproved) {
            // Count remaining pending beneficiaries for the project
            long pendingCount = beneficiariesRepository.countByProjectIdAndIs_approved(projectId, false);
            logger.debug("Project {} now has {} pending beneficiaries.", projectId, pendingCount);

            if (pendingCount == 0) {
                logger.info("All beneficiaries for project {} are now approved. Notifying Issuers.", projectId);
                try {
                    // Fetch all users assigned the 'Issuer' role for this project
                    List<UserResponse> issuerDtos = userService.getAllUsersByRole("Issuer", projectId);
                    
                    if (!issuerDtos.isEmpty()) {
                        List<Integer> issuerIds = issuerDtos.stream().map(UserResponse::getUserId).collect(Collectors.toList());
                        List<Users> issuerUsers = usersRepository.findAllById(issuerIds);

                        for (Users issuer : issuerUsers) {
                            emailService.sendIssuerApprovalDoneEmail(issuer, project);
                        }
                        logger.info("Sent 'Approval Done' notification to {} Issuers for project {}.", issuerUsers.size(), projectId);
                    } else {
                        logger.warn("Beneficiary approval complete, but no Issuers found for project ID {} to notify.", projectId);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send 'Approval Done' email to Issuers for project ID {}: {}", projectId, e.getMessage());
                    // Do not throw an exception here, email failure shouldn't rollback status change
                }
            }
        }

        return beneficiariesToUpdate.size();
    }
}