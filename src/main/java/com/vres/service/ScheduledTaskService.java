package com.vres.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vres.dto.UserResponse;
import com.vres.entity.Projects;
import com.vres.entity.Users;
import com.vres.repository.ProjectsRepository;
import com.vres.repository.UsersRepository;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired private ProjectsRepository projectsRepository;
    @Autowired private UserService userService; // <-- FIXED: Use UserService
    @Autowired private UsersRepository usersRepository;
    @Autowired private EmailService emailService;

    
    @Scheduled(cron = "0 0 9 * * ?") // 9:00 AM every day
    @Transactional
    public void notifyCheckersForEndedRegistrations() {
        logger.info("Running scheduled task: notifyCheckersForEndedRegistrations...");

        // 1. Find projects that ended yesterday
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Projects> projects = projectsRepository.findByEndDate(Date.valueOf(yesterday));

        if (projects.isEmpty()) {
            logger.info("No projects found with registration end date of {}. Task complete.", yesterday);
            return;
        }

        logger.info("Found {} projects that ended on {}. Processing notifications...", projects.size(), yesterday);

        for (Projects project : projects) {
            try {
                // 2. Find all "Checker" users for this project
                // --- FIXED: Call userService ---
                List<UserResponse> checkerDtos = userService.getAllUsersByRole("Checker", project.getId());

                if (checkerDtos.isEmpty()) {
                    logger.warn("Project ID {} has no Checkers assigned. Cannot send notification.", project.getId());
                    continue; // Go to the next project
                }

                List<Integer> checkerIds = checkerDtos.stream().map(UserResponse::getUserId).collect(Collectors.toList());
                List<Users> checkerUsers = usersRepository.findAllById(checkerIds);

                // 3. Send email to each Checker
                logger.info("Notifying {} Checkers for project ID {}.", checkerUsers.size(), project.getId());
                for (Users checker : checkerUsers) {
                    emailService.sendCheckerRegistrationEndedEmail(checker, project);
                }

                // 4. (Optional) Update project status
                if ("In Progress".equals(project.getStatus())) {
                    project.setStatus("Approval Pending"); // Or a similar status
                    projectsRepository.save(project);
                     logger.info("Updated project ID {} status to 'Approval Pending'.", project.getId());
                }

            } catch (Exception e) {
                logger.error("Failed to process checker notifications for project ID {}: {}", project.getId(), e.getMessage(), e);
                // Continue to the next project
            }
        }
        logger.info("Scheduled task 'notifyCheckersForEndedRegistrations' finished.");
    }
}