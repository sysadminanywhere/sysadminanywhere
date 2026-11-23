package com.sysadminanywhere.inventory.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatchRemediationServiceTest {
    @Test
    void selectsLatestValidPatchChronologicallyAcrossWmiFormats() {
        LocalDate today = LocalDate.of(2026, 11, 23);

        assertThat(PatchRemediationService.latestPatchDate(
                List.of("9/30/2026", "2026-10-01", "11/2/2026", "12/31/2026", "11/31/2025"), today))
                .isEqualTo(LocalDate.of(2026, 11, 2));
    }

    @Test
    void missingOrInvalidDatesCannotVerifyRemediation() {
        assertThat(PatchRemediationService.latestPatchDate(List.of("", "11/31/2026"), LocalDate.of(2026, 11, 23)))
                .isNull();
    }
}
