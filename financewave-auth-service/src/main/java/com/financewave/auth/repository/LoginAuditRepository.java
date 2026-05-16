package com.financewave.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.financewave.auth.entity.LoginAudit;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
}