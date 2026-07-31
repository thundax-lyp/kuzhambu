package com.thundax.kuzhambu.classics.application.publication.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentSnapshotAssembler;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationFragment;
import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationPayload;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationJob;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationPrepareFacadeRequest;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ClassicsPublicationPayloadAssembler {
    private static final int COLLECTION_TITLE_LIMIT = 80;

    private final ClassicsContentSnapshotAssembler snapshotAssembler;

    public ClassicsPublicationPayloadAssembler() {
        this.snapshotAssembler = new ClassicsContentSnapshotAssembler();
    }

    public ClassicsPublicationPayload assemble(ClassicsPublicationJob job, ClassicsContentVersion version) {
        validateVersionIdentity(job, version);
        JsonNode snapshot = snapshotAssembler.fromSnapshotJson(version.getSnapshotJson());
        validateSnapshotIdentity(job, snapshot);

        String title = requiredText(snapshot, "title");
        List<FieldValue> bodyFields = bodyFields(job.getContentType(), snapshot);
        List<QaValue> qaPairs = qaPairs(snapshot);
        List<String> tags = tagNames(snapshot);
        List<ClassicsPublicationFragment> fragments = fragments(title, bodyFields, tags, qaPairs);
        if (fragments.isEmpty()) {
            throw invalid("Snapshot has no publishable body or QA fragment");
        }

        List<String> textSegments = new ArrayList<>();
        addText(textSegments, title);
        bodyFields.forEach(field -> addText(textSegments, field.value()));
        qaPairs.forEach(pair -> {
            addText(textSegments, pair.question());
            addText(textSegments, pair.answer());
        });

        String sourceId = job.getContentType().name() + ":" + job.getContentId();
        DiscoverySearchPublicationPrepareFacadeRequest searchDocument =
                DiscoverySearchPublicationPrepareFacadeRequest.builder()
                        .sourceId(sourceId)
                        .contentType(job.getContentType().name())
                        .contentId(String.valueOf(job.getContentId()))
                        .contentVersionId(String.valueOf(job.getContentVersionId()))
                        .contentVersionNo(job.getContentVersionNo())
                        .title(title)
                        .summary(text(snapshot, "summary"))
                        .categoryId(categoryId(job.getContentType(), snapshot))
                        .categoryName(categoryName(job.getContentType(), snapshot))
                        .volumeId(idText(snapshot, "volumeId"))
                        .volumeTitle(text(snapshot, "volumeTitle"))
                        .textSegments(List.copyOf(textSegments))
                        .tagNames(tags)
                        .contentUpdatedAt(instant(snapshot, "contentUpdatedAt"))
                        .build();
        return new ClassicsPublicationPayload(
                searchDocument,
                collectionName(job.getContentType(), job.getContentId(), title),
                List.copyOf(fragments));
    }

    private static void validateVersionIdentity(ClassicsPublicationJob job, ClassicsContentVersion version) {
        if (job == null
                || version == null
                || job.getContentVersionId() == null
                || job.getContentVersionNo() == null
                || version.getId() == null
                || version.getContentType() != job.getContentType()
                || version.getContentId() == null
                || !Objects.equals(version.getId().value(), job.getContentVersionId())
                || !Objects.equals(version.getContentId().value(), job.getContentId())
                || version.getVersionNo() != job.getContentVersionNo()) {
            throw invalid("Job and formal version identity do not match");
        }
    }

    private static void validateSnapshotIdentity(ClassicsPublicationJob job, JsonNode snapshot) {
        if (!job.getContentType().name().equals(text(snapshot, "contentType"))
                || !Objects.equals(job.getContentId(), longValue(snapshot, "contentId"))) {
            throw invalid("Snapshot and job content identity do not match");
        }
    }

    private static List<FieldValue> bodyFields(ClassicsContentType contentType, JsonNode snapshot) {
        List<FieldValue> fields = new ArrayList<>();
        switch (contentType) {
            case SANCAI_ENTRY -> {
                addField(fields, "门类", snapshot, "categoryTitle");
                addField(fields, "卷册", snapshot, "volumeTitle");
                addField(fields, "原文", snapshot, "originalText");
                addField(fields, "译文", snapshot, "translationText");
                addField(fields, "摘要", snapshot, "summary");
            }
            case WANGQI_DOCUMENT -> {
                addField(fields, "摘要", snapshot, "summary");
                addField(fields, "正文", snapshot, "content");
            }
            case MING_CUSTOMS -> {
                addField(fields, "分类", snapshot, "category");
                addField(fields, "章", snapshot, "chapter");
                addField(fields, "节", snapshot, "section");
                addField(fields, "摘要", snapshot, "summary");
                addField(fields, "正文", snapshot, "content");
                addField(fields, "原文摘录", snapshot, "originalExcerpts");
            }
        }
        return fields;
    }

    private static List<QaValue> qaPairs(JsonNode snapshot) {
        JsonNode pairs = snapshot.get("qaPairs");
        if (pairs == null || !pairs.isArray()) {
            return List.of();
        }
        List<QaValue> result = new ArrayList<>();
        for (JsonNode pair : pairs) {
            String question = text(pair, "question");
            String answer = text(pair, "answer");
            if (question != null && answer != null) {
                result.add(new QaValue(question, answer));
            }
        }
        return result;
    }

    private static List<String> tagNames(JsonNode snapshot) {
        JsonNode tags = snapshot.get("tags");
        if (tags == null || !tags.isArray()) {
            return List.of();
        }
        Set<String> names = new LinkedHashSet<>();
        for (JsonNode tag : tags) {
            String status = text(tag, "status");
            String name = text(tag, "tagNameSnapshot");
            if ("ACTIVE".equals(status) && name != null) {
                names.add(name);
            }
        }
        return List.copyOf(names);
    }

    private static List<ClassicsPublicationFragment> fragments(
            String title, List<FieldValue> bodyFields, List<String> tags, List<QaValue> qaPairs) {
        List<ClassicsPublicationFragment> fragments = new ArrayList<>();
        List<String> mainLines = new ArrayList<>();
        bodyFields.forEach(field -> mainLines.add(field.label() + "：" + field.value()));
        if (!tags.isEmpty()) {
            mainLines.add("标签：" + String.join("、", tags));
        }
        if (!mainLines.isEmpty()) {
            fragments.add(new ClassicsPublicationFragment(title, String.join("\n", mainLines), 0));
        }
        for (int index = 0; index < qaPairs.size(); index++) {
            QaValue pair = qaPairs.get(index);
            fragments.add(new ClassicsPublicationFragment(pair.question(), pair.answer(), index + 1));
        }
        return fragments;
    }

    private static String collectionName(ClassicsContentType contentType, Long contentId, String title) {
        String sanitized = title.replace("\r", "").replace("\n", "").trim();
        if (sanitized.isEmpty()) {
            sanitized = "untitled";
        }
        int codePoints = sanitized.codePointCount(0, sanitized.length());
        if (codePoints > COLLECTION_TITLE_LIMIT) {
            sanitized = sanitized.substring(0, sanitized.offsetByCodePoints(0, COLLECTION_TITLE_LIMIT));
        }
        return contentType.name() + ":" + contentId + ":" + sanitized;
    }

    private static String categoryId(ClassicsContentType contentType, JsonNode snapshot) {
        return contentType == ClassicsContentType.SANCAI_ENTRY ? idText(snapshot, "categoryId") : null;
    }

    private static String categoryName(ClassicsContentType contentType, JsonNode snapshot) {
        return switch (contentType) {
            case SANCAI_ENTRY -> text(snapshot, "categoryTitle");
            case MING_CUSTOMS -> text(snapshot, "category");
            case WANGQI_DOCUMENT -> null;
        };
    }

    private static String requiredText(JsonNode snapshot, String fieldName) {
        String value = text(snapshot, fieldName);
        if (value == null) {
            throw invalid("Snapshot title must not be blank");
        }
        return value;
    }

    private static String text(JsonNode snapshot, String fieldName) {
        JsonNode value = snapshot == null ? null : snapshot.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private static Long longValue(JsonNode snapshot, String fieldName) {
        JsonNode value = snapshot == null ? null : snapshot.get(fieldName);
        return value == null || !value.canConvertToLong() ? null : value.asLong();
    }

    private static String idText(JsonNode snapshot, String fieldName) {
        Long value = longValue(snapshot, fieldName);
        return value == null ? null : String.valueOf(value);
    }

    private static Instant instant(JsonNode snapshot, String fieldName) {
        String value = text(snapshot, fieldName);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeException exception) {
            throw invalid("Snapshot " + fieldName + " is invalid");
        }
    }

    private static void addField(List<FieldValue> fields, String label, JsonNode snapshot, String fieldName) {
        String value = text(snapshot, fieldName);
        if (value != null) {
            fields.add(new FieldValue(label, value));
        }
    }

    private static void addText(List<String> target, String value) {
        if (value != null) {
            target.add(value);
        }
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }

    private record FieldValue(String label, String value) {}

    private record QaValue(String question, String answer) {}
}
