package com.financewave.account.repository;

import com.financewave.account.entity.VpaMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VpaRepository extends JpaRepository<VpaMapping, Long> {

    Optional<VpaMapping> findByVpa(String vpa);

    boolean existsByVpa(String vpa);
}