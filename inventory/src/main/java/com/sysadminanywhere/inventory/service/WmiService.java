package com.sysadminanywhere.inventory.service;

import lombok.SneakyThrows;
import org.sentrysoftware.wmi.WmiHelper;
import org.sentrysoftware.wmi.wbem.WmiWbemServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WmiService {

    String username;
    char[] password;

    private int timeOut = 30000;

    public void init(String userName, String password) {
        this.username = userName;
        this.password = password.toCharArray();
    }

    @SneakyThrows
    public List<Map<String, Object>> execute(String hostName, String wqlQuery) {
        final String namespace = WmiHelper.DEFAULT_NAMESPACE;
        String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

        List<Map<String, Object>> result;

        try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, username, password)) {
            result = wbemServices.executeWql(wqlQuery, timeOut);
        }

        return result;
    }

    @SneakyThrows
    public void invoke(String hostName, String className, String methodName, Map<String, Object> inputMap) {
        final String namespace = WmiHelper.DEFAULT_NAMESPACE;
        String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

        try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, username, password)) {
            wbemServices.executeMethod(networkResource, className, methodName, inputMap);
        }
    }

}