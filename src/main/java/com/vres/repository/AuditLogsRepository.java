package com.vres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vres.entity.AuditLogs;

@Repository
public interface AuditLogsRepository extends JpaRepository<AuditLogs, Integer> {

}
