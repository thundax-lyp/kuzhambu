package com.thundax.kuzhambu.classics.application.content.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.id.Identifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record WangqiDocumentVersionSnapshot(
        String contentType,
        Long contentId,
        String contentUpdatedAt,
        String title,
        String summary,
        String contentFormat,
        String content,
        String documentTime,
        Long storageObjectId,
        String visibility,
        List<WangqiTagSnapshot> tags,
        List<WangqiQaPairSnapshot> qaPairs) {

    public static WangqiDocumentVersionSnapshot from(WangqiDocument document) {
        return from(document, List.of(), List.of());
    }

    public static WangqiDocumentVersionSnapshot from(
            WangqiDocument document, List<ClassicsContentTag> tags, List<ClassicsContentQaPair> qaPairs) {
        return new WangqiDocumentVersionSnapshot(
                ClassicsContentType.WANGQI_DOCUMENT.value(),
                identifier(document.contentId()),
                date(document.contentUpdatedAt()),
                document.getTitle(),
                document.getSummary(),
                value(document.getContentFormat()),
                document.getContent(),
                date(document.getDocumentTime()),
                identifier(document.getStorageObjectId()),
                value(document.getVisibility()),
                tags == null
                        ? List.of()
                        : tags.stream().map(WangqiTagSnapshot::from).toList(),
                qaPairs == null
                        ? List.of()
                        : qaPairs.stream().map(WangqiQaPairSnapshot::from).toList());
    }

    public static WangqiDocumentVersionSnapshot from(JsonNode snapshot) {
        return new WangqiDocumentVersionSnapshot(
                text(snapshot, "contentType"),
                longValue(snapshot, "contentId"),
                text(snapshot, "contentUpdatedAt"),
                text(snapshot, "title"),
                text(snapshot, "summary"),
                text(snapshot, "contentFormat"),
                text(snapshot, "content"),
                text(snapshot, "documentTime"),
                longValue(snapshot, "storageObjectId"),
                text(snapshot, "visibility"),
                WangqiTagSnapshot.list(snapshot, "tags"),
                WangqiQaPairSnapshot.list(snapshot, "qaPairs"));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("contentType", contentType);
        snapshot.put("contentId", contentId);
        snapshot.put("contentUpdatedAt", contentUpdatedAt);
        snapshot.put("title", title);
        snapshot.put("summary", summary);
        snapshot.put("contentFormat", contentFormat);
        snapshot.put("content", content);
        snapshot.put("documentTime", documentTime);
        snapshot.put("storageObjectId", storageObjectId);
        snapshot.put("visibility", visibility);
        snapshot.put(
                "tags",
                tags == null
                        ? List.of()
                        : tags.stream().map(WangqiTagSnapshot::toMap).toList());
        snapshot.put(
                "qaPairs",
                qaPairs == null
                        ? List.of()
                        : qaPairs.stream().map(WangqiQaPairSnapshot::toMap).toList());
        return snapshot;
    }

    public record WangqiTagSnapshot(
            Long id, Long tagId, String tagNameSnapshot, String source, String status, Integer priority) {

        public static WangqiTagSnapshot from(ClassicsContentTag tag) {
            return new WangqiTagSnapshot(
                    identifier(tag == null ? null : tag.getId()),
                    tag == null || tag.getTagId() == null
                            ? null
                            : tag.getTagId().value(),
                    tag == null ? null : tag.getTagNameSnapshot(),
                    value(tag == null ? null : tag.getSource()),
                    value(tag == null ? null : tag.getStatus()),
                    tag == null ? null : tag.getPriority());
        }

        public static WangqiTagSnapshot from(JsonNode snapshot) {
            return new WangqiTagSnapshot(
                    longValue(snapshot, "id"),
                    longValue(snapshot, "tagId"),
                    text(snapshot, "tagNameSnapshot"),
                    text(snapshot, "source"),
                    text(snapshot, "status"),
                    intValue(snapshot, "priority"));
        }

        public static List<WangqiTagSnapshot> list(JsonNode snapshot, String fieldName) {
            JsonNode tags = snapshot == null ? null : snapshot.get(fieldName);
            if (tags == null || !tags.isArray()) {
                return List.of();
            }
            List<WangqiTagSnapshot> result = new ArrayList<>(tags.size());
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

    public record WangqiQaPairSnapshot(Long id, String question, String answer, String source, Integer priority) {
        public static WangqiQaPairSnapshot from(ClassicsContentQaPair pair) {
            return new WangqiQaPairSnapshot(
                    identifier(pair == null ? null : pair.getId()),
                    pair == null ? null : pair.getQuestion(),
                    pair == null ? null : pair.getAnswer(),
                    value(pair == null ? null : pair.getSource()),
                    pair == null ? null : pair.getPriority());
        }

        public static WangqiQaPairSnapshot from(JsonNode snapshot) {
            return new WangqiQaPairSnapshot(
                    longValue(snapshot, "id"),
                    text(snapshot, "question"),
                    text(snapshot, "answer"),
                    text(snapshot, "source"),
                    intValue(snapshot, "priority"));
        }

        public static List<WangqiQaPairSnapshot> list(JsonNode snapshot, String fieldName) {
            JsonNode qaPairs = snapshot == null ? null : snapshot.get(fieldName);
            if (qaPairs == null || !qaPairs.isArray()) {
                return List.of();
            }
            List<WangqiQaPairSnapshot> result = new ArrayList<>(qaPairs.size());
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

    private static Long identifier(Identifier<Long> id) {
        return id == null ? null : id.value();
    }

    private static String date(Date date) {
        return date == null ? null : date.toInstant().toString();
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
}
