package com.vres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vres.dto.DepartmentRequest;
import com.vres.dto.DepartmentResponse;
import com.vres.entity.Department;
import com.vres.entity.Projects;
import com.vres.repository.DepartmentRepository;
import com.vres.repository.ProjectsRepository;
import com.vres.repository.UsersRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    public List<DepartmentResponse> getAllDepartmentsForProject(int projectId) {
        logger.info("Fetching all departments for project ID: {}", projectId);

        List<DepartmentResponse> departments = departmentRepository.findByProjectId(projectId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        logger.debug("Found {} departments for project ID {}", departments.size(), projectId);
        return departments;
    }

    public DepartmentResponse getDepartmentById(int departmentId) {
        logger.info("Fetching department with ID: {}", departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> {
                    logger.error("Department not found with ID: {}", departmentId);
                    return new EntityNotFoundException("Department not found with id: " + departmentId);
                });

        logger.debug("Department found with ID: {}", departmentId);
        return convertToResponse(department);
    }

    public DepartmentResponse onboardDepartment(int projectId, DepartmentRequest request) {
        logger.info("Onboarding new department for project ID: {}", projectId);

        Projects project = projectsRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.error("Project not found with ID: {}", projectId);
                    return new EntityNotFoundException("Project not found with id: " + projectId);
                });

        if (!usersRepository.existsById(request.getCheckerUserId())) {
            logger.error("Checker user not found with ID: {}", request.getCheckerUserId());
            throw new EntityNotFoundException("Checker user not found with id: " + request.getCheckerUserId());
        }

        if (!usersRepository.existsById(request.getMakerUserId())) {
            logger.error("Maker user not found with ID: {}", request.getMakerUserId());
            throw new EntityNotFoundException("Maker user not found with id: " + request.getMakerUserId());
        }

        Department department = new Department();
        department.setCheckerId(request.getCheckerUserId());
        department.setMakerId(request.getMakerUserId());
        department.setProject(project);

        Department savedDepartment = departmentRepository.save(department);
        logger.info("Department onboarded successfully with ID: {}", savedDepartment.getId());

        return convertToResponse(savedDepartment);
    }

    public DepartmentResponse updateDepartment(int departmentId, DepartmentRequest request) {
        logger.info("Updating department with ID: {}", departmentId);

        Department existingDepartment = departmentRepository.findById(departmentId)
                .orElseThrow(() -> {
                    logger.error("Department not found with ID: {}", departmentId);
                    return new EntityNotFoundException("Department not found with id: " + departmentId);
                });

        if (!usersRepository.existsById(request.getCheckerUserId())) {
            logger.error("Checker user not found with ID: {}", request.getCheckerUserId());
            throw new EntityNotFoundException("Checker user not found with id: " + request.getCheckerUserId());
        }

        if (!usersRepository.existsById(request.getMakerUserId())) {
            logger.error("Maker user not found with ID: {}", request.getMakerUserId());
            throw new EntityNotFoundException("Maker user not found with id: " + request.getMakerUserId());
        }

        existingDepartment.setCheckerId(request.getCheckerUserId());
        existingDepartment.setMakerId(request.getMakerUserId());

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        logger.info("Department updated successfully with ID: {}", updatedDepartment.getId());

        return convertToResponse(updatedDepartment);
    }

    private DepartmentResponse convertToResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setDepartmentId(department.getId());
        response.setCheckerUserId(department.getCheckerId());
        response.setMakerUserId(department.getMakerId());
        return response;
    }
}