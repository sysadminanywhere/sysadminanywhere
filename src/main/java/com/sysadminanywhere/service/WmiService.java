package com.sysadminanywhere.service;

import lombok.SneakyThrows;
import org.sentrysoftware.wmi.WmiHelper;
import org.sentrysoftware.wmi.exceptions.WmiComException;
import org.sentrysoftware.wmi.exceptions.WqlQuerySyntaxException;
import org.sentrysoftware.wmi.wbem.WmiWbemServices;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
public class WmiService {

    String username;
    char[] password;

    private int timeOut = 30000;

    public void init(String userName, String password) {
        this.username = userName;
        this.password = password.toCharArray();
    }

    @Cacheable(value = "wmi_execute", key = "{#hostName, #wqlQuery}")
    public List<Map<String, Object>> execute(String hostName, String wqlQuery) throws WmiComException, WqlQuerySyntaxException, TimeoutException {
        final String namespace = WmiHelper.DEFAULT_NAMESPACE;
        String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

        List<Map<String, Object>> result;

        try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, username, password)) {
            result = wbemServices.executeWql(wqlQuery, timeOut);
        }

        return result;
    }

    public void invoke(String hostName, String className, String methodName, Map<String, Object> inputMap) throws WmiComException {
        final String namespace = WmiHelper.DEFAULT_NAMESPACE;
        String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

        String path = networkResource + ":" + className;

        try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, username, password)) {
            wbemServices.executeMethod(path, className, methodName, inputMap);
        }
    }

    public void executeCommand(String hostName, String command) throws WmiComException, TimeoutException {
        final String namespace = WmiHelper.DEFAULT_NAMESPACE;
        String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

        try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, username, password)) {
            wbemServices.executeCommand(command, "c:/", Charset.defaultCharset(), 30000);
        }
    }

}