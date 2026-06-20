package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.id.Identifier;
import java.util.Date;
import java.util.LinkedHashMap;
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
        String visibility) {

    public static WangqiDocumentVersionSnapshot from(WangqiDocument document) {
        return new WangqiDocumentVersionSnapshot(
                ClassicsContentType.WANGQI_DOCUMENT.value(),
                id(document.contentId()),
                date(document.contentUpdatedAt()),
                document.getTitle(),
                document.getSummary(),
                value(document.getContentFormat()),
                document.getContent(),
                date(document.getDocumentTime()),
                id(document.getStorageObjectId()),
                value(document.getVisibility()));
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
