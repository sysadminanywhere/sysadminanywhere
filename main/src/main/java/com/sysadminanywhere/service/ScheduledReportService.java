package com.sysadminanywhere.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.domain.ObjectToListMapConverter;
import com.sysadminanywhere.model.ReportItem;
import com.sysadminanywhere.model.ScheduledReportConfig;
import com.sysadminanywhere.model.ScheduledReportRun;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ScheduledReportService {
    private final ObjectMapper objectMapper;
    private final UsersService usersService;
    private final ComputersService computersService;
    private final GroupsService groupsService;
    private final PrintersService printersService;
    private final ContactsService contactsService;
    private final ReportGeneratorService reportGeneratorService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final MessageSource messageSource;
    private final Path configPath;
    private final Path runsPath;
    private final Path outputDirectory;
    private final Map<String, String> lastRunKeys = new ConcurrentHashMap<>();

    public ScheduledReportService(ObjectMapper objectMapper, UsersService usersService,
                                  ComputersService computersService, GroupsService groupsService,
                                  PrintersService printersService, ContactsService contactsService,
                                  ReportGeneratorService reportGeneratorService,
                                  ObjectProvider<JavaMailSender> mailSenderProvider,
                                  MessageSource messageSource,
                                  @Value("${reports.scheduler.config-path:${user.dir}/data/scheduled-reports.json}") String configPath,
                                  @Value("${reports.scheduler.runs-path:${user.dir}/data/scheduled-report-runs.json}") String runsPath,
                                  @Value("${reports.scheduler.output-dir:${user.dir}/data/scheduled-reports}") String outputDirectory) {
        this.objectMapper = objectMapper;
        this.usersService = usersService;
        this.computersService = computersService;
        this.groupsService = groupsService;
        this.printersService = printersService;
        this.contactsService = contactsService;
        this.reportGeneratorService = reportGeneratorService;
        this.mailSenderProvider = mailSenderProvider;
        this.messageSource = messageSource;
        this.configPath = Paths.get(configPath);
        this.runsPath = Paths.get(runsPath);
        this.outputDirectory = Paths.get(outputDirectory);
    }

    public synchronized List<ScheduledReportConfig> list() {
        return read(configPath, new TypeReference<>() {});
    }

    public synchronized List<ScheduledReportRun> runs() {
        List<ScheduledReportRun> stored = read(runsPath, new TypeReference<List<ScheduledReportRun>>() {});
        return stored.stream()
                .sorted(Comparator.comparing(ScheduledReportRun::getStartedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(100)
                .toList();
    }

    public synchronized ScheduledReportConfig save(ScheduledReportConfig config) {
        validate(config);
        if (config.getId() == null || config.getId().isBlank()) {
            config.setId(UUID.randomUUID().toString());
        }
        List<ScheduledReportConfig> configs = list();
        configs.removeIf(item -> config.getId().equals(item.getId()));
        configs.add(config);
        write(configPath, configs);
        return config;
    }

    public synchronized void delete(String id) {
        List<ScheduledReportConfig> configs = list();
        configs.removeIf(item -> Objects.equals(id, item.getId()));
        write(configPath, configs);
    }

    public ScheduledReportRun runNow(String id) {
        ScheduledReportConfig config = list().stream()
                .filter(item -> Objects.equals(id, item.getId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown scheduled report"));
        return execute(config);
    }

    // Run at the start of every minute so a fixed-delay drift cannot skip a configured time.
    @Scheduled(cron = "0 * * * * *")
    public void runDueReports() {
        LocalDateTime now = LocalDateTime.now();
        for (ScheduledReportConfig config : list()) {
            if (!config.isEnabled() || !isDue(config, now)) continue;
            String key = config.getId() + ":" + now.toLocalDate();
            if ("WEEKLY".equalsIgnoreCase(config.getFrequency())) key += ":" + now.getDayOfWeek();
            if (key.equals(lastRunKeys.get(config.getId()))) continue;
            lastRunKeys.put(config.getId(), key);
            execute(config);
        }
    }

    private boolean isDue(ScheduledReportConfig config, LocalDateTime now) {
        if (now.getHour() != config.getHour() || now.getMinute() != config.getMinute()) return false;
        return !"WEEKLY".equalsIgnoreCase(config.getFrequency()) || now.getDayOfWeek() == DayOfWeek.MONDAY;
    }

    private ScheduledReportRun execute(ScheduledReportConfig config) {
        ScheduledReportRun run = new ScheduledReportRun(config.getId(), LocalDateTime.now(), null, "FAILED", null, null);
        try {
            ReportItem report = loadReport(config.getEntry(), config.getReportId());
            String filter = resolveFilter(report.getFilter());
            String[] attributes = reportAttributes(config.getEntry(), report);
            List<?> objects = loadObjects(config.getEntry(), filter, attributes);
            String extension = "CSV".equalsIgnoreCase(config.getFormat()) ? ".csv" : ".pdf";
            Files.createDirectories(outputDirectory);
            Path file = outputDirectory.resolve(config.getId() + "-" + System.currentTimeMillis() + extension);
            String reportName = messageSource.getMessage(report.getName(), null, Locale.ENGLISH);
            String reportDescription = messageSource.getMessage(report.getDescription(), null, Locale.ENGLISH);
            if (extension.equals(".pdf")) {
                byte[] pdf = reportGeneratorService.generateReport(objects, reportName, reportDescription,
                        attributes, translatedNames(report.getNames()));
                if (pdf == null) throw new IllegalStateException("PDF generation returned no data");
                Files.write(file, pdf);
            } else {
                Files.writeString(file, csv(objects, attributes, translatedNames(report.getNames())), StandardCharsets.UTF_8);
            }
            run.setFilePath(file.toAbsolutePath().toString());
            String emailWarning = sendEmail(config, file);
            run.setStatus(emailWarning == null ? "COMPLETED" : "WARNING");
            run.setError(emailWarning);
        } catch (Exception exception) {
            run.setError(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
        }
        run.setFinishedAt(LocalDateTime.now());
        appendRun(run);
        return run;
    }

    private ReportItem loadReport(String entry, String reportId) throws IOException {
        ResourceLines resource = new ResourceLines("reports/" + entry.toLowerCase(Locale.ROOT) + ".json");
        ReportItem[] reports = objectMapper.readValue(resource.text(), ReportItem[].class);
        return Arrays.stream(reports).filter(item -> reportId.equalsIgnoreCase(item.getId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown report: " + entry + "/" + reportId));
    }

    private String[] reportAttributes(String entry, ReportItem report) {
        Class<?> type = switch (entry.toLowerCase(Locale.ROOT)) {
            case "users" -> com.sysadminanywhere.common.directory.model.UserEntry.class;
            case "computers" -> com.sysadminanywhere.common.directory.model.ComputerEntry.class;
            case "groups" -> com.sysadminanywhere.common.directory.model.GroupEntry.class;
            case "printers" -> com.sysadminanywhere.common.directory.model.PrinterEntry.class;
            case "contacts" -> com.sysadminanywhere.common.directory.model.ContactEntry.class;
            default -> throw new IllegalArgumentException("Unsupported report category: " + entry);
        };
        List<String> attributes = new ArrayList<>();
        for (String column : report.getColumns()) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (!field.getName().equalsIgnoreCase(column)) continue;
                com.sysadminanywhere.common.directory.model.AD ad = field.getAnnotation(com.sysadminanywhere.common.directory.model.AD.class);
                if (ad != null) attributes.add(ad.name());
                break;
            }
        }
        return attributes.toArray(String[]::new);
    }

    private List<?> loadObjects(String entry, String filter, String[] attributes) {
        return switch (entry.toLowerCase(Locale.ROOT)) {
            case "users" -> Optional.ofNullable(usersService.getAll(filter, attributes)).orElse(List.of());
            case "computers" -> Optional.ofNullable(computersService.getAll(filter, attributes)).orElse(List.of());
            case "groups" -> Optional.ofNullable(groupsService.getAll(filter, attributes)).orElse(List.of());
            case "printers" -> Optional.ofNullable(printersService.getAll(filter, attributes)).orElse(List.of());
            case "contacts" -> Optional.ofNullable(contactsService.getAll(filter, attributes)).orElse(List.of());
            default -> throw new IllegalArgumentException("Unsupported report category: " + entry);
        };
    }

    private String resolveFilter(String filter) {
        if (filter == null) return "";
        Matcher matcher = Pattern.compile("\\{([^:}]+):([^}]+)\\}").matcher(filter);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            long value = Long.parseLong(matcher.group(2).trim());
            String replacement = switch (key) {
                case "days" -> fileTime(value).toString();
                case "maxPwdAgeDays" -> fileTime(Math.max(0, usersService.getLdapService().getMaxPwdAgeDays() - value)).toString();
                default -> matcher.group(0);
            };
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private Long fileTime(long days) {
        long seconds = java.time.Instant.now().minusSeconds(days * 86400L).getEpochSecond() + 11644473600L;
        return seconds * 10_000_000L;
    }

    private String[] translatedNames(String[] names) {
        return Arrays.stream(names)
                .map(name -> messageSource.getMessage(name, null, Locale.ENGLISH))
                .toArray(String[]::new);
    }

    private String csv(List<?> objects, String[] attributes, String[] names) {
        List<Map<String, Object>> rows = ObjectToListMapConverter.convertToListMap(objects, attributes);
        StringBuilder csv = new StringBuilder();
        csv.append(Arrays.stream(names).map(this::escapeCsv).collect(Collectors.joining(","))).append('\n');
        for (Map<String, Object> row : rows) {
            csv.append(Arrays.stream(attributes).map(attribute -> escapeCsv(row.get(attribute))).collect(Collectors.joining(","))).append('\n');
        }
        return csv.toString();
    }

    private String escapeCsv(Object value) {
        String text = value == null ? "" : value.toString().replace("\"", "\"\"");
        return '"' + text + '"';
    }

    private String sendEmail(ScheduledReportConfig config, Path file) {
        if (config.getRecipients() == null || config.getRecipients().isBlank()) return null;
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) return "Report generated, but mail is not configured";
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(Arrays.stream(config.getRecipients().split(","))
                    .map(String::trim).filter(item -> !item.isBlank()).toArray(String[]::new));
            helper.setSubject("Scheduled report: " + file.getFileName());
            helper.setText("Scheduled report is attached.");
            helper.addAttachment(file.getFileName().toString(), file.toFile());
            sender.send(message);
            return null;
        } catch (Exception exception) {
            return "Report generated, but email delivery failed: " + exception.getMessage();
        }
    }

    private synchronized void appendRun(ScheduledReportRun run) {
        List<ScheduledReportRun> runs = read(runsPath, new TypeReference<List<ScheduledReportRun>>() {});
        runs.add(run);
        if (runs.size() > 100) runs = new ArrayList<>(runs.subList(runs.size() - 100, runs.size()));
        write(runsPath, runs);
    }

    private void validate(ScheduledReportConfig config) {
        if (config == null || config.getEntry() == null || config.getReportId() == null) throw new IllegalArgumentException("Report is required");
        if (config.getFormat() == null || config.getFrequency() == null) throw new IllegalArgumentException("Format and frequency are required");
        if (!Set.of("users", "computers", "groups", "printers", "contacts").contains(config.getEntry().toLowerCase(Locale.ROOT))) throw new IllegalArgumentException("Unsupported report category");
        if (!Set.of("PDF", "CSV").contains(config.getFormat().toUpperCase(Locale.ROOT))) throw new IllegalArgumentException("Format must be PDF or CSV");
        if (!Set.of("DAILY", "WEEKLY").contains(config.getFrequency().toUpperCase(Locale.ROOT))) throw new IllegalArgumentException("Frequency must be DAILY or WEEKLY");
        if (config.getHour() < 0 || config.getHour() > 23 || config.getMinute() < 0 || config.getMinute() > 59) throw new IllegalArgumentException("Invalid schedule time");
    }

    private <T> List<T> read(Path path, TypeReference<List<T>> type) {
        if (!Files.exists(path)) return new ArrayList<>();
        try { return Optional.ofNullable(objectMapper.readValue(path.toFile(), type)).orElse(new ArrayList<>()); }
        catch (IOException exception) { return new ArrayList<>(); }
    }

    private void write(Path path, Object value) {
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), value);
        } catch (IOException exception) { throw new IllegalStateException("Unable to save scheduled report data", exception); }
    }

    private static class ResourceLines {
        private final String path;
        ResourceLines(String path) { this.path = path; }
        String text() throws IOException {
            try (InputStream stream = new ClassPathResource(path).getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
