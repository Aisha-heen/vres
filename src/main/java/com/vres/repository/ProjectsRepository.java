package com.vres.repository;


import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vres.entity.Projects;

public interface ProjectsRepository extends JpaRepository<Projects, Integer> {
	Optional<Projects> findByTitle(String title);
	// --- NEW QUERY METHOD ---
    @Query("SELECT pu.project FROM ProjectUser pu WHERE pu.userId = :userId AND pu.role.name = 'Project Coordinator'")
    List<Projects> findProjectsByCoordinatorId(@Param("userId") int userId);
    /**
     * Finds all projects where the registration end date matches the given date.
     * --- UPDATED with @Query to fix parsing error ---
     */
    @Query("SELECT p FROM Projects p WHERE p.end_date = :endDate")
    List<Projects> findByEndDate(@Param("endDate") Date endDate);
    
 }
