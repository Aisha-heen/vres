package com.vres.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vres.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
	List<Department> findByProjectId(int projectId);
	Optional<Department> findByMakerId(Integer makerId);
	Optional<Department> findByCheckerId(Integer checkerId);
	@Query("SELECT d FROM Department d WHERE d.project.id = :projectId AND (d.makerId = :userId OR d.checkerId = :userId)")
    Optional<Department> findByProjectIdAndUser(@Param("projectId") Integer projectId, @Param("userId") Integer userId);
	
	@Modifying
    void deleteByProjectId(int projectId);
}
