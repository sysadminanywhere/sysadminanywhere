package com.sysadminanywhere.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VersionService {

    @Value("${info.version}")
    private String version;

    public String getVersion() {
        return version;
    }
}