package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.id.Identifier;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassicsContentSnapshotAssembler {
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    public String toSnapshotJson(Versionable content) {
        return toJson(toSnapshot(content));
    }

    private Map<String, Object> toSnapshot(Versionable content) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("contentType", content.contentType().value());
        snapshot.put("contentId", id(content.contentId()));
        snapshot.put("contentUpdatedAt", date(content.contentUpdatedAt()));
        if (content.contentType() == ClassicsContentType.SANCAI_ENTRY) {
            appendSancai(snapshot, (SancaiEntry) content);
        } else if (content.contentType() == ClassicsContentType.WANGQI_DOCUMENT) {
            appendWangqi(snapshot, (WangqiDocument) content);
        } else if (content.contentType() == ClassicsContentType.MING_CUSTOMS) {
            appendMingCustoms(snapshot, (MingCustomsEntry) content);
        }
        return snapshot;
    }

    private void appendSancai(Map<String, Object> snapshot, SancaiEntry entry) {
        snapshot.put("volumeId", id(entry.getVolumeId()));
        snapshot.put("title", entry.getTitle());
        snapshot.put("originalText", entry.getOriginalText());
        snapshot.put("translationText", entry.getTranslationText());
        snapshot.put("summary", entry.getSummary());
        snapshot.put("lifecycleStatus", value(entry.getLifecycleStatus()));
        snapshot.put("visibility", value(entry.getVisibility()));
        snapshot.put("translationStatus", value(entry.getTranslationStatus()));
        snapshot.put("imageStatus", value(entry.getImageStatus()));
        snapshot.put("visualAssetStatus", value(entry.getVisualAssetStatus()));
        snapshot.put("refinementStatus", value(entry.getRefinementStatus()));
        snapshot.put("priority", entry.getPriority());
    }

    private void appendWangqi(Map<String, Object> snapshot, WangqiDocument document) {
        snapshot.put("title", document.getTitle());
        snapshot.put("summary", document.getSummary());
        snapshot.put("contentFormat", value(document.getContentFormat()));
        snapshot.put("content", document.getContent());
        snapshot.put("documentTime", date(document.getDocumentTime()));
        snapshot.put("storageObjectId", id(document.getStorageObjectId()));
        snapshot.put("visibility", value(document.getVisibility()));
    }

    private void appendMingCustoms(Map<String, Object> snapshot, MingCustomsEntry entry) {
        snapshot.put("title", entry.getTitle());
        snapshot.put("category", entry.getCategory());
        snapshot.put("chapter", entry.getChapter());
        snapshot.put("section", entry.getSection());
        snapshot.put("summary", entry.getSummary());
        snapshot.put("contentFormat", value(entry.getContentFormat()));
        snapshot.put("content", entry.getContent());
        snapshot.put("originalExcerpts", entry.getOriginalExcerpts());
        snapshot.put("visibility", value(entry.getVisibility()));
    }

    private static Long id(Identifier<Long> id) {
        return id == null ? null : id.value();
    }

    private static Long id(SancaiVolumeId id) {
        return id == null ? null : id.value();
    }

    private static Long id(StorageObjectId id) {
        return id == null ? null : id.value();
    }

    private static String date(Date date) {
        return date == null ? null : date.toInstant().toString();
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String toJson(Map<String, Object> snapshot) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append('"').append(':');
            appendJsonValue(builder, entry.getValue());
        }
        builder.append('}');
        return builder.toString();
    }

    private static void appendJsonValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else {
            builder.append('"').append(escape(String.valueOf(value))).append('"');
        }
    }

    private static String escape(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '"' || character == '\\') {
                builder.append('\\').append(character);
            } else if (character == '\b') {
                builder.append("\\b");
            } else if (character == '\f') {
                builder.append("\\f");
            } else if (character == '\n') {
                builder.append("\\n");
            } else if (character == '\r') {
                builder.append("\\r");
            } else if (character == '\t') {
                builder.append("\\t");
            } else if (character < 0x20) {
                builder.append("\\u");
                builder.append(HEX_DIGITS[(character >> 12) & 0x0F]);
                builder.append(HEX_DIGITS[(character >> 8) & 0x0F]);
                builder.append(HEX_DIGITS[(character >> 4) & 0x0F]);
                builder.append(HEX_DIGITS[character & 0x0F]);
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }
}
