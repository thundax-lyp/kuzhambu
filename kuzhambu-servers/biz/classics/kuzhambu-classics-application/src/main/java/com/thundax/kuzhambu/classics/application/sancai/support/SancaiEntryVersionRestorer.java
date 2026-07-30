package com.thundax.kuzhambu.classics.application.sancai.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.io.IOException;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SancaiEntryVersionRestorer {

    private final SancaiRepository repository;
    private final ObjectMapper objectMapper;

    public SancaiEntryVersionRestorer(SancaiRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public SancaiEntry restoreSnapshot(ClassicsContentVersion version) {
        validateVersion(version);
        JsonNode snapshot = readSnapshot(version.getSnapshotJson());
        validateSnapshot(version, snapshot);

        SancaiEntry current = repository.getEntryById(
                SancaiEntryIdCodec.toDomain(version.getContentId().value()));
        if (current == null) {
            throw new BizException("三才图会条目不存在，无法恢复历史版本");
        }

        SancaiEntry restored = toEntry(snapshot);
        restored.setId(current.getId());
        restored.setPriority(repository.maxEntryPriority() + 1);
        restored.setContentUpdatedAt(Instant.now());
        updateRestoredEntryOrThrow(restored);
        return restored;
    }

    public void markVersioned(SancaiEntry entry) {
        updateRestoredEntryOrThrow(entry);
    }

    private void updateRestoredEntryOrThrow(SancaiEntry entry) {
        if (repository.updateRestoredEntry(entry) != 1) {
            throw new BizException("三才图会条目不存在，无法恢复历史版本");
        }
    }

    private static void validateVersion(ClassicsContentVersion version) {
        if (version == null || version.getContentType() != ClassicsContentType.SANCAI_ENTRY) {
            throw new BizException("历史版本不是三才图会条目版本");
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
        if (!ClassicsContentType.SANCAI_ENTRY.value().equals(text(snapshot, "contentType"))
                || !version.getContentId().equals(ClassicsContentIdCodec.toDomain(longValue(snapshot, "contentId")))) {
            throw new BizException("历史版本快照不属于当前三才图会条目");
        }
    }

    private static SancaiEntry toEntry(JsonNode snapshot) {
        SancaiEntry entry = new SancaiEntry();
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(longValue(snapshot, "volumeId")));
        entry.setTitle(text(snapshot, "title"));
        entry.setOriginalText(text(snapshot, "originalText"));
        entry.setTranslationText(text(snapshot, "translationText"));
        entry.setSummary(text(snapshot, "summary"));
        entry.setLifecycleStatus(enumValue(SancaiEntryLifecycleStatus.class, text(snapshot, "lifecycleStatus")));
        entry.setVisibility(enumValue(SancaiEntryVisibility.class, text(snapshot, "visibility")));
        entry.setTranslationStatus(enumValue(SancaiEntryTranslationStatus.class, text(snapshot, "translationStatus")));
        entry.setImageStatus(enumValue(SancaiEntryImageStatus.class, text(snapshot, "imageStatus")));
        entry.setVisualAssetStatus(enumValue(SancaiEntryVisualAssetStatus.class, text(snapshot, "visualAssetStatus")));
        entry.setRefinementStatus(enumValue(SancaiEntryRefinementStatus.class, text(snapshot, "refinementStatus")));
        return entry;
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
