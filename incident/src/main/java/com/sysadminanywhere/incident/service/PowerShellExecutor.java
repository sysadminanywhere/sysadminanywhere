package com.sysadminanywhere.incident.service;

import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;

public interface PowerShellExecutor {
    WinRmToolResponse execute(String script);
}