package com.vres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vres.entity.Beneficiaries;

public interface BeneficiariesRepository extends JpaRepository<Beneficiaries, Integer> {

    // This method finds beneficiaries for a project
    List<Beneficiaries> findByProjectId(int projectId);

    // This counts all beneficiaries for a project
    long countByProjectId(int projectId);
 
    // This correctly counts approved beneficiaries
    @Query("SELECT count(b) FROM Beneficiaries b WHERE b.project.id = :projectId AND b.is_approved = :isApproved")
    long countByProjectIdAndIs_approved(@Param("projectId") int projectId, @Param("isApproved") boolean is_approved);

    // --- THIS IS THE NEW METHOD YOU NEED ---
    /**
     * Finds beneficiaries who are approved (is_approved = true)
     * AND do not have an entry in the Vouchers table.
     * Can be filtered by departmentId (if departmentId is not null).
     */
    @Query("SELECT b FROM Beneficiaries b " +
           "LEFT JOIN Vouchers v ON b.id = v.beneficiary.id " +
           "WHERE b.project.id = :projectId " +
           "AND b.is_approved = true " +
           "AND v.id IS NULL " +
           "AND (:departmentId IS NULL OR b.department.id = :departmentId)")
    List<Beneficiaries> findApprovedBeneficiariesWithoutVoucher(
            @Param("projectId") int projectId, 
            @Param("departmentId") Integer departmentId
    );
    
    // --- (You might also have this method, keep it if you do) ---
    @Query("SELECT b FROM Beneficiaries b " +
           "WHERE b.project.id = :projectId " +
           "AND (:isApproved IS NULL OR b.is_approved = :isApproved) " +
           "AND (:departmentId IS NULL OR b.department.id = :departmentId)")
    List<Beneficiaries> findByProjectAndFilters(
            @Param("projectId") int projectId, 
            @Param("isApproved") Boolean isApproved, 
            @Param("departmentId") Integer departmentId
    );
}