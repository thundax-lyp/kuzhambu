package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import java.util.List;
import java.util.Map;

public class ClassicsContentSnapshotAssembler {
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    public String toSnapshotJson(Versionable content) {
        return toJson(toSnapshot(content, List.of(), List.of()));
    }

    public String toSnapshotJson(
            Versionable content, List<ClassicsContentTag> tags, List<ClassicsContentQaPair> qaPairs) {
        return toJson(toSnapshot(content, tags, qaPairs));
    }

    public String toSnapshotJson(SancaiEntry entry, List<SancaiEntryImage> images) {
        return toJson(SancaiEntryVersionSnapshot.from(entry, images).toMap());
    }

    public String toSnapshotJsonWithImageResources(
            SancaiEntry entry, List<SancaiEntryVersionSnapshot.ImageResource> images) {
        return toJson(
                SancaiEntryVersionSnapshot.fromImageResources(entry, images).toMap());
    }

    public String toSancaiSnapshotJson(
            SancaiEntry entry,
            String volumeTitle,
            Long categoryId,
            String categoryTitle,
            List<SancaiEntryVersionSnapshot.ImageResource> images,
            List<ClassicsContentTag> tags,
            List<ClassicsContentQaPair> qaPairs) {
        return toJson(SancaiEntryVersionSnapshot.fromImageResources(
                        entry, volumeTitle, categoryId, categoryTitle, images, tags, qaPairs)
                .toMap());
    }

    private Map<String, Object> toSnapshot(
            Versionable content, List<ClassicsContentTag> tags, List<ClassicsContentQaPair> qaPairs) {
        if (content.contentType() == ClassicsContentType.SANCAI_ENTRY) {
            return SancaiEntryVersionSnapshot.from((SancaiEntry) content).toMap();
        }
        if (content.contentType() == ClassicsContentType.WANGQI_DOCUMENT) {
            return WangqiDocumentVersionSnapshot.from((WangqiDocument) content, tags, qaPairs)
                    .toMap();
        }
        if (content.contentType() == ClassicsContentType.MING_CUSTOMS) {
            return MingCustomsVersionSnapshot.from((MingCustomsEntry) content, tags, qaPairs)
                    .toMap();
        }
        throw new IllegalArgumentException("Unsupported classics content type: " + content.contentType());
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
        } else if (value instanceof Map<?, ?> map) {
            appendMap(builder, map);
        } else if (value instanceof Iterable<?> iterable) {
            appendIterable(builder, iterable);
        } else {
            builder.append('"').append(escape(String.valueOf(value))).append('"');
        }
    }

    private static void appendMap(StringBuilder builder, Map<?, ?> map) {
        builder.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"')
                    .append(escape(String.valueOf(entry.getKey())))
                    .append('"')
                    .append(':');
            appendJsonValue(builder, entry.getValue());
        }
        builder.append('}');
    }

    private static void appendIterable(StringBuilder builder, Iterable<?> iterable) {
        builder.append('[');
        boolean first = true;
        for (Object item : iterable) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            appendJsonValue(builder, item);
        }
        builder.append(']');
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
