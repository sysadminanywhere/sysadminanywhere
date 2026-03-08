package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import com.sysadminanywhere.directory.service.WmiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/wmi")
@RequiredArgsConstructor
public class WmiController {

    private final WmiService wmiService;

    /**
     * Выполнение WMI запроса на удаленном хосте
     */
    @PostMapping("/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> execute(@Valid @RequestBody ExecuteDto executeDto) {
        try {
            List<Map<String, Object>> result = wmiService.execute(
                    executeDto.getHostName(),
                    executeDto.getWqlQuery());

            log.info("WMI execute successful for host: {}", executeDto.getHostName());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for execute: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error executing WMI query on host: {}", executeDto.getHostName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to execute WMI query"));
        }
    }

    /**
     * Очистка кэша WMI запроса
     */
    @PostMapping("/execute/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> clearExecuteCache(@Valid @RequestBody ExecuteDto executeDto) {
        try {
            wmiService.clearExecuteCache(executeDto.getHostName(), executeDto.getWqlQuery());

            log.info("WMI execute cache cleared for host: {}", executeDto.getHostName());
            return ResponseEntity.ok(Map.of("message", "Cache cleared successfully"));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for cache clear: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error clearing WMI cache for host: {}", executeDto.getHostName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to clear cache"));
        }
    }

    /**
     * Вызов WMI метода на удаленном хосте
     */
    @PostMapping("/invoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> invoke(@Valid @RequestBody InvokeDto invokeDto) {
        try {
            Map<String, Object> result = wmiService.invoke(
                    invokeDto.getHostName(),
                    invokeDto.getPath(),
                    invokeDto.getClassName(),
                    invokeDto.getMethodName(),
                    invokeDto.getInputMap());

            log.info("WMI invoke successful for host: {}, method: {}",
                    invokeDto.getHostName(), invokeDto.getMethodName());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for invoke: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error invoking WMI method on host: {}", invokeDto.getHostName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to invoke WMI method"));
        }
    }

    /**
     * Выполнение системной команды на удаленном хосте
     */
    @PostMapping("/command")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> command(@Valid @RequestBody CommandDto commandDto) {
        try {
            wmiService.executeCommand(
                    commandDto.getHostName(),
                    commandDto.getCommand(),
                    commandDto.getWorkingDirectory());

            log.info("Command executed successfully on host: {}", commandDto.getHostName());
            return ResponseEntity.ok(Map.of("message", "Command executed successfully"));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for command execution: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("Error executing command on host: {}", commandDto.getHostName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to execute command"));
        }
    }

}

