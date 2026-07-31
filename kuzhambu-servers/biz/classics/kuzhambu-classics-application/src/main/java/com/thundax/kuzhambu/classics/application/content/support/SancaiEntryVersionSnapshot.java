package com.thundax.kuzhambu.classics.application.content.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.common.core.id.Identifier;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SancaiEntryVersionSnapshot(
        String contentType,
        Long contentId,
        String contentUpdatedAt,
        Long volumeId,
        String volumeTitle,
        Long categoryId,
        String categoryTitle,
        String title,
        String originalText,
        String translationText,
        String summary,
        String lifecycleStatus,
        String translationStatus,
        String imageStatus,
        String visualAssetStatus,
        String refinementStatus,
        int priority,
        List<ImageResource> images,
        List<SancaiTagSnapshot> tags,
        List<SancaiQaPairSnapshot> qaPairs) {

    public static SancaiEntryVersionSnapshot from(SancaiEntry entry) {
        return from(entry, List.of());
    }

    public static SancaiEntryVersionSnapshot from(SancaiEntry entry, List<SancaiEntryImage> images) {
        return from(entry, null, null, null, images, List.of(), List.of());
    }

    public static SancaiEntryVersionSnapshot from(
            SancaiEntry entry,
            String volumeTitle,
            Long categoryId,
            String categoryTitle,
            List<SancaiEntryImage> images,
            List<ClassicsContentTag> tags,
            List<ClassicsContentQaPair> qaPairs) {
        return new SancaiEntryVersionSnapshot(
                ClassicsContentType.SANCAI_ENTRY.value(),
                id(entry.contentId()),
                date(entry.contentUpdatedAt()),
                entry.getVolumeId() == null ? null : entry.getVolumeId().value(),
                volumeTitle,
                categoryId,
                categoryTitle,
                entry.getTitle(),
                entry.getOriginalText(),
                entry.getTranslationText(),
                entry.getSummary(),
                value(entry.getLifecycleStatus()),
                value(entry.getTranslationStatus()),
                value(entry.getImageStatus()),
                value(entry.getVisualAssetStatus()),
                value(entry.getRefinementStatus()),
                entry.getPriority(),
                imageResources(images),
                tagSnapshots(tags),
                qaPairSnapshots(qaPairs));
    }

    public static SancaiEntryVersionSnapshot fromImageResources(SancaiEntry entry, List<ImageResource> images) {
        return fromImageResources(entry, null, null, null, images, List.of(), List.of());
    }

    public static SancaiEntryVersionSnapshot fromImageResources(
            SancaiEntry entry,
            String volumeTitle,
            Long categoryId,
            String categoryTitle,
            List<ImageResource> images,
            List<ClassicsContentTag> tags,
            List<ClassicsContentQaPair> qaPairs) {
        return new SancaiEntryVersionSnapshot(
                ClassicsContentType.SANCAI_ENTRY.value(),
                id(entry.contentId()),
                date(entry.contentUpdatedAt()),
                entry.getVolumeId() == null ? null : entry.getVolumeId().value(),
                volumeTitle,
                categoryId,
                categoryTitle,
                entry.getTitle(),
                entry.getOriginalText(),
                entry.getTranslationText(),
                entry.getSummary(),
                value(entry.getLifecycleStatus()),
                value(entry.getTranslationStatus()),
                value(entry.getImageStatus()),
                value(entry.getVisualAssetStatus()),
                value(entry.getRefinementStatus()),
                entry.getPriority(),
                images == null
                        ? List.of()
                        : images.stream()
                                .sorted(Comparator.comparingInt(ImageResource::priority))
                                .toList(),
                tagSnapshots(tags),
                qaPairSnapshots(qaPairs));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("contentType", contentType);
        snapshot.put("contentId", contentId);
        snapshot.put("contentUpdatedAt", contentUpdatedAt);
        snapshot.put("volumeId", volumeId);
        snapshot.put("volumeTitle", volumeTitle);
        snapshot.put("categoryId", categoryId);
        snapshot.put("categoryTitle", categoryTitle);
        snapshot.put("title", title);
        snapshot.put("originalText", originalText);
        snapshot.put("translationText", translationText);
        snapshot.put("summary", summary);
        snapshot.put("lifecycleStatus", lifecycleStatus);
        snapshot.put("translationStatus", translationStatus);
        snapshot.put("imageStatus", imageStatus);
        snapshot.put("visualAssetStatus", visualAssetStatus);
        snapshot.put("refinementStatus", refinementStatus);
        snapshot.put("priority", priority);
        snapshot.put("images", images.stream().map(ImageResource::toMap).toList());
        snapshot.put("tags", tags.stream().map(SancaiTagSnapshot::toMap).toList());
        snapshot.put(
                "qaPairs", qaPairs.stream().map(SancaiQaPairSnapshot::toMap).toList());
        return snapshot;
    }

    public static SancaiEntryVersionSnapshot from(JsonNode snapshot) {
        return new SancaiEntryVersionSnapshot(
                text(snapshot, "contentType"),
                longValue(snapshot, "contentId"),
                text(snapshot, "contentUpdatedAt"),
                longValue(snapshot, "volumeId"),
                text(snapshot, "volumeTitle"),
                longValue(snapshot, "categoryId"),
                text(snapshot, "categoryTitle"),
                text(snapshot, "title"),
                text(snapshot, "originalText"),
                text(snapshot, "translationText"),
                text(snapshot, "summary"),
                text(snapshot, "lifecycleStatus"),
                text(snapshot, "translationStatus"),
                text(snapshot, "imageStatus"),
                text(snapshot, "visualAssetStatus"),
                text(snapshot, "refinementStatus"),
                intValue(snapshot, "priority", 0),
                ImageResource.list(snapshot, "images"),
                SancaiTagSnapshot.list(snapshot, "tags"),
                SancaiQaPairSnapshot.list(snapshot, "qaPairs"));
    }

    private static List<SancaiTagSnapshot> tagSnapshots(List<ClassicsContentTag> tags) {
        return tags == null
                ? List.of()
                : tags.stream()
                        .filter(tag -> tag != null && tag.getStatus() == ClassicsContentTagStatus.ACTIVE)
                        .map(SancaiTagSnapshot::from)
                        .toList();
    }

    private static List<SancaiQaPairSnapshot> qaPairSnapshots(List<ClassicsContentQaPair> qaPairs) {
        return qaPairs == null
                ? List.of()
                : qaPairs.stream()
                        .filter(pair -> pair != null
                                && pair.getQuestion() != null
                                && !pair.getQuestion().isBlank()
                                && pair.getAnswer() != null
                                && !pair.getAnswer().isBlank())
                        .map(SancaiQaPairSnapshot::from)
                        .toList();
    }

    private static List<ImageResource> imageResources(List<SancaiEntryImage> images) {
        return images == null
                ? List.of()
                : images.stream()
                        .sorted(Comparator.comparingInt(SancaiEntryImage::getPriority))
                        .map(ImageResource::from)
                        .toList();
    }

    private static Long id(Identifier<Long> id) {
        return id == null ? null : id.value();
    }

    private static String date(Instant date) {
        return date == null ? null : date.toString();
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String text(JsonNode snapshot, String fieldName) {
        JsonNode value = snapshot == null ? null : snapshot.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static Long longValue(JsonNode snapshot, String fieldName) {
        JsonNode value = snapshot == null ? null : snapshot.get(fieldName);
        return value == null || value.isNull() ? null : value.asLong();
    }

    private static Integer intValue(JsonNode snapshot, String fieldName) {
        JsonNode value = snapshot == null ? null : snapshot.get(fieldName);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private static int intValue(JsonNode snapshot, String fieldName, int defaultValue) {
        Integer value = intValue(snapshot, fieldName);
        return value == null ? defaultValue : value;
    }

    private static boolean booleanValue(JsonNode snapshot, String fieldName) {
        JsonNode value = snapshot == null ? null : snapshot.get(fieldName);
        return value != null && !value.isNull() && value.asBoolean();
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

        public static ImageResource from(SancaiEntryImage image, StorageObjectFacadeDto storage) {
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

        public static ImageResource from(JsonNode snapshot) {
            return new ImageResource(
                    longValue(snapshot, "imageId"),
                    longValue(snapshot, "storageObjectId"),
                    text(snapshot, "originalFilename"),
                    text(snapshot, "contentType"),
                    longValue(snapshot, "size"),
                    text(snapshot, "imageType"),
                    text(snapshot, "title"),
                    booleanValue(snapshot, "currentUsed"),
                    intValue(snapshot, "priority", 0));
        }

        public static List<ImageResource> list(JsonNode snapshot, String fieldName) {
            JsonNode images = snapshot == null ? null : snapshot.get(fieldName);
            if (images == null || !images.isArray()) {
                return List.of();
            }
            List<ImageResource> result = new ArrayList<>(images.size());
            for (JsonNode node : images) {
                result.add(from(node));
            }
            return result;
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

    public record SancaiTagSnapshot(
            Long id, Long tagId, String tagNameSnapshot, String source, String status, Integer priority) {

        private static SancaiTagSnapshot from(ClassicsContentTag tag) {
            return new SancaiTagSnapshot(
                    SancaiEntryVersionSnapshot.id(tag.getId()),
                    tag.getTagId() == null ? null : tag.getTagId().value(),
                    tag.getTagNameSnapshot(),
                    value(tag.getSource()),
                    value(tag.getStatus()),
                    tag.getPriority());
        }

        public static SancaiTagSnapshot from(JsonNode snapshot) {
            return new SancaiTagSnapshot(
                    longValue(snapshot, "id"),
                    longValue(snapshot, "tagId"),
                    text(snapshot, "tagNameSnapshot"),
                    text(snapshot, "source"),
                    text(snapshot, "status"),
                    intValue(snapshot, "priority"));
        }

        public static List<SancaiTagSnapshot> list(JsonNode snapshot, String fieldName) {
            JsonNode tags = snapshot == null ? null : snapshot.get(fieldName);
            if (tags == null || !tags.isArray()) {
                return List.of();
            }
            List<SancaiTagSnapshot> result = new ArrayList<>(tags.size());
            for (JsonNode node : tags) {
                result.add(from(node));
            }
            return result;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("tagId", tagId);
            map.put("tagNameSnapshot", tagNameSnapshot);
            map.put("source", source);
            map.put("status", status);
            map.put("priority", priority);
            return map;
        }
    }

    public record SancaiQaPairSnapshot(Long id, String question, String answer, String source, Integer priority) {

        private static SancaiQaPairSnapshot from(ClassicsContentQaPair pair) {
            return new SancaiQaPairSnapshot(
                    SancaiEntryVersionSnapshot.id(pair.getId()),
                    pair.getQuestion(),
                    pair.getAnswer(),
                    value(pair.getSource()),
                    pair.getPriority());
        }

        public static SancaiQaPairSnapshot from(JsonNode snapshot) {
            return new SancaiQaPairSnapshot(
                    longValue(snapshot, "id"),
                    text(snapshot, "question"),
                    text(snapshot, "answer"),
                    text(snapshot, "source"),
                    intValue(snapshot, "priority"));
        }

        public static List<SancaiQaPairSnapshot> list(JsonNode snapshot, String fieldName) {
            JsonNode qaPairs = snapshot == null ? null : snapshot.get(fieldName);
            if (qaPairs == null || !qaPairs.isArray()) {
                return List.of();
            }
            List<SancaiQaPairSnapshot> result = new ArrayList<>(qaPairs.size());
            for (JsonNode node : qaPairs) {
                result.add(from(node));
            }
            return result;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("question", question);
            map.put("answer", answer);
            map.put("source", source);
            map.put("priority", priority);
            return map;
        }
    }
}
