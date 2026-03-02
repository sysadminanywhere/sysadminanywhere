package com.sysadminanywhere.incident.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @JsonProperty("RecordId")
    private Long recordId;

    @JsonProperty("EventId")
    private Integer eventId;

    @JsonProperty("TimeCreated")
    private OffsetDateTime timeCreated;

    @JsonProperty("MachineName")
    private String machineName;

    @JsonProperty("LevelDisplayName")
    private String levelDisplayName;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("EventData")
    private Map<String, String> eventData;

}