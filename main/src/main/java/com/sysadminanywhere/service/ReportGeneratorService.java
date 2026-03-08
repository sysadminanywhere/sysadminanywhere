package com.sysadminanywhere.service;

import com.sysadminanywhere.domain.ObjectToListMapConverter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportGeneratorService {

    private final IReportEngine engine;

    public ReportGeneratorService(IReportEngine engine) {
        this.engine = engine;
    }

    public byte[] generateReport(List<?> objectList,
                                 String name,
                                 String description,
                                 String[] attributes,
                                 String[] names) {

        List<Map<String, Object>> dataList = ObjectToListMapConverter.convertToListMap(objectList, attributes);

        ClassPathResource resource = null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IRunAndRenderTask task = null;

        try {
            EngineConfig config = new EngineConfig();
            URL fontUrl = getClass().getClassLoader().getResource("/reports/fonts");
            config.setFontConfig(fontUrl);

            HashMap<String, Object> params = new HashMap<>();
            params.put("report_name", name);
            params.put("report_description", description);

            if(attributes.length == 2) {
                resource = new ClassPathResource("reports/columns2.rptdesign");
                params.put("column1_field", attributes[0]);
                params.put("column2_field", attributes[1]);
                params.put("column1_name", names[0]);
                params.put("column2_name", names[1]);
            }

            if(attributes.length == 3) {
                resource = new ClassPathResource("reports/columns3.rptdesign");
                params.put("column1_field", attributes[0]);
                params.put("column2_field", attributes[1]);
                params.put("column3_field", attributes[2]);
                params.put("column1_name", names[0]);
                params.put("column2_name", names[1]);
                params.put("column3_name", names[2]);
            }

            IReportRunnable design = engine.openReportDesign(resource.getInputStream());
            task = engine.createRunAndRenderTask(design);

            task.getAppContext().put("DATA_LIST", dataList);

            task.setParameterValues(params);

            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFormat("pdf");
            options.setOutputStream(outputStream);

            options.setEmbededFont(true);

            task.setRenderOption(options);

            task.run();

            return outputStream.toByteArray();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            if (task != null) task.close();
        }

        return null;
    }

}