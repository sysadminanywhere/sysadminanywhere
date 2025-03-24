package com.sysadminanywhere.service;

import com.sysadminanywhere.entity.LogEntity;
import com.sysadminanywhere.repository.LogsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
public class LogsService {

    private final LogsRepository logsRepository;

    public LogsService(LogsRepository logsRepository) {
        this.logsRepository = logsRepository;
    }

    public Page<LogEntity> getLogs(Long id, Pageable pageable, Map<String, Object> filters) {
        LocalDate startDate = filters.get("startDate") != null ? (LocalDate) filters.get("startDate") : LocalDate.now();
        LocalDate endDate = filters.get("endDate") != null ? (LocalDate) filters.get("endDate") : LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return logsRepository.findByRuleIdAndCreatedAtBetween(id, startDateTime, endDateTime, pageable);
    }

    public LogEntity addToLog(Long ruleId, String message) {
        LogEntity log = new LogEntity();
        log.setCreatedAt(LocalDateTime.now());
        log.setRuleId(ruleId);
        log.setMessage(message);

        LogEntity result = logsRepository.save(log);
        return result;
    }

}