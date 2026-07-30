package com.thundax.kuzhambu.classics.application.content.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.common.core.id.Identifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record MingCustomsVersionSnapshot(
        String contentType,
        Long contentId,
        String contentUpdatedAt,
        String title,
        String category,
        String chapter,
        String section,
        String summary,
        String contentFormat,
        String content,
        String originalExcerpts,
        String visibility,
        List<MingCustomsTagSnapshot> tags,
        List<MingCustomsQaPairSnapshot> qaPairs) {

    public static MingCustomsVersionSnapshot from(MingCustomsEntry entry) {
        return from(entry, List.of(), List.of());
    }

    public static MingCustomsVersionSnapshot from(
            MingCustomsEntry entry, List<ClassicsContentTag> tags, List<ClassicsContentQaPair> qaPairs) {
        return new MingCustomsVersionSnapshot(
                ClassicsContentType.MING_CUSTOMS.value(),
                identifier(entry.contentId()),
                date(entry.contentUpdatedAt()),
                entry.getTitle(),
                entry.getCategory(),
                entry.getChapter(),
                entry.getSection(),
                entry.getSummary(),
                value(entry.getContentFormat()),
                entry.getContent(),
                entry.getOriginalExcerpts(),
                value(entry.getVisibility()),
                tags == null
                        ? List.of()
                        : tags.stream().map(MingCustomsTagSnapshot::from).toList(),
                qaPairs == null
                        ? List.of()
                        : qaPairs.stream().map(MingCustomsQaPairSnapshot::from).toList());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("contentType", contentType);
        snapshot.put("contentId", contentId);
        snapshot.put("contentUpdatedAt", contentUpdatedAt);
        snapshot.put("title", title);
        snapshot.put("category", category);
        snapshot.put("chapter", chapter);
        snapshot.put("section", section);
        snapshot.put("summary", summary);
        snapshot.put("contentFormat", contentFormat);
        snapshot.put("content", content);
        snapshot.put("originalExcerpts", originalExcerpts);
        snapshot.put("visibility", visibility);
        snapshot.put(
                "tags",
                tags == null
                        ? List.of()
                        : tags.stream().map(MingCustomsTagSnapshot::toMap).toList());
        snapshot.put(
                "qaPairs",
                qaPairs == null
                        ? List.of()
                        : qaPairs.stream().map(MingCustomsQaPairSnapshot::toMap).toList());
        return snapshot;
    }

    public static MingCustomsVersionSnapshot from(JsonNode snapshot) {
        return new MingCustomsVersionSnapshot(
                text(snapshot, "contentType"),
                longValue(snapshot, "contentId"),
                text(snapshot, "contentUpdatedAt"),
                text(snapshot, "title"),
                text(snapshot, "category"),
                text(snapshot, "chapter"),
                text(snapshot, "section"),
                text(snapshot, "summary"),
                text(snapshot, "contentFormat"),
                text(snapshot, "content"),
                text(snapshot, "originalExcerpts"),
                text(snapshot, "visibility"),
                MingCustomsTagSnapshot.list(snapshot, "tags"),
                MingCustomsQaPairSnapshot.list(snapshot, "qaPairs"));
    }

    public record MingCustomsTagSnapshot(
            Long id, Long tagId, String tagNameSnapshot, String source, String status, Integer priority) {
        public static MingCustomsTagSnapshot from(ClassicsContentTag tag) {
            return new MingCustomsTagSnapshot(
                    identifier(tag == null ? null : tag.getId()),
                    tag == null || tag.getTagId() == null
                            ? null
                            : tag.getTagId().value(),
                    tag == null ? null : tag.getTagNameSnapshot(),
                    value(tag == null ? null : tag.getSource()),
                    value(tag == null ? null : tag.getStatus()),
                    tag == null ? null : tag.getPriority());
        }

        public static MingCustomsTagSnapshot from(JsonNode snapshot) {
            return new MingCustomsTagSnapshot(
                    longValue(snapshot, "id"),
                    longValue(snapshot, "tagId"),
                    text(snapshot, "tagNameSnapshot"),
                    text(snapshot, "source"),
                    text(snapshot, "status"),
                    intValue(snapshot, "priority"));
        }

        public static List<MingCustomsTagSnapshot> list(JsonNode snapshot, String fieldName) {
            JsonNode tags = snapshot == null ? null : snapshot.get(fieldName);
            if (tags == null || !tags.isArray()) {
                return List.of();
            }
            List<MingCustomsTagSnapshot> result = new ArrayList<>(tags.size());
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

    public record MingCustomsQaPairSnapshot(Long id, String question, String answer, String source, Integer priority) {
        public static MingCustomsQaPairSnapshot from(ClassicsContentQaPair pair) {
            return new MingCustomsQaPairSnapshot(
                    identifier(pair == null ? null : pair.getId()),
                    pair == null ? null : pair.getQuestion(),
                    pair == null ? null : pair.getAnswer(),
                    value(pair == null ? null : pair.getSource()),
                    pair == null ? null : pair.getPriority());
        }

        public static MingCustomsQaPairSnapshot from(JsonNode snapshot) {
            return new MingCustomsQaPairSnapshot(
                    longValue(snapshot, "id"),
                    text(snapshot, "question"),
                    text(snapshot, "answer"),
                    text(snapshot, "source"),
                    intValue(snapshot, "priority"));
        }

        public static List<MingCustomsQaPairSnapshot> list(JsonNode snapshot, String fieldName) {
            JsonNode qaPairs = snapshot == null ? null : snapshot.get(fieldName);
            if (qaPairs == null || !qaPairs.isArray()) {
                return List.of();
            }
            List<MingCustomsQaPairSnapshot> result = new ArrayList<>(qaPairs.size());
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
}
