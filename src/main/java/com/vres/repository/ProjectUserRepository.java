package com.vres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vres.entity.ProjectUser;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUser, Integer> {
    List<ProjectUser> findAllByUserId(int userId);
    List<ProjectUser> findByProjectId(int projectId);
    List<ProjectUser> findByProjectIdAndUserIdIn(int projectId, List<Integer> userIds);
    boolean existsByProjectIdAndUserIdAndVendorStatus(int projectId, int userId, Integer vendorStatus);
}

