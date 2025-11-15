package com.sysadminanywhere.service;

import com.sysadminanywhere.domain.ObjectToListMapConverter;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportGeneratorService {

    public void generateReport(List<?> objectList,
                               String column1Field, String column2Field, String column3Field,
                               String column1Name, String column2Name, String column3Name,
                               String outputPath) throws Exception {

        List<Map<String, Object>> dataList = ObjectToListMapConverter.convertToListMap(objectList);

        EngineConfig config = new EngineConfig();
        Platform.startup(config);
        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        IReportEngine engine = factory.createReportEngine(config);

        try {
            IReportRunnable design = engine.openReportDesign("reports/columns3.rptdesign");
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
            options.setOutputFileName(outputPath);
            options.setOutputFormat("pdf");
            task.setRenderOption(options);

            task.run();
            task.close();

        } finally {
            engine.destroy();
            Platform.shutdown();
        }
    }

}