package com.sysadminanywhere.config;

import com.sysadminanywhere.domain.InventorySetting;
import com.sysadminanywhere.domain.MonitoringSetting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SysadminAnywhereConfiguration {

    @Value("${sysadminanywhere.inventory.address:}")
    private String inventoryAddress;

    @Value("${sysadminanywhere.monitoring.address:}")
    private String monitoringAddress;

    @Bean
    public InventorySetting getInventorySetting() {
        InventorySetting setting = new InventorySetting();

        setting.setAddress(inventoryAddress);
        setting.setAvailable(!inventoryAddress.isEmpty());

        return setting;
    }

    @Bean
    public MonitoringSetting getMonitoringSetting() {
        MonitoringSetting setting = new MonitoringSetting();

        setting.setAddress(monitoringAddress);
        setting.setAvailable(!monitoringAddress.isEmpty());

        return setting;
    }

}
