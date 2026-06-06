package com.financewave.beneficiary.repository;

import com.financewave.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    // CASE-INSENSITIVE FETCH
    @Query("SELECT b FROM Beneficiary b WHERE LOWER(b.username) = LOWER(:username)")
    List<Beneficiary> findByUsernameIgnoreCase(@Param("username") String username);

    List<Beneficiary> findByStatus(String status);
}