package com.sysadminanywhere.service;

import com.sysadminanywhere.common.directory.dto.DomainHealthDto;
import com.sysadminanywhere.model.SecurityAuditSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardSnapshotServiceTest {
    @Test
    void reusesSnapshotsUntilInvalidated() {
        LdapService ldap = mock(LdapService.class);
        SecurityAuditService audit = mock(SecurityAuditService.class);
        DomainHealthDto health = new DomainHealthDto("HEALTHY", LocalDateTime.now(), List.of());
        SecurityAuditSnapshot security = new SecurityAuditSnapshot(LocalDateTime.now(),
                0, 0, 0, 0, 0, List.of(), null);
        when(ldap.getDomainHealth()).thenReturn(health);
        when(audit.scan()).thenReturn(security);
        DashboardSnapshotService service = new DashboardSnapshotService(ldap, audit);

        assertSame(health, service.domainHealth());
        assertSame(health, service.domainHealth());
        assertSame(security, service.securityAudit());
        assertSame(security, service.securityAudit());
        verify(ldap).getDomainHealth();
        verify(audit).scan();

        service.invalidate();
        service.domainHealth();
        service.securityAudit();
        verify(ldap, times(2)).getDomainHealth();
        verify(audit, times(2)).scan();
    }
}
