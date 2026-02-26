package com.sysadminanywhere.incident.service;

import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.AuthSchemes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!mock") // Используется ВСЕГДА, кроме когда активен mock
@Slf4j
public class RealPowerShellExecutor implements PowerShellExecutor {

    @Value("${wef.server}")
    private String hostname;

    @Value("${wef.port}")
    private int port;

    @Value("${wef.username}")
    private String username;

    @Value("${wef.password}")
    private String password;

    @Value("${wef.use.ssl}")
    private boolean useSsl;

    private final WinRmClientContext context = WinRmClientContext.newInstance();

    @Override
    public WinRmToolResponse execute(String script) {
        log.info("Executing PowerShell on {}:{}", hostname, port);

        WinRmTool tool = WinRmTool.Builder.builder(hostname, username, password)
                .authenticationScheme(AuthSchemes.BASIC)
                .port(port)
                .useHttps(useSsl)
                .context(context)
                .disableCertificateChecks(true) // только если нужно (не для prod!)
                .build();

        return tool.executePs(script);
    }

    // Очистка ресурсов при уничтожении бина
    public void destroy() {
        context.shutdown();
    }

}