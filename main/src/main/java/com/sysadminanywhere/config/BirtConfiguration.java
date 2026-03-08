package com.sysadminanywhere.config;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BirtConfiguration {

    @Bean
    public IReportEngine reportEngine() throws BirtException {
        EngineConfig config = new EngineConfig();
        config.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY,
                Thread.currentThread().getContextClassLoader());

        Platform.startup(config);
        IReportEngineFactory factory = (IReportEngineFactory) Platform
                .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        return factory.createReportEngine(config);
    }

}