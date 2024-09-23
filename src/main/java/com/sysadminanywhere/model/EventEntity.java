package com.sysadminanywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventEntity {

    @WMIAttribute(name = "RecordNumber")
    String recordNumber;

    @WMIAttribute(name = "EventType")
    Long eventType;

    @WMIAttribute(name = "EventCode")
    String eventCode;

    @WMIAttribute(name = "Type")
    String type;

    @WMIAttribute(name = "TimeGenerated")
    LocalDateTime timeGenerated;

    @WMIAttribute(name = "SourceName")
    String sourceName;

    @WMIAttribute(name = "Category")
    String category;

    @WMIAttribute(name = "User")
    String user;

    @WMIAttribute(name = "Message")
    String message;

    @WMIAttribute(name = "Logfile")
    String logfile;
}
