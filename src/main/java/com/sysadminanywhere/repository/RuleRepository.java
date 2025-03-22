package com.sysadminanywhere.repository;

import com.sysadminanywhere.entity.RuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {

    Page<RuleEntity> findAll(Pageable pageable);

}
