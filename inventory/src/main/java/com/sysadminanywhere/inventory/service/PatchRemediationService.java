package com.sysadminanywhere.inventory.service;

import com.sysadminanywhere.common.inventory.model.ComputerPatchStatus;
import com.sysadminanywhere.common.inventory.model.PatchRemediation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
public class PatchRemediationService {
    private static final List<DateTimeFormatter> PATCH_DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("M/d/uuuu").withResolverStyle(ResolverStyle.STRICT));

    private final JdbcTemplate jdbc;
    private final int staleDays;

    public PatchRemediationService(JdbcTemplate jdbc, @Value("${inventory.patch.stale-days:90}") int staleDays) {
        this.jdbc = jdbc;
        this.staleDays = Math.max(1, staleDays);
    }

    public List<ComputerPatchStatus> getPatchStatuses() {
        Map<String, List<String>> datesByComputer = new TreeMap<>();
        jdbc.query("""
                SELECT c.name, p.property_value
                FROM computers c
                JOIN computer_hardwares ch ON ch.computer_id = c.id
                JOIN hardware_models hm ON hm.id = ch.hardware_model_id
                JOIN hardware_properties p ON p.computer_hardware_id = ch.id
                WHERE hm.hardware_type = 'Patch' AND p.property_name = 'InstalledOn'
                """, row -> {
            datesByComputer.computeIfAbsent(row.getString("name"), name -> new ArrayList<>())
                    .add(row.getString("property_value"));
        });
        LocalDate today = LocalDate.now();
        return datesByComputer.entrySet().stream().map(entry -> {
            LocalDate latest = latestPatchDate(entry.getValue(), today);
            return new ComputerPatchStatus(entry.getKey(), latest == null ? null : latest.toString(),
                    latest == null || latest.isBefore(today.minusDays(staleDays)));
        }).toList();
    }

    public List<PatchRemediation> findAll() {
        return jdbc.query("""
                SELECT c.name, r.owner, r.due_date, r.verified_at
                FROM patch_remediation r JOIN computers c ON c.id = r.computer_id
                ORDER BY r.verified_at NULLS FIRST, r.due_date, c.name
                """, (row, index) -> {
            OffsetDateTime verified = row.getObject("verified_at", OffsetDateTime.class);
            return new PatchRemediation(row.getString("name"), row.getString("owner"),
                    row.getObject("due_date", LocalDate.class), verified == null ? null : verified.toInstant());
        });
    }

    public boolean assign(PatchRemediation request) {
        String owner = request.owner() == null ? "" : request.owner().strip();
        if (request.computer() == null || request.computer().isBlank() || owner.isEmpty()
                || owner.length() > 255 || request.dueDate() == null) {
            throw new IllegalArgumentException("Computer, owner and due date are required");
        }
        return jdbc.update("""
                INSERT INTO patch_remediation (computer_id, owner, due_date, assigned_at)
                SELECT id, ?, ?, clock_timestamp() FROM computers WHERE name = ?
                ON CONFLICT (computer_id) DO UPDATE
                SET owner = EXCLUDED.owner, due_date = EXCLUDED.due_date,
                    assigned_at = EXCLUDED.assigned_at, verified_at = NULL
                """, owner, request.dueDate(), request.computer()) > 0;
    }

    void verifyAfterScan(Long computerId, Instant scanStarted, LocalDate latestPatch) {
        if (latestPatch == null) {
            return;
        }
        jdbc.update("""
                UPDATE patch_remediation SET verified_at = CASE
                    WHEN ? THEN COALESCE(verified_at, clock_timestamp()) ELSE NULL END
                WHERE computer_id = ? AND assigned_at < ?
                """, !latestPatch.isBefore(LocalDate.now().minusDays(staleDays)), computerId,
                scanStarted.atOffset(ZoneOffset.UTC));
    }

    static LocalDate latestPatchDate(List<String> rawDates, LocalDate today) {
        return rawDates.stream().map(PatchRemediationService::parsePatchDate)
                .filter(Objects::nonNull).filter(date -> !date.isAfter(today))
                .max(Comparator.naturalOrder()).orElse(null);
    }

    private static LocalDate parsePatchDate(String value) {
        if (value == null) {
            return null;
        }
        for (DateTimeFormatter formatter : PATCH_DATE_FORMATS) {
            try {
                return LocalDate.parse(value.strip(), formatter);
            } catch (DateTimeParseException ignored) {
                // WMI exposes InstalledOn in multiple formats.
            }
        }
        return null;
    }
}
