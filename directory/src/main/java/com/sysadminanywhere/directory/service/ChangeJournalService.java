package com.sysadminanywhere.directory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.dto.ChangeJournalDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChangeJournalService {
    private static final Set<String> SENSITIVE_ATTRIBUTES = Set.of(
            "userpassword", "unicodepwd", "supplementalcredentials", "dbcsppwd", "ntpwdhistory");

    private final ObjectMapper objectMapper;
    private final Path journalPath;
    private final int maxEntries;

    public ChangeJournalService(ObjectMapper objectMapper,
                                @Value("${directory.audit.journal-path:${user.dir}/data/change-journal.json}") String journalPath,
                                @Value("${directory.audit.journal-max-entries:10000}") int maxEntries) {
        this.objectMapper = objectMapper;
        this.journalPath = Paths.get(journalPath);
        this.maxEntries = Math.max(1, maxEntries);
    }

    public synchronized void record(String action, Entry before, Entry after, String distinguishedName) {
        try {
            Entry source = after != null ? after : before;
            String dn = distinguishedName != null ? distinguishedName : source == null ? "" : source.getDn().getName();
            ChangeJournalDto item = new ChangeJournalDto(
                    UUID.randomUUID().toString(),
                    value(source, "name", "cn"),
                    dn,
                    value(source, "objectclass", "objectClass"),
                    action,
                    actor(),
                    LocalDateTime.now(),
                    attributes(before),
                    attributes(after));
            List<ChangeJournalDto> items = read();
            items.add(item);
            if (items.size() > maxEntries) {
                items = new ArrayList<>(items.subList(items.size() - maxEntries, items.size()));
            }
            if (journalPath.getParent() != null) {
                Files.createDirectories(journalPath.getParent());
            }
            String temporaryPrefix = journalPath.getFileName().toString();
            if (temporaryPrefix.length() < 3) {
                temporaryPrefix = temporaryPrefix + "---";
            }
            Path temporaryPath = Files.createTempFile(journalPath.getParent() == null
                            ? Paths.get(".") : journalPath.getParent(), temporaryPrefix, ".tmp");
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(temporaryPath.toFile(), items);
                try {
                    Files.move(temporaryPath, journalPath, StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                    Files.move(temporaryPath, journalPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporaryPath);
            }
        } catch (IOException | RuntimeException exception) {
            log.warn("Unable to persist change journal: {}", exception.getMessage());
        }
    }

    public synchronized List<ChangeJournalDto> find(Map<String, String> filters) {
        filters = filters == null ? Map.of() : filters;
        String objectName = filters.getOrDefault("objectName", "");
        String distinguishedName = filters.getOrDefault("distinguishedName", "");
        String actor = filters.getOrDefault("actor", "");
        String action = filters.getOrDefault("action", "");
        LocalDate start = parseDate(filters.get("startDate"));
        LocalDate end = parseDate(filters.get("endDate"));
        List<ChangeJournalDto> items;
        try {
            items = read();
        } catch (IOException | RuntimeException exception) {
            log.warn("Unable to read change journal: {}", exception.getMessage());
            return new ArrayList<>();
        }
        return items.stream().filter(Objects::nonNull).filter(item -> contains(item.getObjectName(), objectName)
                && contains(item.getDistinguishedName(), distinguishedName)
                && contains(item.getActor(), actor)
                && (action == null || action.isBlank() || action.equalsIgnoreCase(item.getAction()))
                && (start == null || item.getChangedAt() != null && !item.getChangedAt().toLocalDate().isBefore(start))
                && (end == null || item.getChangedAt() != null && !item.getChangedAt().toLocalDate().isAfter(end)))
                .sorted(Comparator.comparing(ChangeJournalDto::getChangedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private List<ChangeJournalDto> read() throws IOException {
        if (!Files.exists(journalPath)) {
            return new ArrayList<>();
        }
        List<ChangeJournalDto> result = objectMapper.readValue(journalPath.toFile(), new TypeReference<>() {});
        return result == null ? new ArrayList<>() : result;
    }

    private Map<String, String> attributes(Entry entry) {
        if (entry == null) return null;
        Map<String, String> result = new TreeMap<>();
        entry.forEach(attribute -> {
            String name = attribute.getUpId();
            result.put(name, SENSITIVE_ATTRIBUTES.contains(name.toLowerCase(Locale.ROOT))
                    ? "[redacted]" : attribute.toString());
        });
        return result;
    }

    private String value(Entry entry, String... names) {
        if (entry == null) return null;
        for (String name : names) {
            if (entry.get(name) != null) {
                try {
                    return entry.get(name).getString();
                } catch (Exception ignored) {
                    return entry.get(name).toString();
                }
            }
        }
        return null;
    }

    private String actor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || authentication.getName() == null ? "system" : authentication.getName();
    }

    private boolean contains(String value, String filter) {
        return filter == null || filter.isBlank() || (value != null && value.toLowerCase().contains(filter.toLowerCase()));
    }

    private LocalDate parseDate(String value) {
        try { return value == null || value.isBlank() ? null : LocalDate.parse(value); }
        catch (RuntimeException ignored) { return null; }
    }
}
