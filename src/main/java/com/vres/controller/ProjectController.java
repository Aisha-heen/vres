package com.vres.controller;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.multipart.MultipartFile;

import com.vres.dto.BeneficiaryDto;
import com.vres.dto.CoordinatorDto;
import com.vres.dto.GenericResponse;
import com.vres.dto.ProjectDetailsCreationRequest;
import com.vres.dto.ProjectInitiationRequest;
import com.vres.dto.ProjectResponse;
import com.vres.dto.ProjectVoucherDto;
import com.vres.dto.UnassignedUserDto;
import com.vres.dto.UserResponse;
import com.vres.dto.VoucherCreationRequest;
import com.vres.entity.Projects;
import com.vres.service.ProjectService;
import com.vres.service.UserService;

@RestController
@RequestMapping("/vres/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    // --- NEW ENDPOINT FOR VENDORS ---
    @GetMapping("/{projectId}/vendors")
    public ResponseEntity<List<UserResponse>> getVendorsForProject(@PathVariable int projectId) {
        List<UserResponse> vendors = projectService.getVendorsForProject(projectId);
        return ResponseEntity.ok(vendors);
    }
    // ---------------------------------

    @GetMapping("/by-coordinator/{userId}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByCoordinator(@PathVariable int userId) {
    	List<ProjectResponse> projects = projectService.getProjectsByCoordinator(userId);
    	return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/coordinators")
    public ResponseEntity<List<CoordinatorDto>> getProjectCoordinators() {
        return ResponseEntity.ok(userService.getProjectCoordinators());
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @PostMapping("/initiate")
    public ResponseEntity<ProjectResponse> initiateProject(@RequestBody ProjectInitiationRequest request) {
        ProjectResponse createdProject = projectService.initiateProject(request);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @PutMapping("/{projectId}/details")
    public ResponseEntity<GenericResponse> defineProjectDetails(@PathVariable int projectId, @RequestBody ProjectDetailsCreationRequest request) {
        projectService.defineProjectDetails(projectId, request);
        return ResponseEntity.ok(new GenericResponse("Project details and roles assigned successfully."));
    }
    
    @GetMapping("/{projectId}/unassigned-users")
    public ResponseEntity<List<UnassignedUserDto>> getUnassignedUsers() {
        List<UnassignedUserDto> users = projectService.getUnassignedProjectUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{projectId}/beneficiaries/upload")
    public ResponseEntity<GenericResponse> uploadBeneficiaries(
            @PathVariable int projectId, 
            @RequestParam("department_id") int departmentId,
            @RequestParam("beneficiary_files") MultipartFile[] files) {
        projectService.processBeneficiaryUpload(projectId, departmentId, files);
        return new ResponseEntity<>(new GenericResponse("Files are being processed."), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{projectId}/departments/{departmentId}/beneficiaries")
    public ResponseEntity<List<BeneficiaryDto>> getBeneficiariesForProject(
            @PathVariable int projectId,
            @RequestParam(required = false) String status,
            @PathVariable Integer departmentId) {
        List<BeneficiaryDto> beneficiaries = projectService.getBeneficiariesForProjectAndDepartment(projectId, status, departmentId);
        return ResponseEntity.ok(beneficiaries);
    }

    @GetMapping("/{projectId}/approved-beneficiaries")
    public ResponseEntity<List<BeneficiaryDto>> getApprovedBeneficiaries(@PathVariable int projectId, @RequestParam(required = false) Integer departmentId) {
        List<BeneficiaryDto> beneficiaries = projectService.getApprovedBeneficiariesForProject(projectId, departmentId);
        return ResponseEntity.ok(beneficiaries);
    }

    @GetMapping("/{projectId}/beneficiaries")
    public ResponseEntity<List<BeneficiaryDto>> getBeneficiariesForProject(
            @PathVariable int projectId,
            @RequestParam(required = false) String status){
        List<BeneficiaryDto> beneficiaries = projectService.getBeneficiariesForProject(projectId, status);
        return ResponseEntity.ok(beneficiaries);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Optional<Projects>> getProject(@PathVariable int projectId) {
        Optional<Projects> project = projectService.getProject(projectId);
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{projectId}/vouchers")
    public ResponseEntity<GenericResponse> createVouchers(@PathVariable int projectId, @RequestBody VoucherCreationRequest request) {
        projectService.createVouchersForProject(projectId, request);
        return ResponseEntity.ok(new GenericResponse("Vouchers created successfully."));
    }
    
    @GetMapping("/{projectId}/vouchers")
    public ResponseEntity<List<ProjectVoucherDto>> getVouchersForProject(@PathVariable int projectId) {
        List<ProjectVoucherDto> vouchers = projectService.getVouchersForProject(projectId);
        return ResponseEntity.ok(vouchers);
    }
}