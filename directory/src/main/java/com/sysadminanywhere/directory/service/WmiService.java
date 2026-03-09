package com.sysadminanywhere.directory.service;

import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.wmi.WmiHelper;
import org.sentrysoftware.wmi.exceptions.WmiComException;
import org.sentrysoftware.wmi.exceptions.WqlQuerySyntaxException;
import org.sentrysoftware.wmi.wbem.WmiWbemServices;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class WmiService {

    private final int timeOut = 30000;

    private final LdapService ldapService;
    private final VaultService vaultService;

    public WmiService(LdapService ldapService, VaultService vaultService) {
        this.ldapService = ldapService;
        this.vaultService = vaultService;
    }

    /**
     * Выполнение WMI запроса на удаленном хосте
     * @param hostName имя хоста
     * @param wqlQuery WQL запрос
     * @return список результатов
     * @throws WmiComException при ошибке WMI
     * @throws WqlQuerySyntaxException при синтаксической ошибке запроса
     * @throws TimeoutException при timeout
     */
    @Cacheable(value = "wmi_execute", key = "{#hostName, #wqlQuery}")
    public List<Map<String, Object>> execute(String hostName, String wqlQuery) throws WmiComException, WqlQuerySyntaxException, TimeoutException {
        try {
            hostName = checkHostName(hostName);

            final String namespace = WmiHelper.DEFAULT_NAMESPACE;
            String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

            List<Map<String, Object>> result;

            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            String service = resolveServiceFromContext();
            String password = getPasswordForContext(service, userName);

            try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, userName, password.toCharArray())) {
                result = wbemServices.executeWql(wqlQuery, timeOut);
            }

            log.info("WMI query executed successfully on host: {}", hostName);
            return result;
        } catch (WmiComException | WqlQuerySyntaxException | TimeoutException e) {
            log.error("Error executing WMI query on host {}: {}", hostName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error executing WMI query on host {}: {}", hostName, e.getMessage());
            throw new WmiComException("Failed to execute WMI query: " + e.getMessage());
        }
    }

    /**
     * Очистка кэша WMI запроса
     * @param hostName имя хоста
     * @param wqlQuery WQL запрос
     */
    @CacheEvict(value = "wmi_execute", key = "{#hostName, #wqlQuery}")
    public void clearExecuteCache(String hostName, String wqlQuery) {
        log.info("Cache cleared for WMI query on host: {}", hostName);
    }

    /**
     * Вызов WMI метода на удаленном хосте
     * @param hostName имя хоста
     * @param path путь к объекту
     * @param className название класса
     * @param methodName название метода
     * @param inputMap входные параметры
     * @return результат вызова метода
     * @throws WmiComException при ошибке WMI
     */
    public Map<String, Object> invoke(String hostName, String path, String className, String methodName, Map<String, Object> inputMap) throws WmiComException {
        try {
            hostName = checkHostName(hostName);

            final String namespace = WmiHelper.DEFAULT_NAMESPACE;
            String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

            Map<String, Object> result;

            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            String service = resolveServiceFromContext();
            String password = getPasswordForContext(service, userName);

            try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, userName, password.toCharArray())) {
                result = wbemServices.executeMethod(path, className, methodName, inputMap);
            }

            log.info("WMI method {} invoked successfully on host {}", methodName, hostName);
            return result;
        } catch (WmiComException e) {
            log.error("Error invoking WMI method {} on host {}: {}", methodName, hostName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error invoking WMI method {} on host {}: {}", methodName, hostName, e.getMessage());
            throw new WmiComException("Failed to invoke WMI method: " + e.getMessage());
        }
    }

    /**
     * Выполнение системной команды на удаленном хосте
     * @param hostName имя хоста
     * @param command команда для выполнения
     * @param workingDirectory рабочая директория
     * @throws WmiComException при ошибке WMI
     * @throws TimeoutException при timeout
     */
    public void executeCommand(String hostName, String command, String workingDirectory) throws WmiComException, TimeoutException {
        try {
            hostName = checkHostName(hostName);

            final String namespace = WmiHelper.DEFAULT_NAMESPACE;
            String networkResource = WmiHelper.createNetworkResource(hostName, namespace);

            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            String service = resolveServiceFromContext();
            String password = getPasswordForContext(service, userName);

            try (WmiWbemServices wbemServices = WmiWbemServices.getInstance(networkResource, userName, password.toCharArray())) {
                wbemServices.executeCommand(command, workingDirectory, Charset.defaultCharset(), timeOut);
            }

            log.info("Command executed successfully on host {}: {}", hostName, command);
        } catch (WmiComException | TimeoutException e) {
            log.error("Error executing command on host {}: {}", hostName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error executing command on host {}: {}", hostName, e.getMessage());
            throw new WmiComException("Failed to execute command: " + e.getMessage());
        }
    }

    private String getPasswordForContext(String service, String userName) {
        String password = vaultService.getPassword(service, userName);
        if (password == null && !"legacy".equals(service)) {
            password = vaultService.getPassword(userName);
        }
        return password;
    }

    private String resolveServiceFromContext() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof String service && !service.isBlank()) {
            return service.trim().toLowerCase();
        }
        return "legacy";
    }

    /**
     * Проверка и нормализация имени хоста
     * @param hostName имя хоста
     * @return нормализованное имя хоста
     */
    private String checkHostName(String hostName) {
        String domainName = ldapService.getDomainName();

        //        if (!hostName.toLowerCase().endsWith(domainName))
        //            hostName += "." + domainName;

        return hostName;
    }

}
