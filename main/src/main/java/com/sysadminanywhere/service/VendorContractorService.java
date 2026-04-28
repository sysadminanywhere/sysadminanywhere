package com.sysadminanywhere.service;

import com.sysadminanywhere.common.vendor.model.VendorContractor;
import com.sysadminanywhere.entity.VendorContractorEntity;
import com.sysadminanywhere.repository.VendorContractorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VendorContractorService {

    private final VendorContractorRepository repository;
    private final ADAccessService adAccessService;

    public VendorContractorService(VendorContractorRepository repository, ADAccessService adAccessService) {
        this.repository = repository;
        this.adAccessService = adAccessService;
    }

    public Page<VendorContractor> getAllContractors(Pageable pageable) {
        return repository.findAll(pageable).map(VendorContractorEntity::toModel);
    }

    public VendorContractor getContractorById(Long id) {
        return repository.findById(id)
                .map(VendorContractorEntity::toModel)
                .orElse(null);
    }

    public VendorContractor createContractor(VendorContractor contractor) {
        VendorContractorEntity entity = VendorContractorEntity.fromModel(contractor);
        entity.setStatus(VendorContractor.ContractorStatus.PENDING);
        VendorContractorEntity saved = repository.save(entity);
        
        // Если статус ACTIVE и указаны AD данные, создаем пользователя в AD
        if (contractor.getStatus() == VendorContractor.ContractorStatus.ACTIVE 
                && contractor.getAdUsername() != null 
                && !contractor.getAdUsername().isEmpty()) {
            try {
                adAccessService.createADUser(saved.toModel());
            } catch (Exception e) {
                // Логируем ошибку, но не прерываем создание записи
                System.err.println("Failed to create AD user: " + e.getMessage());
            }
        }
        
        return saved.toModel();
    }

    public VendorContractor updateContractor(Long id, VendorContractor contractor) {
        VendorContractorEntity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contractor not found"));
        
        VendorContractor.ContractorStatus oldStatus = existing.getStatus();
        
        existing.setName(contractor.getName());
        existing.setCompany(contractor.getCompany());
        existing.setEmail(contractor.getEmail());
        existing.setPhone(contractor.getPhone());
        existing.setAdUsername(contractor.getAdUsername());
        existing.setAdDomain(contractor.getAdDomain());
        existing.setStartDate(contractor.getStartDate());
        existing.setEndDate(contractor.getEndDate());
        existing.setAccessLevel(contractor.getAccessLevel());
        existing.setStatus(contractor.getStatus());
        existing.setNotes(contractor.getNotes());
        
        VendorContractorEntity updated = repository.save(existing);
        VendorContractor updatedModel = updated.toModel();
        
        // Обработка изменений статуса для управления доступом в AD
        handleStatusChange(oldStatus, contractor.getStatus(), updatedModel);
        
        return updatedModel;
    }
    
    private void handleStatusChange(VendorContractor.ContractorStatus oldStatus, 
                                    VendorContractor.ContractorStatus newStatus,
                                    VendorContractor contractor) {
        // Если статус изменился на ACTIVE и есть AD данные - создаем пользователя
        if (oldStatus != VendorContractor.ContractorStatus.ACTIVE 
                && newStatus == VendorContractor.ContractorStatus.ACTIVE
                && contractor.getAdUsername() != null 
                && !contractor.getAdUsername().isEmpty()) {
            try {
                adAccessService.createADUser(contractor);
            } catch (Exception e) {
                System.err.println("Failed to create AD user: " + e.getMessage());
            }
        }
        
        // Если статус изменился на EXPIRED или REVOKED - отключаем пользователя
        if ((oldStatus == VendorContractor.ContractorStatus.ACTIVE 
                || oldStatus == VendorContractor.ContractorStatus.PENDING)
                && (newStatus == VendorContractor.ContractorStatus.EXPIRED 
                || newStatus == VendorContractor.ContractorStatus.REVOKED)
                && contractor.getAdUsername() != null 
                && !contractor.getAdUsername().isEmpty()) {
            try {
                adAccessService.disableADUser(contractor);
            } catch (Exception e) {
                System.err.println("Failed to disable AD user: " + e.getMessage());
            }
        }
    }

    public void deleteContractor(Long id) {
        repository.deleteById(id);
    }

    public List<VendorContractor> getContractorsByStatus(VendorContractor.ContractorStatus status) {
        return repository.findByStatus(status).stream()
                .map(VendorContractorEntity::toModel)
                .collect(Collectors.toList());
    }

    public List<VendorContractor> getContractorsByCompany(String company) {
        return repository.findByCompany(company).stream()
                .map(VendorContractorEntity::toModel)
                .collect(Collectors.toList());
    }

    public List<VendorContractor> getContractorsExpiringSoon(LocalDate date) {
        return repository.findExpiringContractors(date).stream()
                .map(VendorContractorEntity::toModel)
                .collect(Collectors.toList());
    }

    public void updateExpiredContractors() {
        LocalDate today = LocalDate.now();
        List<VendorContractorEntity> expired = repository.findExpiringContractors(today);
        expired.forEach(entity -> {
            if (entity.getEndDate().isBefore(today) && entity.getStatus() == VendorContractor.ContractorStatus.ACTIVE) {
                VendorContractor.ContractorStatus oldStatus = entity.getStatus();
                entity.setStatus(VendorContractor.ContractorStatus.EXPIRED);
                VendorContractorEntity updated = repository.save(entity);
                
                // Отключаем пользователя в AD при истечении контракта
                if (entity.getAdUsername() != null && !entity.getAdUsername().isEmpty()) {
                    try {
                        adAccessService.disableADUser(updated.toModel());
                    } catch (Exception e) {
                        System.err.println("Failed to disable AD user for expired contract: " + e.getMessage());
                    }
                }
            }
        });
    }

}
