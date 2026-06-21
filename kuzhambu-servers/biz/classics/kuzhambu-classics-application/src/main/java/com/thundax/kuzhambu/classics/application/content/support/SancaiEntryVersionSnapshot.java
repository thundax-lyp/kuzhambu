package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.common.core.id.Identifier;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
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
        int priority,
        List<ImageResource> images) {

    public static SancaiEntryVersionSnapshot from(SancaiEntry entry) {
        return from(entry, List.of());
    }

    public static SancaiEntryVersionSnapshot from(SancaiEntry entry, List<SancaiEntryImage> images) {
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
                entry.getPriority(),
                imageResources(images));
    }

    public static SancaiEntryVersionSnapshot fromImageResources(SancaiEntry entry, List<ImageResource> images) {
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
                entry.getPriority(),
                images == null
                        ? List.of()
                        : images.stream()
                                .filter(ImageResource::currentUsed)
                                .sorted(Comparator.comparingInt(ImageResource::priority))
                                .toList());
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
        snapshot.put("images", images.stream().map(ImageResource::toMap).toList());
        return snapshot;
    }

    private static List<ImageResource> imageResources(List<SancaiEntryImage> images) {
        return images == null
                ? List.of()
                : images.stream()
                        .filter(SancaiEntryImage::isCurrentUsed)
                        .sorted(Comparator.comparingInt(SancaiEntryImage::getPriority))
                        .map(ImageResource::from)
                        .toList();
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

    public record ImageResource(
            Long imageId,
            Long storageObjectId,
            String originalFilename,
            String contentType,
            Long size,
            String imageType,
            String title,
            boolean currentUsed,
            int priority) {

        public static ImageResource from(SancaiEntryImage image) {
            return from(image, null);
        }

        public static ImageResource from(SancaiEntryImage image, StoredObject storage) {
            return new ImageResource(
                    id(image.getId()),
                    id(image.getStorageObjectId()),
                    storage == null ? null : storage.getOriginalFilename(),
                    storage == null ? null : storage.getContentType(),
                    storage == null ? null : storage.getSize(),
                    value(image.getImageType()),
                    image.getTitle(),
                    image.isCurrentUsed(),
                    image.getPriority());
        }

        public Map<String, Object> toMap() {
            Map<String, Object> resource = new LinkedHashMap<>();
            resource.put("imageId", imageId);
            resource.put("storageObjectId", storageObjectId);
            resource.put("originalFilename", originalFilename);
            resource.put("contentType", contentType);
            resource.put("size", size);
            resource.put("imageType", imageType);
            resource.put("title", title);
            resource.put("currentUsed", currentUsed);
            resource.put("priority", priority);
            return resource;
        }
    }
}
