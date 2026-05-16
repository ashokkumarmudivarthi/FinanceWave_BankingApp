package com.financewave.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financewave.auth.entity.LoginAudit;
import java.util.List;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
	List<LoginAudit> findByUsername(String username);
}