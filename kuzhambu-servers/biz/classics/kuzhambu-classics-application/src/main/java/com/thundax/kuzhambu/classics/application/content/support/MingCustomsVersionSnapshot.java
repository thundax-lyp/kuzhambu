package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.common.core.id.Identifier;
import java.util.Date;
import java.util.LinkedHashMap;
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
        String visibility) {

    public static MingCustomsVersionSnapshot from(MingCustomsEntry entry) {
        return new MingCustomsVersionSnapshot(
                ClassicsContentType.MING_CUSTOMS.value(),
                id(entry.contentId()),
                date(entry.contentUpdatedAt()),
                entry.getTitle(),
                entry.getCategory(),
                entry.getChapter(),
                entry.getSection(),
                entry.getSummary(),
                value(entry.getContentFormat()),
                entry.getContent(),
                entry.getOriginalExcerpts(),
                value(entry.getVisibility()));
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
