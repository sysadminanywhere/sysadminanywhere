package com.sysadminanywhere.service;

import com.lowagie.text.FontFactory;
import com.sysadminanywhere.domain.ObjectToListMapConverter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportGeneratorService {

    public byte[] generateReport(List<?> objectList,
                                 String name,
                                 String description,
                                 String[] attributes,
                                 String[] names) {

        List<Map<String, Object>> dataList = ObjectToListMapConverter.convertToListMap(objectList, attributes);

        IReportEngine engine = null;
        ClassPathResource resource = null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            EngineConfig config = new EngineConfig();
            URL fontUrl = getClass().getClassLoader().getResource("/reports/fonts");
            config.setFontConfig(fontUrl);

            Platform.startup(config);
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            engine = factory.createReportEngine(config);

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
            IRunAndRenderTask task = engine.createRunAndRenderTask(design);

            task.getAppContext().put("DATA_LIST", dataList);

            task.setParameterValues(params);

            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFormat("pdf");
            options.setOutputStream(outputStream);
            task.setRenderOption(options);

            task.run();
            task.close();

            return outputStream.toByteArray();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            if (engine != null)
                engine.destroy();
            Platform.shutdown();
        }

        return null;
    }

}