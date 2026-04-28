package com.sysadminanywhere.common.incident.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketItem {

    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private Category category;
    private String requester;
    private String assignee;
    private Long incidentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String resolution;
    private List<CommentItem> comments;

}
