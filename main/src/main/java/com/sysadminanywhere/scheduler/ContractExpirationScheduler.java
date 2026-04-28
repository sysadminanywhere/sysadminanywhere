package com.sysadminanywhere.scheduler;

import com.sysadminanywhere.common.vendor.model.VendorContractor;
import com.sysadminanywhere.service.VendorContractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ContractExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ContractExpirationScheduler.class);

    private final VendorContractorService contractorService;

    public ContractExpirationScheduler(VendorContractorService contractorService) {
        this.contractorService = contractorService;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Запуск каждый день в 9:00 утра
    public void checkExpiringContracts() {
        logger.info("Checking for expiring contracts...");
        
        LocalDate today = LocalDate.now();
        
        // Проверка контрактов, истекающих через 7 дней
        checkContractsExpiringIn(today.plusDays(7), 7);
        
        // Проверка контрактов, истекающих через 3 дня
        checkContractsExpiringIn(today.plusDays(3), 3);
        
        // Проверка контрактов, истекающих через 1 день
        checkContractsExpiringIn(today.plusDays(1), 1);
        
        // Обновление статуса истекших контрактов
        contractorService.updateExpiredContractors();
    }

    private void checkContractsExpiringIn(LocalDate targetDate, int daysBefore) {
        List<VendorContractor> expiringContractors = contractorService.getContractorsExpiringSoon(targetDate);
        
        if (!expiringContractors.isEmpty()) {
            logger.info("Found {} contracts expiring in {} days", expiringContractors.size(), daysBefore);
            
            for (VendorContractor contractor : expiringContractors) {
                if (contractor.getStatus() == VendorContractor.ContractorStatus.ACTIVE) {
                    logger.warn("Contract for {} ({}) expires in {} days on {}", 
                            contractor.getName(), 
                            contractor.getCompany(), 
                            daysBefore, 
                            contractor.getEndDate());
                    
                    // Здесь можно добавить отправку email уведомлений
                    // emailService.sendExpirationNotification(contractor, daysBefore);
                }
            }
        }
    }

}
