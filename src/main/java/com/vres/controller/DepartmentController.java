package com.vres.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vres.dto.DepartmentRequest;
import com.vres.dto.DepartmentResponse;
import com.vres.service.DepartmentService;

@RestController
@RequestMapping("/vres/projects/{projectId}/departments") // MODIFIED: Path updated
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartmentsForProject(@PathVariable int projectId) {
        return ResponseEntity.ok(departmentService.getAllDepartmentsForProject(projectId));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> onboardDepartment(@PathVariable int projectId, @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.onboardDepartment(projectId, request));
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable int projectId, @PathVariable int departmentId) {
        return ResponseEntity.ok(departmentService.getDepartmentById(departmentId));
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponse> updateDepartment(@PathVariable int projectId, @PathVariable int departmentId, @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(departmentId, request));
    }
}

