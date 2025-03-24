package com.sysadminanywhere.repository;

import com.sysadminanywhere.entity.LogEntity;
import com.sysadminanywhere.entity.RuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface LogsRepository  extends JpaRepository<LogEntity, Long> {

    Page<LogEntity> findByRuleIdAndCreatedAtBetween(Long id, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}