package com.sysadminanywhere.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookSubscription {
    private String id;
    private boolean enabled;
    private String url;
    private String secret;
    private List<String> events;
}
