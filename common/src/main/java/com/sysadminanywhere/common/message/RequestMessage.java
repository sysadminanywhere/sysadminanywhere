package com.sysadminanywhere.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestMessage {

    private String id;
    private String serviceName;
    private String action;
    private Object data;

}