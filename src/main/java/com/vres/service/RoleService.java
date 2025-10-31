package com.vres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vres.dto.RoleDto;
import com.vres.entity.Roles;
import com.vres.repository.RolesRepository;

@Service
public class RoleService {

    @Autowired
    private RolesRepository rolesRepository;
    public List<RoleDto> getAllRoles() {
        return rolesRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RoleDto convertToDto(Roles role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }
}
