package com.thundax.kuzhambu.classics.application.wangqi.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class WangqiDocumentVersionRestorer {

    private final WangqiDocumentRepository repository;
    private final ObjectMapper objectMapper;

    public WangqiDocumentVersionRestorer(WangqiDocumentRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public WangqiDocument restoreSnapshot(ClassicsContentVersion version) {
        validateVersion(version);
        JsonNode snapshot = readSnapshot(version.getSnapshotJson());
        validateSnapshot(version, snapshot);

        WangqiDocument current = repository.getById(
                WangqiDocumentIdCodec.toDomain(version.getContentId().value()));
        if (current == null) {
            throw new BizException("王圻文档不存在，无法恢复历史版本");
        }

        WangqiDocument restored = toDocument(snapshot);
        restored.setId(current.getId());
        restored.setContentUpdatedAt(new Date());
        repository.updateRestoredVersion(restored);
        return restored;
    }

    public void markVersioned(WangqiDocument document) {
        repository.updateRestoredVersion(document);
    }

    private static void validateVersion(ClassicsContentVersion version) {
        if (version == null || version.getContentType() != ClassicsContentType.WANGQI_DOCUMENT) {
            throw new BizException("历史版本不是王圻文档版本");
        }
    }

    private JsonNode readSnapshot(String snapshotJson) {
        try {
            return objectMapper.readTree(snapshotJson);
        } catch (IOException exception) {
            throw new BizException(
                    "CLASSICS-13005", "classics.content.version.snapshot-invalid", "历史版本快照不可解析", exception);
        }
    }

    private static void validateSnapshot(ClassicsContentVersion version, JsonNode snapshot) {
        if (!ClassicsContentType.WANGQI_DOCUMENT.value().equals(text(snapshot, "contentType"))
                || !version.getContentId().equals(ClassicsContentIdCodec.toDomain(longValue(snapshot, "contentId")))) {
            throw new BizException("历史版本快照不属于当前王圻文档");
        }
    }

    private static WangqiDocument toDocument(JsonNode snapshot) {
        WangqiDocument document = new WangqiDocument();
        document.setTitle(text(snapshot, "title"));
        document.setSummary(text(snapshot, "summary"));
        document.setContentFormat(enumValue(WangqiContentFormat.class, text(snapshot, "contentFormat")));
        document.setContent(text(snapshot, "content"));
        document.setDocumentTime(date(snapshot, "documentTime"));
        document.setStorageObjectId(StorageObjectIdCodec.toDomain(longValue(snapshot, "storageObjectId")));
        document.setVisibility(enumValue(WangqiDocumentVisibility.class, text(snapshot, "visibility")));
        return document;
    }

    private static Date date(JsonNode snapshot, String fieldName) {
        String value = text(snapshot, fieldName);
        return StringUtils.isBlank(value) ? null : Date.from(Instant.parse(value));
    }

    private static Long longValue(JsonNode snapshot, String fieldName) {
        JsonNode value = snapshot == null ? null : snapshot.get(fieldName);
        return value == null || value.isNull() ? null : value.asLong();
    }

    private static String text(JsonNode snapshot, String fieldName) {
        JsonNode value = snapshot == null ? null : snapshot.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value) {
        return StringUtils.isBlank(value) ? null : Enum.valueOf(type, value);
    }
}
