package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.wmi.dto.CommandDto;
import com.sysadminanywhere.common.wmi.dto.ExecuteDto;
import com.sysadminanywhere.common.wmi.dto.InvokeDto;
import com.sysadminanywhere.directory.service.WmiService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/wmi")
public class WmiController {

    private final WmiService wmiService;

    public WmiController(WmiService wmiService) {
        this.wmiService = wmiService;
    }

    @PostMapping("/execute")
    public ResponseEntity<List<Map<String, Object>>> execute(@RequestBody ExecuteDto executeDto) {
        try {
            return new ResponseEntity<>(wmiService.execute(executeDto.getHostName(), executeDto.getWqlQuery()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/execute/clear")
    public void clearExecuteCache(@RequestBody ExecuteDto executeDto) {
        try {
            wmiService.clearExecuteCache(executeDto.getHostName(), executeDto.getWqlQuery());
        } catch (Exception e) {
        }
    }

    @PostMapping("/invoke")
    public ResponseEntity<Map<String, Object>> invoke(@RequestBody InvokeDto invokeDto) {
        try {
            return new ResponseEntity<>(wmiService.invoke(invokeDto.getHostName(),
                    invokeDto.getPath(),
                    invokeDto.getClassName(),
                    invokeDto.getMethodName(),
                    invokeDto.getInputMap()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.NO_CONTENT);
        }
    }

    @PostMapping("/api/wmi/command")
    public ResponseEntity<Boolean> command(@RequestBody CommandDto commandDto) {
        try {
            wmiService.executeCommand(commandDto.getHostName(),
                    commandDto.getCommand(),
                    commandDto.getWorkingDirectory());
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
        }
    }

}
