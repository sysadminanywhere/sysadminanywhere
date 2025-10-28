package com.sysadminanywhere.inventory.wmi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventEntity {

    @WMIAttribute(name = "RecordNumber")
    long recordNumber;

    @WMIAttribute(name = "EventType")
    String eventType;

    @WMIAttribute(name = "EventCode")
    String eventCode;

    @WMIAttribute(name = "Type")
    String type;

    @WMIAttribute(name = "TimeGenerated")
    String timeGenerated;

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

    @WMIAttribute(name = "InsertionStrings")
    String[] insertionStrings;

}