package com.sysadminanywhere.incident.service;

import io.cloudsoft.winrm4j.client.WinRmClientContext;
import io.cloudsoft.winrm4j.winrm.WinRmTool;
import org.apache.http.client.config.AuthSchemes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WinRmClientCompatibilityTest {
    @Test
    void initializesWinRmWithThePackagedCxfStack() {
        WinRmClientContext context = WinRmClientContext.newInstance();
        try {
            var client = WinRmTool.Builder.builder("localhost", "fixture-user", "fixture-password")
                    .authenticationScheme(AuthSchemes.BASIC)
                    .port(5985)
                    .context(context)
                    .build();
            assertThat(client).isNotNull();
        } finally {
            context.shutdown();
        }
    }
}
