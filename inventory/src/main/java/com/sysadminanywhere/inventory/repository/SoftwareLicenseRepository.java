package com.sysadminanywhere.inventory.repository;

import com.sysadminanywhere.inventory.entity.SoftwareLicense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoftwareLicenseRepository extends JpaRepository<SoftwareLicense, Long> {
}
