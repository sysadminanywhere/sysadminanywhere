package com.sysadminanywhere.incident.mapper;

import com.sysadminanywhere.incident.entity.CommentEntity;
import com.sysadminanywhere.incident.entity.TicketEntity;
import com.sysadminanywhere.common.incident.model.CommentItem;
import com.sysadminanywhere.common.incident.model.TicketItem;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TicketMapper {

    private TicketMapper() {
    }

    // ============================================================
    // Entity -> DTO
    // ============================================================

    public static TicketItem toItem(TicketEntity entity) {
        if (entity == null) {
            return null;
        }

        return TicketItem.builder()
                .id(entity.getId())
                .ticketNumber(entity.getTicketNumber())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .category(entity.getCategory())
                .requester(entity.getRequester())
                .assignee(entity.getAssignee())
                .incidentId(entity.getIncidentId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .resolvedAt(entity.getResolvedAt())
                .resolvedBy(entity.getResolvedBy())
                .resolution(entity.getResolution())
                .build();
    }

    public static List<TicketItem> toItemList(List<TicketEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(TicketMapper::toItem)
                .collect(Collectors.toList());
    }

    // ============================================================
    // DTO -> Entity
    // ============================================================

    public static TicketEntity toEntity(TicketItem item) {
        if (item == null) {
            return null;
        }

        return TicketEntity.builder()
                .id(item.getId())
                .ticketNumber(item.getTicketNumber())
                .title(item.getTitle())
                .description(item.getDescription())
                .status(item.getStatus())
                .priority(item.getPriority())
                .category(item.getCategory())
                .requester(item.getRequester())
                .assignee(item.getAssignee())
                .incidentId(item.getIncidentId())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .resolvedAt(item.getResolvedAt())
                .resolvedBy(item.getResolvedBy())
                .resolution(item.getResolution())
                .build();
    }

    // ============================================================
    // Partial update
    // ============================================================

    public static void updateEntityFromItem(TicketItem item, TicketEntity entity) {
        if (item == null || entity == null) {
            return;
        }

        if (item.getTitle() != null) {
            entity.setTitle(item.getTitle());
        }
        if (item.getDescription() != null) {
            entity.setDescription(item.getDescription());
        }
        if (item.getStatus() != null) {
            entity.setStatus(item.getStatus());
        }
        if (item.getPriority() != null) {
            entity.setPriority(item.getPriority());
        }
        if (item.getCategory() != null) {
            entity.setCategory(item.getCategory());
        }
        if (item.getRequester() != null) {
            entity.setRequester(item.getRequester());
        }
        if (item.getAssignee() != null) {
            entity.setAssignee(item.getAssignee());
        }
        if (item.getIncidentId() != null) {
            entity.setIncidentId(item.getIncidentId());
        }
        if (item.getResolvedAt() != null) {
            entity.setResolvedAt(item.getResolvedAt());
        }
        if (item.getResolvedBy() != null) {
            entity.setResolvedBy(item.getResolvedBy());
        }
        if (item.getResolution() != null) {
            entity.setResolution(item.getResolution());
        }
    }

    // ============================================================
    // Comment mapping
    // ============================================================

    public static CommentItem toCommentItem(CommentEntity entity) {
        if (entity == null) {
            return null;
        }

        return CommentItem.builder()
                .id(entity.getId())
                .ticketId(entity.getTicketId())
                .author(entity.getAuthor())
                .content(entity.getContent())
                .isInternal(entity.getIsInternal())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static List<CommentItem> toCommentItemList(List<CommentEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(TicketMapper::toCommentItem)
                .collect(Collectors.toList());
    }

    public static CommentEntity toCommentEntity(CommentItem item) {
        if (item == null) {
            return null;
        }

        return CommentEntity.builder()
                .id(item.getId())
                .ticketId(item.getTicketId())
                .author(item.getAuthor())
                .content(item.getContent())
                .isInternal(item.getIsInternal())
                .createdAt(item.getCreatedAt())
                .build();
    }

}
