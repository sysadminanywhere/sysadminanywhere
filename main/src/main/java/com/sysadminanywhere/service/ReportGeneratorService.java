package com.sysadminanywhere.service;

import com.sysadminanywhere.domain.ObjectToListMapConverter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportGeneratorService {

    public byte[] generateReport(List<?> objectList,
                                 String column1Field, String column2Field, String column3Field,
                                 String column1Name, String column2Name, String column3Name) {

        List<Map<String, Object>> dataList = ObjectToListMapConverter.convertToListMap(objectList);

        IReportEngine engine = null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            EngineConfig config = new EngineConfig();
            Platform.startup(config);
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            engine = factory.createReportEngine(config);

            ClassPathResource resource = new ClassPathResource("reports/columns3.rptdesign");

            IReportRunnable design = engine.openReportDesign(resource.getInputStream());
            IRunAndRenderTask task = engine.createRunAndRenderTask(design);

            HashMap<String, Object> params = new HashMap<>();
            params.put("column1_field", column1Field);
            params.put("column2_field", column2Field);
            params.put("column3_field", column3Field);
            params.put("column1_name", column1Name);
            params.put("column2_name", column2Name);
            params.put("column3_name", column3Name);

            task.setParameterValues(params);

            task.getAppContext().put("DATA_LIST", dataList);

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