package com.sysadminanywhere.views.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadminanywhere.common.directory.model.AD;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.directory.model.GroupEntry;
import com.sysadminanywhere.model.ReportItem;
import com.sysadminanywhere.service.*;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.lang.reflect.Field;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RolesAllowed("admins")
@PageTitle("Report")
@Route(value = "reports/report")
@PermitAll
@Uses(Icon.class)
public class ReportPreviewView extends Div implements BeforeEnterObserver {

    private String id;
    private String entry;

    private final ComputersService computersService;
    private final UsersService usersService;
    private final GroupsService groupsService;
    private final PrintersService printersService;

    private final ReportGeneratorService reportGeneratorService;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        if (!parametersMap.isEmpty()
                && parametersMap.containsKey("entry")
                && parametersMap.containsKey("id")) {

            entry = parametersMap.get("entry").get(0);
            id = parametersMap.get("id").get(0);

            updateView();
        }
    }

    public ReportPreviewView(ComputersService computersService,
                             UsersService usersService,
                             GroupsService groupsService,
                             PrintersService printersService,
                             ReportGeneratorService reportGeneratorService) {
        this.computersService = computersService;
        this.usersService = usersService;
        this.groupsService = groupsService;
        this.printersService = printersService;
        this.reportGeneratorService = reportGeneratorService;
    }

    private void updateView() {

        ReportItem reportItem = null;

        try {
            Resource resource = new ClassPathResource("reports/" + entry.toLowerCase() + ".json");
            InputStream inputStream = resource.getInputStream();
            String json = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));

            ReportItem[] reports = new ObjectMapper().readValue(json, ReportItem[].class);

            Optional<ReportItem> item = Arrays.stream(reports).filter(c -> c.getId().equalsIgnoreCase(id)).findFirst();
            if (item.isPresent())
                reportItem = item.get();
            else
                return;

        } catch (IOException e) {
            return;
        }

        byte[] result = null;

        switch (entry.toLowerCase()) {
            case "computers":
                result = computerReports(reportItem);
                break;
            case "users":
                result = userReports(reportItem);
                break;
            case "groups":
                result = groupReports(reportItem);
                break;
        }

        if (result != null) {
            byte[] finalResult = result;
            PdfViewer pdfViewer = new PdfViewer();
            StreamResource resource = new StreamResource(reportItem.getId() + ".pdf", () -> new ByteArrayInputStream(finalResult));
            pdfViewer.setSrc(resource);

            add(new VerticalLayout(pdfViewer));
        }
    }

    private byte[] computerReports(ReportItem reportItem) {
        String[] attributes = getAttributes(reportItem.getColumns(), ComputerEntry.class);

        return reportGeneratorService.generateReport(computersService.getAll(reportItem.getFilter(), attributes),
                reportItem.getName(), reportItem.getDescription(),
                attributes, attributes);
    }

    private byte[] userReports(ReportItem reportItem) {

        if (reportItem.getFilter().contains("{")) {
            Pattern pattern = Pattern.compile("\\{([^:}]+):([^}]+)\\}");
            Matcher matcher = pattern.matcher(reportItem.getFilter());

            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String key = matcher.group(1).trim();
                String value = matcher.group(2).trim();

                String replacement;

                switch (key) {
                    case "days":
                        replacement = getFileTime(Integer.parseInt(value)).toString();
                        matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                        break;
                    case "maxPwdAgeDays":
                        long maxPwdAgeDays = usersService.getLdapService().getMaxPwdAgeDays();
                        long thresholdDays = maxPwdAgeDays - Integer.parseInt(value);

                        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
                        ZonedDateTime cutoffDate = now.minusDays(thresholdDays);

                        ZonedDateTime windowsEpoch = ZonedDateTime.of(1601, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
                        long seconds = ChronoUnit.SECONDS.between(windowsEpoch, cutoffDate);
                        Long pwdLastSetCutoff = seconds * 10_000_000L;

                        replacement = pwdLastSetCutoff.toString();
                        matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                        break;
                }
            }
            matcher.appendTail(result);
            reportItem.setFilter(result.toString());
        }

        String[] attributes = getAttributes(reportItem.getColumns(), UserEntry.class);

        return reportGeneratorService.generateReport(usersService.getAll(reportItem.getFilter(), attributes),
                reportItem.getName(), reportItem.getDescription(),
                attributes, attributes);
    }

    private byte[] groupReports(ReportItem reportItem) {
        String[] attributes = getAttributes(reportItem.getColumns(), GroupEntry.class);

        return reportGeneratorService.generateReport(groupsService.getAll(reportItem.getFilter(), attributes),
                reportItem.getName(), reportItem.getDescription(),
                attributes, attributes);
    }

    private Long getFileTime(int days) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime targetDate = now.minusDays(180);

        // 2. Windows FileTime начинается с 1601-01-01
        ZonedDateTime windowsEpoch = ZonedDateTime.of(1601, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        // 3. Вычисляем разницу в 100-наносекундных интервалах
        long secondsBetween = ChronoUnit.SECONDS.between(windowsEpoch, targetDate);
        long nanosPart = ChronoUnit.NANOS.between(windowsEpoch.plusSeconds(secondsBetween), targetDate);

        // 4. Конвертируем в 100-нс интервалы
        long fileTime = secondsBetween * 10_000_000 + nanosPart / 100;

        return fileTime;
    }

    private String[] getAttributes(String[] columns, Class<?> clazz) {
        List<String> attributes = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            AD annotation = field.getAnnotation(AD.class);
            if (annotation == null) continue;

            if (Arrays.stream(columns).anyMatch(field.getName()::equalsIgnoreCase)) {
                attributes.add(annotation.name());
            }

        }

        return attributes.toArray(new String[attributes.size()]);
    }

}