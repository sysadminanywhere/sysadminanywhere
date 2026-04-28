package com.sysadminanywhere.common.incident.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentItem {

    private Long id;
    private Long ticketId;
    private String author;
    private String content;
    private Boolean isInternal;
    private LocalDateTime createdAt;

}
