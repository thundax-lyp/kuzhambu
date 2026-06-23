package com.thundax.kuzhambu.classics.application.content.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateApplyCheck;
import com.thundax.kuzhambu.ai.domain.invocation.service.AiCandidateDomainService;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentExportCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.result.AiCandidateApplyContentResult;
import com.thundax.kuzhambu.classics.application.content.result.ClassicsExportJobResult;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.content.support.AiCandidateQaPairPayload;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsAiCandidatePayloadParser;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentSnapshotAssembler;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsTagBindingSupport;
import com.thundax.kuzhambu.classics.application.content.support.SancaiEntryVersionSnapshot;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.support.SancaiEntryVersionRestorer;
import com.thundax.kuzhambu.classics.application.wangqi.support.WangqiDocumentVersionRestorer;
import com.thundax.kuzhambu.classics.domain.common.client.WorkerRenderClient;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.content.service.ClassicsContentVersioningService;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class ClassicsContentApplicationServiceImpl implements ClassicsContentApplicationService {

    private static final int EXPORT_EXPIRES_DAYS = 7;
    private static final String DEFAULT_EXPORT_TEMPLATE_ID = "classics-export-default";
    private static final String DEFAULT_EXPORT_TEMPLATE_VERSION = "2026.06.01";
    private static final String DEFAULT_RENDER_OPERATION = "CLASSICS_EXPORT";
    private static final String DEFAULT_RENDER_TYPE = "CLASSICS_EXPORT";
    private static final String DEFAULT_TITLE = "classics-export";

    private final ClassicsContentRepository repository;
    private final WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer;
    private final SancaiEntryVersionRestorer sancaiEntryVersionRestorer;
    private final SancaiAssetApplicationService sancaiAssetApplicationService;
    private final StorageApplicationService storageApplicationService;
    private final WorkerRenderClient workerRenderClient;
    private final StorageUploadStreamHelper storageUploadStreamHelper;
    private final ObjectMapper objectMapper;
    private final ClassicsContentVersioningService versioningService = new ClassicsContentVersioningService();
    private final ClassicsContentSnapshotAssembler snapshotAssembler = new ClassicsContentSnapshotAssembler();
    private final AiCandidateDomainService aiCandidateDomainService;
    private final ClassicsAiCandidatePayloadParser aiCandidatePayloadParser;
    private final ClassicsTagBindingSupport tagBindingSupport;

    @Autowired
    public ClassicsContentApplicationServiceImpl(
            ClassicsContentRepository repository,
            WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer,
            SancaiEntryVersionRestorer sancaiEntryVersionRestorer,
            SancaiAssetApplicationService sancaiAssetApplicationService,
            StorageApplicationService storageApplicationService,
            WorkerRenderClient workerRenderClient,
            StorageUploadStreamHelper storageUploadStreamHelper,
            AiCandidateDomainService aiCandidateDomainService,
            ClassicsTagBindingSupport tagBindingSupport) {
        this.repository = repository;
        this.wangqiDocumentVersionRestorer = wangqiDocumentVersionRestorer;
        this.sancaiEntryVersionRestorer = sancaiEntryVersionRestorer;
        this.sancaiAssetApplicationService = sancaiAssetApplicationService;
        this.storageApplicationService = storageApplicationService;
        this.workerRenderClient = workerRenderClient;
        this.storageUploadStreamHelper = storageUploadStreamHelper;
        this.aiCandidateDomainService = aiCandidateDomainService;
        this.tagBindingSupport = tagBindingSupport;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.aiCandidatePayloadParser = new ClassicsAiCandidatePayloadParser(this.objectMapper);
    }

    @Override
    public List<ClassicsContentTag> listTags(String contentType, ClassicsContentId contentId) {
        return repository.listTags(contentType, contentId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortTags(ContentTagSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        String contentType = command == null ? null : command.getContentType();
        ClassicsContentId contentId = command == null ? null : command.getContentId();
        List<ClassicsContentTagId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (StringUtils.isBlank(contentType) || contentId == null || orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<ClassicsContentTag> currentTags = repository.listTags(contentType, contentId, effectiveDirection);
        if (currentTags == null || currentTags.isEmpty() || currentTags.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentTags.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentTags.size());
        List<ClassicsContentTagId> currentOrderedIds = new ArrayList<>(currentTags.size());
        for (int i = 0; i < currentTags.size(); i++) {
            ClassicsContentTag tag = currentTags.get(i);
            if (tag == null || tag.getId() == null) {
                throw sortDbFailure();
            }
            long tagId = tag.getId().value();
            indexById.put(tagId, i);
            priorityById.put(tagId, tag.getPriority());
            currentOrderedIds.add(tag.getId());
        }

        for (ClassicsContentTagId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null || !indexById.containsKey(orderedId.value())) {
                throw sortMissingId();
            }
        }

        int temporaryPriority = repository.maxTagPriority(contentType, contentId) + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            ClassicsContentTagId targetId = orderedIdList.get(i);
            ClassicsContentTagId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updateTagPriorityOrThrow(targetId, temporaryPriority++);
            updateTagPriorityOrThrow(currentId, targetPriority);
            updateTagPriorityOrThrow(targetId, currentPriority);

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);
            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentTagId addTag(ContentTagCommand command) {
        ClassicsContentId contentId = ClassicsContentId.of(command.getContentId());
        int nextPriority = repository.maxTagPriority(command.getContentType().value(), contentId) + 1;
        ClassicsContentTag tag;
        if (tagBindingSupport == null) {
            tag = command.toEntity();
        } else if (command.getSource() == ClassicsContentSource.AI) {
            tag = tagBindingSupport.bindAiTag(command, nextPriority);
        } else {
            tag = tagBindingSupport.bindManualTag(command, nextPriority);
        }
        tag.setId(null);
        if (tagBindingSupport == null) {
            tag.setPriority(nextPriority);
        }
        ClassicsContentTagId createdId = repository.insertTag(tag);
        if (tagBindingSupport != null) {
            tag.setId(createdId);
            tagBindingSupport.syncTagRef(tag);
        }
        return createdId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentTagId updateTag(ContentTagCommand command) {
        ClassicsContentTag existing =
                repository.getTagById(command == null ? null : ClassicsContentTagId.of(command.getId()));
        ClassicsContentTag tag = tagBindingSupport == null
                ? command.toEntity()
                : tagBindingSupport.bindManualTag(command, existing == null ? null : existing.getPriority());
        repository.updateTag(tag);
        if (tagBindingSupport != null) {
            if (existing != null
                    && existing.getTagId() != null
                    && (tag.getTagId() == null || !existing.getTagId().equals(tag.getTagId()))) {
                tagBindingSupport.removeTagRef(existing);
            }
            tagBindingSupport.syncTagRef(tag);
        }
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(ClassicsContentTagId id) {
        ClassicsContentTag existing = repository.getTagById(id);
        repository.deleteTagById(
                existing == null || existing.getContentType() == null
                        ? null
                        : existing.getContentType().value(),
                existing == null ? null : existing.getContentId(),
                id);
        if (tagBindingSupport != null) {
            tagBindingSupport.removeTagRef(existing);
        }
    }

    @Override
    public List<ClassicsContentQaPair> listQaPairs(String contentType, ClassicsContentId contentId) {
        return repository.listQaPairs(contentType, contentId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentQaPairId addQaPair(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = command.toEntity();
        qaPair.setId(null);
        qaPair.setPriority(repository.maxQaPairPriority() + 1);
        return repository.insertQaPair(qaPair);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentQaPairId updateQaPair(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = command.toEntity();
        repository.updateQaPair(qaPair);
        return qaPair.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortQaPairs(ContentQaPairSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        sortQaPairs(command, repository.listQaPairs(effectiveDirection));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortQaPairs(String contentType, ClassicsContentId contentId, ContentQaPairSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        sortQaPairs(command, repository.listQaPairs(contentType, contentId, effectiveDirection));
    }

    private void sortQaPairs(ContentQaPairSortCommand command, List<ClassicsContentQaPair> currentQaPairs) {
        List<ClassicsContentQaPairId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        if (currentQaPairs == null || currentQaPairs.isEmpty() || currentQaPairs.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentQaPairs.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentQaPairs.size());
        List<ClassicsContentQaPairId> currentOrderedIds = new ArrayList<>(currentQaPairs.size());
        for (int i = 0; i < currentQaPairs.size(); i++) {
            ClassicsContentQaPair qaPair = currentQaPairs.get(i);
            if (qaPair == null || qaPair.getId() == null) {
                throw sortDbFailure();
            }
            long qaId = qaPair.getId().value();
            indexById.put(qaId, i);
            priorityById.put(qaId, qaPair.getPriority());
            currentOrderedIds.add(qaPair.getId());
        }

        for (ClassicsContentQaPairId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null || !indexById.containsKey(orderedId.value())) {
                throw sortMissingId();
            }
        }

        int temporaryPriority = repository.maxQaPairPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            ClassicsContentQaPairId targetId = orderedIdList.get(i);
            ClassicsContentQaPairId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updateQaPairPriorityOrThrow(targetId, temporaryPriority++);
            updateQaPairPriorityOrThrow(currentId, targetPriority);
            updateQaPairPriorityOrThrow(targetId, currentPriority);

            priorityById.put(targetId.value(), currentPriority);
            priorityById.put(currentId.value(), targetPriority);
            currentOrderedIds.set(i, targetId);
            currentOrderedIds.set(targetIndex, currentId);
            indexById.put(targetId.value(), i);
            indexById.put(currentId.value(), targetIndex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQaPair(ClassicsContentQaPairId id) {
        repository.deleteQaPairById(id);
    }

    @Override
    public List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId) {
        return repository.listVersions(contentType, contentId);
    }

    @Override
    public ClassicsContentVersion getVersion(ClassicsContentVersionId id) {
        return repository.getVersionById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteVersions(String contentType, ClassicsContentId contentId) {
        return repository.deleteVersions(contentType, contentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentVersion ensureVersioned(
            Versionable content, ClassicsContentChangeType changeType, String changeSummary) {
        if (content == null) {
            return null;
        }
        if (!versioningService.needsVersion(content)) {
            return repository.latestVersion(content.contentType(), content.contentId());
        }

        ClassicsContentVersion version = versioningService.newVersion(
                content,
                versioningService.nextVersionNo(repository.latestVersionNo(content.contentType(), content.contentId())),
                new Date(),
                snapshotJson(content),
                changeType,
                changeSummary);
        ClassicsContentVersionId versionId = repository.insertVersion(version);
        version.setId(versionId);
        versioningService.markVersioned(content, version);
        return version;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentVersion applyAiResult(Versionable content, String changeSummary) {
        return ensureVersioned(content, ClassicsContentChangeType.AI_APPLIED, changeSummary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiCandidateApplyContentResult applyAiCandidate(AiCandidateApplyContentCommand command) {
        if (command == null) {
            throw new BizException("AI候选应用参数不能为空");
        }
        if (command.getCandidateId() == null
                || command.getContentType() == null
                || command.getContentId() == null
                || StringUtils.isBlank(command.getCapability())
                || StringUtils.isBlank(command.getResultFormat())
                || StringUtils.isBlank(command.getResultPayload())) {
            throw new BizException("AI候选应用参数不完整");
        }

        if (aiCandidateDomainService == null) {
            throw new BizException("AI候选服务未就绪");
        }
        AiCandidateApplyCheck check = new AiCandidateApplyCheck();
        check.setCandidateId(command.getCandidateId());
        check.setContentType(command.getContentType().value());
        check.setContentId(command.getContentId());
        check.setCapability(command.getCapability());
        aiCandidateDomainService.requirePendingForApply(check);

        ClassicsContentType contentType = command.getContentType();
        String capability = command.getCapability();
        ClassicsContentId contentId = ClassicsContentId.of(command.getContentId());
        String changeSummary = resolveChangeSummary(capability, command.getChangeSummary());
        Versionable content;

        if (contentType == ClassicsContentType.SANCAI_ENTRY) {
            SancaiEntry entry = repository.getSancaiEntryForAiApply(contentId);
            if (entry == null) {
                throw new BizException("三才内容不存在: " + contentId.value());
            }
            if ("translate".equals(capability)) {
                entry.setTranslationText(aiCandidatePayloadParser.parseText(command.getResultPayload()));
                entry.setTranslationStatus(SancaiEntryTranslationStatus.READY);
                touchContentUpdatedAt(ClassicsContentType.SANCAI_ENTRY, entry);
                ensureUpdate(repository.updateSancaiEntryAiFields(entry), "更新三才内容失败");
            } else if ("summary".equals(capability)) {
                entry.setSummary(aiCandidatePayloadParser.parseText(command.getResultPayload()));
                touchContentUpdatedAt(ClassicsContentType.SANCAI_ENTRY, entry);
                ensureUpdate(repository.updateSancaiEntryAiFields(entry), "更新三才内容失败");
            } else if ("tags".equals(capability)) {
                applyTags(contentType, entry, aiCandidatePayloadParser.parseTags(command.getResultPayload()));
            } else if ("qa".equals(capability)) {
                applyQaPairs(contentType, entry, aiCandidatePayloadParser.parseQaPairs(command.getResultPayload()));
            } else {
                throw new BizException("不支持的 AI 候选能力: " + capability);
            }
            content = entry;
        } else if (contentType == ClassicsContentType.WANGQI_DOCUMENT) {
            WangqiDocument document = repository.getWangqiDocumentForAiApply(contentId);
            if (document == null) {
                throw new BizException("王圻文档不存在: " + contentId.value());
            }
            if ("summary".equals(capability)) {
                document.setSummary(aiCandidatePayloadParser.parseText(command.getResultPayload()));
                touchContentUpdatedAt(ClassicsContentType.WANGQI_DOCUMENT, document);
                ensureUpdate(repository.updateWangqiDocumentAiFields(document), "更新王圻文档失败");
            } else if ("tags".equals(capability)) {
                applyTags(contentType, document, aiCandidatePayloadParser.parseTags(command.getResultPayload()));
            } else if ("qa".equals(capability)) {
                applyQaPairs(contentType, document, aiCandidatePayloadParser.parseQaPairs(command.getResultPayload()));
            } else {
                throw new BizException("不支持的 AI 候选能力: " + capability);
            }
            content = document;
        } else if (contentType == ClassicsContentType.MING_CUSTOMS) {
            MingCustomsEntry entry = repository.getMingCustomsEntryForAiApply(contentId);
            if (entry == null) {
                throw new BizException("明代习俗不存在: " + contentId.value());
            }
            if ("summary".equals(capability)) {
                entry.setSummary(aiCandidatePayloadParser.parseText(command.getResultPayload()));
                touchContentUpdatedAt(ClassicsContentType.MING_CUSTOMS, entry);
                ensureUpdate(repository.updateMingCustomsEntryAiFields(entry), "更新明代习俗失败");
            } else if ("tags".equals(capability)) {
                applyTags(contentType, entry, aiCandidatePayloadParser.parseTags(command.getResultPayload()));
            } else if ("qa".equals(capability)) {
                applyQaPairs(contentType, entry, aiCandidatePayloadParser.parseQaPairs(command.getResultPayload()));
            } else {
                throw new BizException("不支持的 AI 候选能力: " + capability);
            }
            content = entry;
        } else {
            throw new BizException("未知的内容类型: " + contentType);
        }

        if (content == null) {
            throw new BizException("内容不存在");
        }
        ClassicsContentVersion version = applyAiResult(content, changeSummary);
        aiCandidateDomainService.markApplied(
                command.getCandidateId(), command.getResultFormat(), command.getResultPayload(), Instant.now());
        return new AiCandidateApplyContentResult(
                contentType,
                contentId.value(),
                version == null
                        ? null
                        : version.getId() == null ? null : version.getId().value(),
                version == null ? null : version.getVersionNo());
    }

    private String resolveChangeSummary(String capability, String changeSummary) {
        if (StringUtils.isNotBlank(changeSummary)) {
            return changeSummary;
        }
        return switch (capability) {
            case "translate" -> "AI 应用：译文";
            case "summary" -> "AI 应用：摘要";
            case "tags" -> "AI 应用：标签";
            case "qa" -> "AI 应用：问答对";
            default -> throw new BizException("不支持的 AI 候选能力: " + capability);
        };
    }

    private void applyTags(ClassicsContentType contentType, Versionable content, List<String> tags) {
        ClassicsContentId contentId = content == null ? null : content.contentId();
        if (contentId == null) {
            throw new BizException("标签应用目标内容不存在");
        }
        if (tags == null || tags.isEmpty()) {
            throw new BizException("AI候选标签为空");
        }
        if (tagBindingSupport != null) {
            repository.listTags(contentType.value(), contentId, SortDirection.ASC).stream()
                    .filter(tag -> tag != null && tag.getSource() == ClassicsContentSource.AI)
                    .forEach(tagBindingSupport::removeTagRef);
        }
        repository.deleteAiTags(contentType.value(), contentId);
        for (String tagName : tags) {
            if (StringUtils.isBlank(tagName)) {
                continue;
            }
            addTag(new ContentTagCommand(
                    null,
                    contentType,
                    contentId.value(),
                    null,
                    tagName.trim(),
                    ClassicsContentSource.AI,
                    ClassicsContentTagStatus.ACTIVE));
        }
        touchContentUpdatedAt(contentType, content);
    }

    private void applyQaPairs(
            ClassicsContentType contentType, Versionable content, List<AiCandidateQaPairPayload> qaPairs) {
        ClassicsContentId contentId = content == null ? null : content.contentId();
        if (contentId == null) {
            throw new BizException("问答应用目标内容不存在");
        }
        if (qaPairs == null || qaPairs.isEmpty()) {
            throw new BizException("AI候选问答为空");
        }
        repository.deleteAiQaPairs(contentType.value(), contentId);
        for (AiCandidateQaPairPayload pair : qaPairs) {
            if (pair == null || StringUtils.isBlank(pair.getQuestion()) || StringUtils.isBlank(pair.getAnswer())) {
                continue;
            }
            addQaPair(new ContentQaPairCommand(
                    null,
                    contentType,
                    contentId.value(),
                    pair.getQuestion(),
                    pair.getAnswer(),
                    ClassicsContentSource.AI));
        }
        touchContentUpdatedAt(contentType, content);
    }

    private void touchContentUpdatedAt(ClassicsContentType contentType, Versionable content) {
        if (content == null) {
            return;
        }
        Date now = new Date();
        if (contentType == ClassicsContentType.SANCAI_ENTRY) {
            ((SancaiEntry) content).setContentUpdatedAt(now);
            return;
        }
        if (contentType == ClassicsContentType.WANGQI_DOCUMENT) {
            ((WangqiDocument) content).setContentUpdatedAt(now);
            return;
        }
        if (contentType == ClassicsContentType.MING_CUSTOMS) {
            ((MingCustomsEntry) content).setContentUpdatedAt(now);
        }
    }

    private void ensureUpdate(int updated, String message) {
        if (updated != 1) {
            throw new BizException(message + ": " + updated);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentVersion restoreHistoryVersion(ClassicsContentVersionId versionId) {
        ClassicsContentVersion version = repository.getVersionById(versionId);
        if (version == null) {
            throw new BizException("历史版本不存在");
        }
        if (version.getContentType() == ClassicsContentType.WANGQI_DOCUMENT) {
            Versionable restored = wangqiDocumentVersionRestorer.restoreSnapshot(version);
            ClassicsContentVersion restoredVersion = createRestoredVersion(restored, version);
            wangqiDocumentVersionRestorer.markVersioned((WangqiDocument) restored);
            return restoredVersion;
        }
        if (version.getContentType() == ClassicsContentType.SANCAI_ENTRY) {
            Versionable restored = sancaiEntryVersionRestorer.restoreSnapshot(version);
            ClassicsContentVersion restoredVersion = createRestoredVersion(restored, version);
            sancaiEntryVersionRestorer.markVersioned((SancaiEntry) restored);
            return restoredVersion;
        }
        throw new BizException("暂不支持恢复该类型历史版本: " + version.getContentType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsExportJobResult createExportJob(ContentExportCommand command) {
        ClassicsContentExportJob job = command.toEntity();
        if (job.getRequestedAt() == null) {
            job.setRequestedAt(new Date());
        }
        ClassicsContentExportJobId jobId = repository.insertExportJob(job);
        if (jobId == null) {
            throw new BizException("创建导出任务失败");
        }

        try {
            WorkerRenderDtos.WorkerRenderResponse response =
                    workerRenderClient.renderClassicsExport(renderRequest(jobId, job));
            if (!isSuccess(response)) {
                repository.markExportJobFailed(jobId);
                return new ClassicsExportJobResult(jobId, ClassicsExportStatus.FAILED, null);
            }
            StorageUploadResult uploadResult = saveRenderArtifact(jobId, response);
            if (uploadResult.hasError()) {
                repository.markExportJobFailed(jobId);
                return new ClassicsExportJobResult(jobId, ClassicsExportStatus.FAILED, null);
            }
            StorageObjectId storageObjectId = toStorageObjectId(uploadResult);
            int itemCount =
                    response.getSummary() == null || response.getSummary().getItemCount() == null
                            ? 0
                            : response.getSummary().getItemCount();
            repository.markExportJobCompleted(
                    jobId,
                    storageObjectId,
                    new Date(job.getRequestedAt().getTime() + EXPORT_EXPIRES_DAYS * 24L * 60L * 60L * 1000L),
                    itemCount,
                    job.getAssetCount());
            return new ClassicsExportJobResult(jobId, ClassicsExportStatus.COMPLETED, storageObjectId);
        } catch (Exception ex) {
            repository.markExportJobFailed(jobId);
            return new ClassicsExportJobResult(jobId, ClassicsExportStatus.FAILED, null);
        }
    }

    @Override
    public PageResult<ClassicsContentExportJob> pageExportJobs(
            String contentType, String exportKind, String status, PageQuery page) {
        IPage<ClassicsContentExportJob> dataPage =
                repository.pageExportJobs(contentType, exportKind, status, page.getPageNo(), page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
    }

    @Override
    public ClassicsContentExportJob getExportJob(ClassicsContentExportJobId id) {
        return repository.getExportJobById(id);
    }

    private void updateTagPriorityOrThrow(ClassicsContentTagId id, int priority) {
        ClassicsContentTag tag = new ClassicsContentTag();
        tag.setId(id);
        tag.setPriority(priority);
        if (repository.updateTagPriority(tag) != 1) {
            throw sortDbFailure();
        }
    }

    private void updateQaPairPriorityOrThrow(ClassicsContentQaPairId id, int priority) {
        ClassicsContentQaPair qaPair = new ClassicsContentQaPair();
        qaPair.setId(id);
        qaPair.setPriority(priority);
        if (repository.updateQaPairPriority(qaPair) != 1) {
            throw sortDbFailure();
        }
    }

    private ClassicsContentVersion createRestoredVersion(Versionable content, ClassicsContentVersion restoredFrom) {
        ClassicsContentVersion version = versioningService.newVersion(
                content,
                versioningService.nextVersionNo(repository.latestVersionNo(content.contentType(), content.contentId())),
                new Date(),
                snapshotJson(content),
                ClassicsContentChangeType.HISTORY_RESTORED,
                "恢复历史版本 v" + restoredFrom.getVersionNo());
        ClassicsContentVersionId versionId = repository.insertVersion(version);
        version.setId(versionId);
        versioningService.markVersioned(content, version);
        return version;
    }

    private String snapshotJson(Versionable content) {
        if (content instanceof SancaiEntry entry && sancaiAssetApplicationService != null) {
            List<SancaiEntryImage> images = sancaiAssetApplicationService.listImages(entry.getId());
            if (storageApplicationService == null) {
                return snapshotAssembler.toSnapshotJson(entry, images);
            }
            return snapshotAssembler.toSnapshotJsonWithImageResources(
                    entry, images.stream().map(this::toImageResource).toList());
        }
        return snapshotAssembler.toSnapshotJson(content);
    }

    private WorkerRenderDtos.WorkerRenderRequest renderRequest(
            ClassicsContentExportJobId jobId, ClassicsContentExportJob job) {
        WorkerRenderDtos.WorkerRenderRequest request = new WorkerRenderDtos.WorkerRenderRequest();
        request.setRequestId("classics-export-" + jobId.value());
        request.setTraceId("classics-export-" + jobId.value());
        request.setCallerDomain("CLASSICS");
        request.setOperation(DEFAULT_RENDER_OPERATION);
        request.setRenderType(DEFAULT_RENDER_TYPE);
        request.setTemplate(renderTemplate());
        request.setOutput(renderOutput(
                job.getExportFormat() == null ? null : job.getExportFormat().value(), jobId));
        request.setInput(renderInput(job.getScopeJson()));
        request.setOptions(renderOptions());
        return request;
    }

    private WorkerRenderDtos.Template renderTemplate() {
        WorkerRenderDtos.Template template = new WorkerRenderDtos.Template();
        template.setTemplateId(DEFAULT_EXPORT_TEMPLATE_ID);
        template.setTemplateVersion(DEFAULT_EXPORT_TEMPLATE_VERSION);
        return template;
    }

    private WorkerRenderDtos.Output renderOutput(String format, ClassicsContentExportJobId jobId) {
        WorkerRenderDtos.Output output = new WorkerRenderDtos.Output();
        output.setFormat(format);
        output.setFilenameHint(DEFAULT_TITLE + "-" + jobId.value() + "." + formatSafeSuffix(format));
        output.setLocale("zh-CN");
        return output;
    }

    private String formatSafeSuffix(String format) {
        return format == null ? "zip" : format.toLowerCase();
    }

    private WorkerRenderDtos.Input renderInput(String scopeJson) {
        WorkerRenderDtos.Input input = new WorkerRenderDtos.Input();
        input.setSnapshotId(null);
        input.setContentType("CLASSICS_EXPORT_SNAPSHOT");
        input.setPayloadJson(renderPayloadJson(scopeJson));
        return input;
    }

    private String renderPayloadJson(String scopeJson) {
        JsonNode payload = normalizePayload(scopeJson);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private WorkerRenderDtos.Options renderOptions() {
        WorkerRenderDtos.Options options = new WorkerRenderDtos.Options();
        options.setStream(false);
        options.setIncludeMetadata(true);
        return options;
    }

    private JsonNode normalizePayload(String scopeJson) {
        JsonNode payload = parsePayload(scopeJson);
        if (payload.isObject()) {
            ensurePayloadDefaults((ObjectNode) payload);
            return payload;
        }
        ObjectNode wrapped = objectMapper.createObjectNode();
        wrapped.put("title", DEFAULT_TITLE);
        ArrayNode items = objectMapper.createArrayNode();
        items.add(payload == null ? "" : payload.asText());
        wrapped.set("items", items);
        return wrapped;
    }

    private void ensurePayloadDefaults(ObjectNode payload) {
        if (!payload.has("title")) {
            payload.put("title", DEFAULT_TITLE);
        }
        if (!payload.has("items")) {
            payload.set("items", objectMapper.createArrayNode());
        }
    }

    private JsonNode parsePayload(String scopeJson) {
        try {
            if (scopeJson == null || scopeJson.isBlank()) {
                return defaultPayload();
            }
            JsonNode parsed = objectMapper.readTree(scopeJson);
            return parsed == null ? defaultPayload() : parsed;
        } catch (Exception ex) {
            return defaultPayload();
        }
    }

    private ObjectNode defaultPayload() {
        ObjectNode defaultPayload = objectMapper.createObjectNode();
        defaultPayload.put("title", DEFAULT_TITLE);
        defaultPayload.set("items", objectMapper.createArrayNode());
        return defaultPayload;
    }

    private StorageUploadResult saveRenderArtifact(
            ClassicsContentExportJobId jobId, WorkerRenderDtos.WorkerRenderResponse response) throws Exception {
        WorkerRenderDtos.Artifact artifact = response.getArtifact();
        byte[] content = artifactContent(artifact);
        return storageUploadStreamHelper.uploadServerArtifact(
                new ByteArrayInputStream(content),
                filenameHint(artifact.getFilename(), response),
                artifact.getContentType(),
                content.length);
    }

    private String filenameHint(String originalFilename, WorkerRenderDtos.WorkerRenderResponse response) {
        if (originalFilename != null && !originalFilename.isBlank()) {
            return originalFilename;
        }
        String format =
                response.getArtifact() == null ? "zip" : response.getArtifact().getFormat();
        return "classics-export-" + System.currentTimeMillis() + "." + formatSafeSuffix(format);
    }

    private StorageObjectId toStorageObjectId(StorageUploadResult uploadResult) {
        return uploadResult == null
                        || uploadResult.getStorage() == null
                        || uploadResult.getStorage().getId() == null
                ? null
                : StorageObjectId.of(uploadResult.getStorage().getId().value());
    }

    private byte[] artifactContent(WorkerRenderDtos.Artifact artifact) {
        if (artifact == null
                || artifact.getContent() == null
                || artifact.getContent().isBlank()) {
            return new byte[0];
        }
        if ("TEXT".equalsIgnoreCase(artifact.getEncoding())) {
            return artifact.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        if ("BASE64".equalsIgnoreCase(artifact.getEncoding())) {
            return Base64.getDecoder().decode(artifact.getContent());
        }
        throw new BizException("暂不支持的导出产物编码: " + artifact.getEncoding());
    }

    private boolean isSuccess(WorkerRenderDtos.WorkerRenderResponse response) {
        return response != null && "SUCCEEDED".equalsIgnoreCase(response.getStatus()) && response.getArtifact() != null;
    }

    private SancaiEntryVersionSnapshot.ImageResource toImageResource(SancaiEntryImage image) {
        StoredObject storage = image == null || image.getStorageObjectId() == null
                ? null
                : storageApplicationService.get(
                        StoredObjectIdCodec.toDomain(StorageObjectIdCodec.toValue(image.getStorageObjectId())));
        return SancaiEntryVersionSnapshot.ImageResource.from(image, storage);
    }

    private static BizException sortEmptyInput() {
        return new BizException(
                ErrorCode.SORT_EMPTY_INPUT.getCode(),
                ErrorCode.SORT_EMPTY_INPUT.getMessageKey(),
                ErrorCode.SORT_EMPTY_INPUT.getMessage());
    }

    private static BizException sortMissingId() {
        return new BizException(
                ErrorCode.SORT_MISSING_ID.getCode(),
                ErrorCode.SORT_MISSING_ID.getMessageKey(),
                ErrorCode.SORT_MISSING_ID.getMessage());
    }

    private static BizException sortDbFailure() {
        return new BizException(
                ErrorCode.SORT_DB_FAILURE.getCode(),
                ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                ErrorCode.SORT_DB_FAILURE.getMessage());
    }
}
