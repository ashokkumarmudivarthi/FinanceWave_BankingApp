package com.financewave.beneficiary.repository;

import com.financewave.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByUsernameIgnoreCase(String username);

    List<Beneficiary> findByStatus(String status);

    boolean existsByUsernameIgnoreCaseAndBeneficiaryAccountNumberAndStatus(
            String username,
            String account,
            String status
    );
}