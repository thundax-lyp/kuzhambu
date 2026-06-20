package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.common.core.id.Identifier;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public record SancaiEntryVersionSnapshot(
        String contentType,
        Long contentId,
        String contentUpdatedAt,
        Long volumeId,
        String title,
        String originalText,
        String translationText,
        String summary,
        String lifecycleStatus,
        String visibility,
        String translationStatus,
        String imageStatus,
        String visualAssetStatus,
        String refinementStatus,
        int priority) {

    public static SancaiEntryVersionSnapshot from(SancaiEntry entry) {
        return new SancaiEntryVersionSnapshot(
                ClassicsContentType.SANCAI_ENTRY.value(),
                id(entry.contentId()),
                date(entry.contentUpdatedAt()),
                entry.getVolumeId() == null ? null : entry.getVolumeId().value(),
                entry.getTitle(),
                entry.getOriginalText(),
                entry.getTranslationText(),
                entry.getSummary(),
                value(entry.getLifecycleStatus()),
                value(entry.getVisibility()),
                value(entry.getTranslationStatus()),
                value(entry.getImageStatus()),
                value(entry.getVisualAssetStatus()),
                value(entry.getRefinementStatus()),
                entry.getPriority());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("contentType", contentType);
        snapshot.put("contentId", contentId);
        snapshot.put("contentUpdatedAt", contentUpdatedAt);
        snapshot.put("volumeId", volumeId);
        snapshot.put("title", title);
        snapshot.put("originalText", originalText);
        snapshot.put("translationText", translationText);
        snapshot.put("summary", summary);
        snapshot.put("lifecycleStatus", lifecycleStatus);
        snapshot.put("visibility", visibility);
        snapshot.put("translationStatus", translationStatus);
        snapshot.put("imageStatus", imageStatus);
        snapshot.put("visualAssetStatus", visualAssetStatus);
        snapshot.put("refinementStatus", refinementStatus);
        snapshot.put("priority", priority);
        return snapshot;
    }

    private static Long id(Identifier<Long> id) {
        return id == null ? null : id.value();
    }

    private static String date(Date date) {
        return date == null ? null : date.toInstant().toString();
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
