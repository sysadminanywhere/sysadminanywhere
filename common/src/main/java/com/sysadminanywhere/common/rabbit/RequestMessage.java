package com.sysadminanywhere.common.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestMessage {

    private String id;
    private String serviceName;
    private String action;
    private Object data;
    private LocalDateTime timestamp;

}