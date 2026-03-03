package com.sysadminanywhere.incident.mapper;

import com.sysadminanywhere.incident.entity.IncidentEntity;
import com.sysadminanywhere.common.incident.model.IncidentItem;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class IncidentMapper {

    private IncidentMapper() {
    }

    // ============================================================
    // Entity -> DTO
    // ============================================================

    public static IncidentItem toItem(IncidentEntity entity) {
        if (entity == null) {
            return null;
        }

        IncidentItem item = new IncidentItem();

        item.setId(entity.getId());
        item.setSignalId(entity.getSignalId());
        item.setName(entity.getName());
        item.setSeverity(entity.getSeverity());
        item.setStatus(entity.getStatus());
        item.setEventCount(entity.getEventCount());
        item.setRecommendation(entity.getRecommendation());
        item.setContext(entity.getContext());
        item.setAffectedUser(entity.getAffectedUser());
        item.setDeduplicationKey(entity.getDeduplicationKey());
        item.setMeta(entity.getMeta());
        item.setFirstEventTime(entity.getFirstEventTime());
        item.setLastEventTime(entity.getLastEventTime());
        item.setCreatedAt(entity.getCreatedAt());
        item.setUpdatedAt(entity.getUpdatedAt());
        item.setClosedBy(entity.getClosedBy());
        item.setClosedAt(entity.getClosedAt());
        item.setMachineName(entity.getMachineName());

        return item;
    }

    public static List<IncidentItem> toItemList(List<IncidentEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(IncidentMapper::toItem)
                .collect(Collectors.toList());
    }

    // ============================================================
    // DTO -> Entity
    // ============================================================

    public static IncidentEntity toEntity(IncidentItem item) {
        if (item == null) {
            return null;
        }

        return IncidentEntity.builder()
                .id(item.getId())
                .signalId(item.getSignalId())
                .name(item.getName())
                .severity(item.getSeverity())
                .status(item.getStatus())
                .eventCount(item.getEventCount())
                .recommendation(item.getRecommendation())
                .context(item.getContext())
                .affectedUser(item.getAffectedUser())
                .deduplicationKey(item.getDeduplicationKey())
                .meta(item.getMeta())
                .firstEventTime(item.getFirstEventTime())
                .lastEventTime(item.getLastEventTime())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .closedBy(item.getClosedBy())
                .closedAt(item.getClosedAt())
                .machineName(item.getMachineName())
                .build();
    }

    // ============================================================
    // Partial update (важно для PATCH)
    // ============================================================

    public static void updateEntityFromItem(IncidentItem item, IncidentEntity entity) {
        if (item == null || entity == null) {
            return;
        }

        entity.setSignalId(item.getSignalId());
        entity.setName(item.getName());
        entity.setSeverity(item.getSeverity());
        entity.setStatus(item.getStatus());
        entity.setEventCount(item.getEventCount());
        entity.setRecommendation(item.getRecommendation());
        entity.setContext(item.getContext());
        entity.setAffectedUser(item.getAffectedUser());
        entity.setDeduplicationKey(item.getDeduplicationKey());
        entity.setMeta(item.getMeta());
        entity.setFirstEventTime(item.getFirstEventTime());
        entity.setLastEventTime(item.getLastEventTime());
        entity.setClosedBy(item.getClosedBy());
        entity.setClosedAt(item.getClosedAt());
        entity.setMachineName(item.getMachineName());
    }
}