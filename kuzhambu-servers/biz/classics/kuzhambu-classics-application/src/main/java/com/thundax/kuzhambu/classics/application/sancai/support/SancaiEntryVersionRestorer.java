package com.thundax.kuzhambu.classics.application.sancai.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.assembler.ClassicsContentApplicationAssembler;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsTagBindingSupport;
import com.thundax.kuzhambu.classics.application.content.support.SancaiEntryVersionSnapshot;
import com.thundax.kuzhambu.classics.application.sancai.assembler.SancaiApplicationFacadeAssembler;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.io.IOException;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class SancaiEntryVersionRestorer {

    private final SancaiRepository repository;
    private final ClassicsContentRepository contentRepository;
    private final ObjectMapper objectMapper;
    private final ClassicsTagBindingSupport tagBindingSupport;

    public SancaiEntryVersionRestorer(
            SancaiRepository repository,
            ClassicsContentRepository contentRepository,
            ObjectMapper objectMapper,
            ClassicsTagBindingSupport tagBindingSupport) {
        this.repository = repository;
        this.contentRepository = contentRepository;
        this.objectMapper = objectMapper;
        this.tagBindingSupport = tagBindingSupport;
    }

    public SancaiEntry restoreSnapshot(ClassicsContentVersion version) {
        validateVersion(version);
        JsonNode snapshot = readSnapshot(version.getSnapshotJson());
        validateSnapshot(version, snapshot);

        SancaiEntry current = repository.getByEntryId(
                SancaiEntryIdCodec.toDomain(version.getContentId().value()));
        if (current == null) {
            throw new BizException("三才图会条目不存在，无法恢复历史版本");
        }

        SancaiEntryVersionSnapshot parsedSnapshot = SancaiEntryVersionSnapshot.from(snapshot);
        SancaiEntry restored = toEntry(parsedSnapshot);
        restored.setId(current.getId());
        restored.setLifecycleStatus(current.getLifecycleStatus());
        restored.setTransitionStatus(current.getTransitionStatus());
        restored.setCurrentPublicationJobId(current.getCurrentPublicationJobId());
        restored.setPriority(repository.maxEntryPriority() + 1);
        restored.setContentUpdatedAt(Instant.now());
        updateRestoredEntryOrThrow(restored);
        restoreTags(restored, parsedSnapshot);
        restoreQaPairs(restored, parsedSnapshot);
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

    private static SancaiEntry toEntry(SancaiEntryVersionSnapshot snapshot) {
        SancaiEntry entry = new SancaiEntry();
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(snapshot == null ? null : snapshot.volumeId()));
        entry.setTitle(snapshot == null ? null : snapshot.title());
        entry.setOriginalText(snapshot == null ? null : snapshot.originalText());
        entry.setTranslationText(snapshot == null ? null : snapshot.translationText());
        entry.setSummary(snapshot == null ? null : snapshot.summary());
        entry.setTranslationStatus(
                enumValue(SancaiEntryTranslationStatus.class, snapshot == null ? null : snapshot.translationStatus()));
        entry.setImageStatus(enumValue(SancaiEntryImageStatus.class, snapshot == null ? null : snapshot.imageStatus()));
        entry.setVisualAssetStatus(
                enumValue(SancaiEntryVisualAssetStatus.class, snapshot == null ? null : snapshot.visualAssetStatus()));
        entry.setRefinementStatus(
                enumValue(SancaiEntryRefinementStatus.class, snapshot == null ? null : snapshot.refinementStatus()));
        return entry;
    }

    private void restoreTags(SancaiEntry entry, SancaiEntryVersionSnapshot snapshot) {
        if (contentRepository == null || entry == null || snapshot == null || entry.contentId() == null) {
            return;
        }
        contentRepository
                .listTags(ClassicsContentType.SANCAI_ENTRY.value(), entry.contentId(), SortDirection.ASC)
                .forEach(existingTag -> {
                    removeTagRefIfExists(existingTag);
                    contentRepository.deleteByTagId(
                            ClassicsContentType.SANCAI_ENTRY.value(), entry.contentId(), existingTag.getId());
                });
        int priority = contentRepository.maxTagPriority(null, null) + 1;
        for (SancaiEntryVersionSnapshot.SancaiTagSnapshot tagSnapshot : snapshot.tags()) {
            insertTagFromSnapshot(tagSnapshot, entry, priority++);
        }
    }

    private void restoreQaPairs(SancaiEntry entry, SancaiEntryVersionSnapshot snapshot) {
        if (contentRepository == null || entry == null || snapshot == null || entry.contentId() == null) {
            return;
        }
        contentRepository
                .listQaPairs(ClassicsContentType.SANCAI_ENTRY.value(), entry.contentId(), SortDirection.ASC)
                .forEach(pair -> contentRepository.deleteByQaPairId(pair.getId()));
        int priority = contentRepository.maxQaPairPriority() + 1;
        for (SancaiEntryVersionSnapshot.SancaiQaPairSnapshot qaSnapshot : snapshot.qaPairs()) {
            insertQaPairFromSnapshot(qaSnapshot, entry, priority++);
        }
    }

    private void insertTagFromSnapshot(
            SancaiEntryVersionSnapshot.SancaiTagSnapshot snapshot, SancaiEntry entry, int priority) {
        if (snapshot == null || (snapshot.tagId() == null && StringUtils.isBlank(snapshot.tagNameSnapshot()))) {
            return;
        }
        ContentTagCommand command = SancaiApplicationFacadeAssembler.toContentTagCommand(
                snapshot, entry.contentId().value());
        ClassicsContentTag tag;
        if (tagBindingSupport == null || snapshot.tagId() != null) {
            tag = ClassicsContentApplicationAssembler.toTag(command);
            tag.setPriority(priority);
        } else {
            tag = command.source() == ClassicsContentSource.AI
                    ? tagBindingSupport.bindAiTag(command, priority)
                    : tagBindingSupport.bindManualTag(command, priority);
        }
        tag.setId(null);
        contentRepository.insertTag(tag);
        if (tagBindingSupport != null) {
            tagBindingSupport.syncTagRef(tag);
        }
    }

    private void insertQaPairFromSnapshot(
            SancaiEntryVersionSnapshot.SancaiQaPairSnapshot snapshot, SancaiEntry entry, int priority) {
        if (snapshot == null || StringUtils.isBlank(snapshot.question()) || StringUtils.isBlank(snapshot.answer())) {
            return;
        }
        ContentQaPairCommand command = SancaiApplicationFacadeAssembler.toContentQaPairCommand(
                snapshot, entry.contentId().value());
        ClassicsContentQaPair qaPair = ClassicsContentApplicationAssembler.toQaPair(command);
        qaPair.setId(null);
        qaPair.setPriority(priority);
        contentRepository.insertQaPair(qaPair);
    }

    private void removeTagRefIfExists(ClassicsContentTag tag) {
        if (tagBindingSupport != null) {
            tagBindingSupport.removeTagRef(tag);
        }
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
