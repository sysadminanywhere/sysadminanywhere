package com.sysadminanywhere.common.rabbit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage {

    private String requestId;
    private String status;
    private String message;
    private Object result;
    private LocalDateTime timestamp;

}