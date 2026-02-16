package com.sysadminanywhere.incident;

import com.sysadminanywhere.incident.service.IncidentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class IncidentApplication {

    private final IncidentService incidentService;

    public IncidentApplication(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    public static void main(String[] args) {
        SpringApplication.run(IncidentApplication.class, args);
    }

    @Scheduled(fixedDelay = 60000) // каждые 60 секунд
    public void runPipeline() {
        incidentService.processEvents();
    }

}