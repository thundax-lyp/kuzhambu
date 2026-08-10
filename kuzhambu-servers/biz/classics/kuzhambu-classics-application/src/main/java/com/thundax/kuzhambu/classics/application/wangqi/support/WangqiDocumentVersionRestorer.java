package com.thundax.kuzhambu.classics.application.wangqi.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.application.content.assembler.ClassicsContentApplicationAssembler;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsTagBindingSupport;
import com.thundax.kuzhambu.classics.application.content.support.WangqiDocumentVersionSnapshot;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.repository.WangqiDocumentRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.io.IOException;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class WangqiDocumentVersionRestorer {

    private final WangqiDocumentRepository repository;
    private final ClassicsContentRepository contentRepository;
    private final ObjectMapper objectMapper;
    private final ClassicsTagBindingSupport tagBindingSupport;

    public WangqiDocumentVersionRestorer(
            WangqiDocumentRepository repository,
            ClassicsContentRepository contentRepository,
            ObjectMapper objectMapper,
            ClassicsTagBindingSupport tagBindingSupport) {
        this.repository = repository;
        this.contentRepository = contentRepository;
        this.objectMapper = objectMapper;
        this.tagBindingSupport = tagBindingSupport;
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

        WangqiDocumentVersionSnapshot parsedSnapshot = WangqiDocumentVersionSnapshot.from(snapshot);
        WangqiDocument restored = toDocument(parsedSnapshot);
        restored.setId(current.getId());
        restored.setLifecycleStatus(current.getLifecycleStatus());
        restored.setTransitionStatus(current.getTransitionStatus());
        restored.setCurrentPublicationJobId(current.getCurrentPublicationJobId());
        restored.setContentUpdatedAt(Instant.now());
        restoreTags(restored, parsedSnapshot);
        restoreQaPairs(restored, parsedSnapshot);
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

    private static WangqiDocument toDocument(WangqiDocumentVersionSnapshot snapshot) {
        WangqiDocument document = new WangqiDocument();
        document.setTitle(snapshot == null ? null : snapshot.title());
        document.setSummary(snapshot == null ? null : snapshot.summary());
        document.setContentFormat(
                snapshot == null || snapshot.contentFormat() == null
                        ? null
                        : enumValue(WangqiContentFormat.class, snapshot.contentFormat()));
        document.setContent(snapshot == null ? null : snapshot.content());
        document.setDocumentTime(date(snapshot == null ? null : snapshot.documentTime()));
        document.setStorageObjectId(
                StorageObjectIdCodec.toDomain(snapshot == null ? null : snapshot.storageObjectId()));
        return document;
    }

    private void restoreTags(WangqiDocument document, WangqiDocumentVersionSnapshot snapshot) {
        if (document == null || snapshot == null || document.contentId() == null) {
            return;
        }
        contentRepository
                .listTags(ClassicsContentType.WANGQI_DOCUMENT.value(), document.contentId(), SortDirection.ASC)
                .forEach(existingTag -> {
                    removeTagRefIfExists(existingTag);
                    contentRepository.deleteByTagId(
                            ClassicsContentType.WANGQI_DOCUMENT.value(), document.contentId(), existingTag.getId());
                });

        if (snapshot.tags() == null) {
            return;
        }
        int priority = contentRepository.maxTagPriority(null, null) + 1;
        for (int i = 0; i < snapshot.tags().size(); i++) {
            insertTagFromSnapshot(snapshot.tags().get(i), document, priority++);
        }
    }

    private void restoreQaPairs(WangqiDocument document, WangqiDocumentVersionSnapshot snapshot) {
        if (document == null || document.contentId() == null) {
            return;
        }
        contentRepository
                .listQaPairs(ClassicsContentType.WANGQI_DOCUMENT.value(), document.contentId(), SortDirection.ASC)
                .forEach(pair -> contentRepository.deleteByQaPairId(pair.getId()));

        if (snapshot == null || snapshot.qaPairs() == null) {
            return;
        }
        int priority = contentRepository.maxQaPairPriority() + 1;
        for (int i = 0; i < snapshot.qaPairs().size(); i++) {
            insertQaPairFromSnapshot(snapshot.qaPairs().get(i), document, priority++);
        }
    }

    private void insertTagFromSnapshot(
            WangqiDocumentVersionSnapshot.WangqiTagSnapshot snapshot, WangqiDocument document, int fallbackPriority) {
        if (snapshot == null || document == null || document.contentId() == null) {
            return;
        }
        int priority = fallbackPriority;
        if (tagBindingSupport == null) {
            ContentTagCommand command = new ContentTagCommand(
                    null,
                    ClassicsContentType.WANGQI_DOCUMENT,
                    document.contentId().value(),
                    snapshot.tagId(),
                    snapshot.tagNameSnapshot(),
                    parseSource(snapshot.source()),
                    parseTagStatus(snapshot.status()));
            ClassicsContentTag tag = ClassicsContentApplicationAssembler.toTag(command);
            tag.setPriority(priority);
            tag.setId(null);
            contentRepository.insertTag(tag);
            return;
        }

        if (snapshot.tagId() == null && StringUtils.isBlank(snapshot.tagNameSnapshot())) {
            return;
        }

        ClassicsContentTag tag = commandForRestoredTag(document, snapshot, priority);
        if (tag == null) {
            return;
        }
        contentRepository.insertTag(tag);
        tagBindingSupport.syncTagRef(tag);
    }

    private ClassicsContentTag commandForRestoredTag(
            WangqiDocument document, WangqiDocumentVersionSnapshot.WangqiTagSnapshot snapshot, int priority) {
        ClassicsContentSource source = parseSource(snapshot.source());
        ClassicsContentTagStatus status = parseTagStatus(snapshot.status());
        ContentTagCommand command = new ContentTagCommand(
                null,
                ClassicsContentType.WANGQI_DOCUMENT,
                document.contentId().value(),
                snapshot.tagId(),
                snapshot.tagNameSnapshot(),
                source,
                status);
        if (snapshot.tagId() == null) {
            return source == ClassicsContentSource.AI
                    ? tagBindingSupport.bindAiTag(command, priority)
                    : tagBindingSupport.bindManualTag(command, priority);
        }

        ClassicsContentTag tag = ClassicsContentApplicationAssembler.toTag(command);
        tag.setId(null);
        tag.setPriority(priority);
        return tag;
    }

    private void insertQaPairFromSnapshot(
            WangqiDocumentVersionSnapshot.WangqiQaPairSnapshot snapshot,
            WangqiDocument document,
            int fallbackPriority) {
        if (snapshot == null || document == null || document.contentId() == null) {
            return;
        }
        if (StringUtils.isBlank(snapshot.question()) && StringUtils.isBlank(snapshot.answer())) {
            return;
        }

        ContentQaPairCommand command = new ContentQaPairCommand(
                null,
                ClassicsContentType.WANGQI_DOCUMENT,
                document.contentId().value(),
                snapshot.question(),
                snapshot.answer(),
                parseSource(snapshot.source()));
        ClassicsContentQaPair qaPair = ClassicsContentApplicationAssembler.toQaPair(command);
        qaPair.setId(null);
        qaPair.setPriority(fallbackPriority);
        contentRepository.insertQaPair(qaPair);
    }

    private void removeTagRefIfExists(ClassicsContentTag tag) {
        if (tagBindingSupport == null) {
            return;
        }
        tagBindingSupport.removeTagRef(tag);
    }

    private static ClassicsContentSource parseSource(String value) {
        return StringUtils.isBlank(value) ? ClassicsContentSource.MANUAL : ClassicsContentSource.valueOf(value);
    }

    private static ClassicsContentTagStatus parseTagStatus(String value) {
        return StringUtils.isBlank(value) ? ClassicsContentTagStatus.ACTIVE : ClassicsContentTagStatus.valueOf(value);
    }

    private static Instant date(String value) {
        return StringUtils.isBlank(value) ? null : Instant.parse(value);
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
