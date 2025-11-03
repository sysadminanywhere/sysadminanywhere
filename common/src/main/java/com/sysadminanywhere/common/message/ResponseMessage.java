package com.sysadminanywhere.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage {

    private String id;
    private String status;
    private String message;
    private Object result;

}