package com.sysadminanywhere.service;

import com.sysadminanywhere.common.directory.dto.DomainHealthDto;
import com.sysadminanywhere.model.SecurityAuditSnapshot;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Service
public class DashboardSnapshotService {
    private static final Duration MAX_AGE = Duration.ofMinutes(1);

    private final LdapService ldapService;
    private final SecurityAuditService securityAuditService;
    private Snapshot<DomainHealthDto> domainHealth;
    private Snapshot<SecurityAuditSnapshot> securityAudit;

    public DashboardSnapshotService(LdapService ldapService, SecurityAuditService securityAuditService) {
        this.ldapService = ldapService;
        this.securityAuditService = securityAuditService;
    }

    public synchronized DomainHealthDto domainHealth() {
        domainHealth = currentOrLoad(domainHealth, ldapService::getDomainHealth);
        return domainHealth.value();
    }

    public synchronized SecurityAuditSnapshot securityAudit() {
        securityAudit = currentOrLoad(securityAudit, securityAuditService::scan);
        return securityAudit.value();
    }

    public synchronized void invalidate() {
        domainHealth = null;
        securityAudit = null;
    }

    private <T> Snapshot<T> currentOrLoad(Snapshot<T> snapshot, Supplier<T> loader) {
        Instant now = Instant.now();
        if (snapshot != null && Duration.between(snapshot.loadedAt(), now).compareTo(MAX_AGE) < 0) {
            return snapshot;
        }
        return new Snapshot<>(loader.get(), now);
    }

    private record Snapshot<T>(T value, Instant loadedAt) {
    }
}
