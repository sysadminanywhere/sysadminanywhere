package com.sysadminanywhere.directory.service;

import org.sentrysoftware.wmi.WmiHelper;
import org.sentrysoftware.wmi.exceptions.WmiComException;
import org.sentrysoftware.wmi.exceptions.WqlQuerySyntaxException;
import org.sentrysoftware.wmi.wbem.WmiWbemServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
public class WmiService {

    @Value("${ldap.host.username:}")
    private String userName;

    @Value("${ldap.host.password:}")
    private String password;

    private final int timeOut = 30000;

    @Cacheable(value = "wmi_execute", key = "{#hostName, #wqlQuery}")
    public List<Map<String, Object>> execute(String hostName, String wqlQuery) throws WmiComException, WqlQuerySyntaxException, TimeoutException {
        final String namespace = WmiHelper.DEFAULT_NAMESPACE;
        String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

        List<Map<String, Object>> result;

        try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, userName, password.toCharArray())) {
            result = wbemServices.executeWql(wqlQuery, timeOut);
        }

        return result;
    }

    @CacheEvict(value = "wmi_execute", key = "{#hostName, #wqlQuery}")
    public void clearExecuteCache(String hostName, String wqlQuery) {
    }

    public Map<String, Object> invoke(String hostName, String path, String className, String methodName, Map<String, Object> inputMap) throws WmiComException {
        final String namespace = WmiHelper.DEFAULT_NAMESPACE;
        String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

        Map<String, Object> result;

        try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, userName, password.toCharArray())) {
            result = wbemServices.executeMethod(path, className, methodName, inputMap);
        }

        return result;
    }

    public void executeCommand(String hostName, String command, String workingDirectory) throws WmiComException, TimeoutException {
        final String namespace = WmiHelper.DEFAULT_NAMESPACE;
        String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

        try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, userName, password.toCharArray())) {
            wbemServices.executeCommand(command, workingDirectory, Charset.defaultCharset(), timeOut);
        }
    }

}