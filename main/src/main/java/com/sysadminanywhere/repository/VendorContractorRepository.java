package com.sysadminanywhere.repository;

import com.sysadminanywhere.entity.VendorContractorEntity;
import com.sysadminanywhere.common.vendor.model.VendorContractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VendorContractorRepository extends JpaRepository<VendorContractorEntity, Long>, JpaSpecificationExecutor<VendorContractorEntity> {

    @Query("SELECT v FROM VendorContractorEntity v WHERE v.status = :status")
    List<VendorContractorEntity> findByStatus(@Param("status") VendorContractor.ContractorStatus status);

    @Query("SELECT v FROM VendorContractorEntity v WHERE v.company = :company")
    List<VendorContractorEntity> findByCompany(@Param("company") String company);

    @Query("SELECT v FROM VendorContractorEntity v WHERE v.endDate BETWEEN :startDate AND :endDate")
    List<VendorContractorEntity> findByEndDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT v FROM VendorContractorEntity v WHERE v.endDate <= :date AND v.status = 'ACTIVE'")
    List<VendorContractorEntity> findExpiringContractors(@Param("date") LocalDate date);

}
