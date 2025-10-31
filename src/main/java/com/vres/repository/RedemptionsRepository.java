package com.vres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vres.entity.Redemptions;

@Repository
public interface RedemptionsRepository extends JpaRepository<Redemptions, Integer> {
	long countByVoucher_Project_Id(int projectId);

	List<Redemptions> findByVoucher_Project_Id(int projectId);
    
}
