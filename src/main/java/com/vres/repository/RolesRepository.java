package com.vres.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vres.entity.Roles;

public interface RolesRepository extends JpaRepository<Roles, Integer> {

    
    Optional<Roles> findByName(String name);
}
