package com.thundax.kuzhambu.classics.application.content.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
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
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.support.SancaiEntryVersionRestorer;
import com.thundax.kuzhambu.classics.application.searchsync.support.ClassicsSearchIndexSyncPublishSupport;
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
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
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
    private static final String SYSTEM_OWNER_ID = "system";
    private static final String USER_OWNER_TYPE = "USER";
    private static final String EXPORT_REFERENCE_USAGE = "CLASSICS_EXPORT_JOB";

    private final ClassicsContentRepository repository;
    private final WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer;
    private final SancaiEntryVersionRestorer sancaiEntryVersionRestorer;
    private final SancaiAssetApplicationService sancaiAssetApplicationService;
    private final WorkerRenderClient workerRenderClient;
    private final StorageFacade storageFacade;
    private final ObjectMapper objectMapper;
    private final ClassicsContentVersioningService versioningService = new ClassicsContentVersioningService();
    private final ClassicsContentSnapshotAssembler snapshotAssembler = new ClassicsContentSnapshotAssembler();
    private final AiFacade aiFacade;
    private final ClassicsAiCandidatePayloadParser aiCandidatePayloadParser;
    private final ClassicsTagBindingSupport tagBindingSupport;
    private final ClassicsSearchIndexSyncPublishSupport searchIndexSyncPublishSupport;

    @Autowired
    public ClassicsContentApplicationServiceImpl(
            ClassicsContentRepository repository,
            WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer,
            SancaiEntryVersionRestorer sancaiEntryVersionRestorer,
            SancaiAssetApplicationService sancaiAssetApplicationService,
            WorkerRenderClient workerRenderClient,
            StorageFacade storageFacade,
            AiFacade aiFacade,
            ClassicsTagBindingSupport tagBindingSupport,
            ClassicsSearchIndexSyncPublishSupport searchIndexSyncPublishSupport) {
        this.repository = repository;
        this.wangqiDocumentVersionRestorer = wangqiDocumentVersionRestorer;
        this.sancaiEntryVersionRestorer = sancaiEntryVersionRestorer;
        this.sancaiAssetApplicationService = sancaiAssetApplicationService;
        this.workerRenderClient = workerRenderClient;
        this.storageFacade = storageFacade;
        this.aiFacade = aiFacade;
        this.tagBindingSupport = tagBindingSupport;
        this.searchIndexSyncPublishSupport = searchIndexSyncPublishSupport;
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
        ClassicsContentType contentType = command.getContentType();
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
        versionAndPublishContentSync(contentType, contentId, "手动更新标签");
        return createdId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentTagId updateTag(ContentTagCommand command) {
        ClassicsContentType contentType = command.getContentType();
        ClassicsContentId contentId = ClassicsContentId.of(command.getContentId());
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
        versionAndPublishContentSync(contentType, contentId, "手动更新标签");
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
        if (existing != null && existing.getContentType() != null && existing.getContentId() != null) {
            versionAndPublishContentSync(existing.getContentType(), existing.getContentId(), "手动删除标签");
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
        ClassicsContentQaPairId createdId = repository.insertQaPair(qaPair);
        versionAndPublishContentSync(qaPair.getContentType(), qaPair.getContentId(), "手动更新问答对");
        return createdId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentQaPairId updateQaPair(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = command.toEntity();
        repository.updateQaPair(qaPair);
        versionAndPublishContentSync(qaPair.getContentType(), qaPair.getContentId(), "手动更新问答对");
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
        ClassicsContentQaPair existing = repository.getQaPairById(id);
        repository.deleteQaPairById(id);
        if (existing != null && existing.getContentType() != null && existing.getContentId() != null) {
            versionAndPublishContentSync(existing.getContentType(), existing.getContentId(), "手动删除问答对");
        }
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

        if (aiFacade == null) {
            throw new BizException("AI候选服务未就绪");
        }
        aiFacade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                .candidateId(command.getCandidateId())
                .contentType(command.getContentType().value())
                .contentId(command.getContentId())
                .capability(command.getCapability())
                .build());

        ClassicsContentType contentType = command.getContentType();
        String capability = command.getCapability();
        ClassicsContentId contentId = ClassicsContentId.of(command.getContentId());
        String changeSummary = resolveChangeSummary(capability, command.getChangeSummary());
        Versionable content = null;

        if (contentType == ClassicsContentType.SANCAI_ENTRY) {
            SancaiEntry entry = repository.getSancaiEntryForAiApply(contentId);
            if (entry == null) {
                throw new BizException("三才内容不存在: " + contentId.value());
            }
            if ("image_analysis".equals(capability)) {
                if (command.getObjectId() == null) {
                    throw new BizException("AI候选应用参数不完整");
                }
                if (sancaiAssetApplicationService == null) {
                    throw new BizException("三才图片服务未就绪");
                }
                SancaiVisualAsset visualAsset = findVisualAsset(contentId, command.getObjectId());
                if (visualAsset == null) {
                    throw new BizException("三才视觉资产不存在: " + command.getObjectId());
                }
                String imageAnalysisMarkdown = aiCandidatePayloadParser.parseText(command.getResultPayload());
                visualAsset.setImageAnalysisMarkdown(imageAnalysisMarkdown);
                sancaiAssetApplicationService.updateVisualAsset(visualAsset);
            } else if ("visual".equals(capability)) {
                if (command.getObjectId() == null) {
                    throw new BizException("AI候选应用参数不完整");
                }
                if (sancaiAssetApplicationService == null) {
                    throw new BizException("三才图片服务未就绪");
                }
                SancaiVisualAsset visualAsset = findVisualAsset(contentId, command.getObjectId());
                if (visualAsset == null) {
                    throw new BizException("三才视觉资产不存在: " + command.getObjectId());
                }
                String visualDescription = aiCandidatePayloadParser.parseText(command.getResultPayload());
                visualAsset.setVisualDescription(visualDescription);
                sancaiAssetApplicationService.updateVisualAsset(visualAsset);
            } else if ("fusion".equals(capability)) {
                if (command.getObjectId() == null) {
                    throw new BizException("AI候选应用参数不完整");
                }
                if (sancaiAssetApplicationService == null) {
                    throw new BizException("三才图片服务未就绪");
                }
                SancaiVisualAsset visualAsset = findVisualAsset(contentId, command.getObjectId());
                if (visualAsset == null) {
                    throw new BizException("三才视觉资产不存在: " + command.getObjectId());
                }
                String fusionDescription = aiCandidatePayloadParser.parseText(command.getResultPayload());
                visualAsset.setFusionDescription(fusionDescription);
                sancaiAssetApplicationService.updateVisualAsset(visualAsset);
            } else if ("translate".equals(capability)) {
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
            if (!"image_analysis".equals(capability) && !"visual".equals(capability) && !"fusion".equals(capability)) {
                content = entry;
            }
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
            if ("image_analysis".equals(capability) || "visual".equals(capability) || "fusion".equals(capability)) {
                aiFacade.markCandidateApplied(MarkAiCandidateAppliedFacadeRequest.builder()
                        .candidateId(command.getCandidateId())
                        .resultFormat(command.getResultFormat())
                        .resultPayload(command.getResultPayload())
                        .appliedAt(Instant.now())
                        .build());
                return new AiCandidateApplyContentResult(contentType, contentId.value(), null, null);
            }
            throw new BizException("内容不存在");
        }
        ClassicsContentVersion version = applyAiResult(content, changeSummary);
        persistVersionMarkers(content);
        publishSearchSyncAfterCommit(content);
        aiFacade.markCandidateApplied(MarkAiCandidateAppliedFacadeRequest.builder()
                .candidateId(command.getCandidateId())
                .resultFormat(command.getResultFormat())
                .resultPayload(command.getResultPayload())
                .appliedAt(Instant.now())
                .build());
        return new AiCandidateApplyContentResult(
                contentType,
                contentId.value(),
                version == null
                        ? null
                        : version.getId() == null ? null : version.getId().value(),
                version == null ? null : version.getVersionNo());
    }

    private SancaiVisualAsset findVisualAsset(ClassicsContentId contentId, Long objectId) {
        if (sancaiAssetApplicationService == null || contentId == null || objectId == null) {
            return null;
        }
        return sancaiAssetApplicationService.listVisualAssets(SancaiEntryId.of(contentId.value())).stream()
                .filter(visualAsset -> visualAsset != null
                        && visualAsset.getId() != null
                        && objectId.equals(visualAsset.getId().value()))
                .findFirst()
                .orElse(null);
    }

    private String resolveChangeSummary(String capability, String changeSummary) {
        if (StringUtils.isNotBlank(changeSummary)) {
            return changeSummary;
        }
        return switch (capability) {
            case "image_analysis" -> "AI 应用：图片理解";
            case "visual" -> "AI 应用：视觉描述";
            case "fusion" -> "AI 应用：信息融合";
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
            insertTagWithoutVersion(new ContentTagCommand(
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
            insertQaPairWithoutVersion(new ContentQaPairCommand(
                    null,
                    contentType,
                    contentId.value(),
                    pair.getQuestion(),
                    pair.getAnswer(),
                    ClassicsContentSource.AI));
        }
        touchContentUpdatedAt(contentType, content);
    }

    private ClassicsContentTagId insertTagWithoutVersion(ContentTagCommand command) {
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

    private ClassicsContentQaPairId insertQaPairWithoutVersion(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = command.toEntity();
        qaPair.setId(null);
        qaPair.setPriority(repository.maxQaPairPriority() + 1);
        return repository.insertQaPair(qaPair);
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

    private void versionAndPublishContentSync(
            ClassicsContentType contentType, ClassicsContentId contentId, String changeSummary) {
        Versionable content = loadContentForGovernance(contentType, contentId);
        if (content == null) {
            return;
        }
        touchContentUpdatedAt(contentType, content);
        ensureVersioned(content, ClassicsContentChangeType.MANUAL_SAVE, changeSummary);
        persistVersionMarkers(content);
        publishSearchSyncAfterCommit(content);
    }

    private Versionable loadContentForGovernance(ClassicsContentType contentType, ClassicsContentId contentId) {
        if (contentType == null || contentId == null) {
            return null;
        }
        return switch (contentType) {
            case SANCAI_ENTRY -> repository.getSancaiEntryForAiApply(contentId);
            case WANGQI_DOCUMENT -> repository.getWangqiDocumentForAiApply(contentId);
            case MING_CUSTOMS -> repository.getMingCustomsEntryForAiApply(contentId);
        };
    }

    private void persistVersionMarkers(Versionable content) {
        if (content instanceof SancaiEntry entry) {
            repository.updateSancaiEntryVersionMarkers(entry);
            return;
        }
        if (content instanceof WangqiDocument document) {
            repository.updateWangqiDocumentVersionMarkers(document);
            return;
        }
        if (content instanceof MingCustomsEntry entry) {
            repository.updateMingCustomsEntryVersionMarkers(entry);
        }
    }

    private void publishSearchSyncAfterCommit(Versionable content) {
        if (searchIndexSyncPublishSupport == null || content == null || content.contentType() == null) {
            return;
        }
        ClassicsContentId contentId = content.contentId();
        Integer currentVersionNo = content.currentVersionNo();
        if (contentId == null || contentId.value() == null || currentVersionNo == null) {
            return;
        }
        if (isPublicSearchContent(content)) {
            searchIndexSyncPublishSupport.publishUpsertAfterCommit(
                    content.contentType(), String.valueOf(contentId.value()), currentVersionNo);
            return;
        }
        searchIndexSyncPublishSupport.publishDeleteAfterCommit(
                content.contentType(), String.valueOf(contentId.value()), currentVersionNo);
    }

    private boolean isPublicSearchContent(Versionable content) {
        if (content instanceof SancaiEntry entry) {
            return entry.getLifecycleStatus()
                            == com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus
                                    .PUBLISHED
                    && entry.getVisibility()
                            == com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility.PUBLIC;
        }
        if (content instanceof WangqiDocument document) {
            return document.getVisibility()
                    == com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility.PUBLIC;
        }
        if (content instanceof MingCustomsEntry entry) {
            return entry.getVisibility()
                    == com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility.PUBLIC;
        }
        return false;
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
            UploadStorageFacadeResponse uploadResponse = saveRenderArtifact(jobId, response);
            if (uploadResponse == null || uploadResponse.getStorageObjectId() == null) {
                repository.markExportJobFailed(jobId);
                return new ClassicsExportJobResult(jobId, ClassicsExportStatus.FAILED, null);
            }
            bindRenderArtifactOwner(jobId, uploadResponse.getStorageObjectId());
            StorageObjectId storageObjectId = toStorageObjectId(uploadResponse);
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
        return repository.pageExportJobs(contentType, exportKind, status, page.getPageNo(), page.getPageSize());
    }

    @Override
    public ClassicsContentExportJob getExportJob(ClassicsContentExportJobId id) {
        return repository.getExportJobById(id);
    }

    @Override
    public ClassicsStoredContentResult getExportJobContent(ClassicsContentExportJobId id) {
        ClassicsContentExportJob job = getExportJob(id);
        if (job == null
                || job.getStorageObjectId() == null
                || job.getStatus() != ClassicsExportStatus.COMPLETED
                || job.getExpiresAt() == null
                || job.getExpiresAt().before(new Date())
                || storageFacade == null) {
            return null;
        }
        OpenStorageFacadeResponse response = storageFacade.open(OpenStorageFacadeRequest.builder()
                .storageObjectId(StorageObjectIdCodec.toValue(job.getStorageObjectId()))
                .build());
        if (response == null || response.getStoredObject() == null || response.getInputStream() == null) {
            return null;
        }
        StorageObjectFacadeDto storedObject = response.getStoredObject();
        return new ClassicsStoredContentResult(
                storedObject.getId(),
                storedObject.getOriginalFilename(),
                storedObject.getContentType(),
                storedObject.getSize(),
                response.getInputStream());
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
            if (storageFacade == null) {
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

    private UploadStorageFacadeResponse saveRenderArtifact(
            ClassicsContentExportJobId jobId, WorkerRenderDtos.WorkerRenderResponse response) throws Exception {
        WorkerRenderDtos.Artifact artifact = response.getArtifact();
        byte[] content = artifactContent(artifact);
        return storageFacade == null
                ? null
                : storageFacade.upload(UploadStorageFacadeRequest.builder()
                        .inputStream(new ByteArrayInputStream(content))
                        .originalFilename(filenameHint(artifact.getFilename(), response))
                        .contentType(artifact.getContentType())
                        .sizeBytes((long) content.length)
                        .ownerType(USER_OWNER_TYPE)
                        .ownerId(SYSTEM_OWNER_ID)
                        .build());
    }

    private String filenameHint(String originalFilename, WorkerRenderDtos.WorkerRenderResponse response) {
        if (originalFilename != null && !originalFilename.isBlank()) {
            return originalFilename;
        }
        String format =
                response.getArtifact() == null ? "zip" : response.getArtifact().getFormat();
        return "classics-export-" + System.currentTimeMillis() + "." + formatSafeSuffix(format);
    }

    private void bindRenderArtifactOwner(ClassicsContentExportJobId jobId, Long storageObjectId) {
        if (storageFacade == null || storageObjectId == null) {
            return;
        }
        storageFacade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(storageObjectId))
                .ownerId(SYSTEM_OWNER_ID)
                .ownerType(USER_OWNER_TYPE)
                .ownerParams("usage=" + EXPORT_REFERENCE_USAGE + ";jobId=" + jobId.value())
                .build());
    }

    private StorageObjectId toStorageObjectId(UploadStorageFacadeResponse uploadResponse) {
        return uploadResponse == null || uploadResponse.getStorageObjectId() == null
                ? null
                : StorageObjectId.of(uploadResponse.getStorageObjectId());
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
        StorageObjectFacadeDto storage = null;
        if (image != null && image.getStorageObjectId() != null && storageFacade != null) {
            OpenStorageFacadeResponse response = storageFacade.open(OpenStorageFacadeRequest.builder()
                    .storageObjectId(image.getStorageObjectId().value())
                    .build());
            storage = response == null ? null : response.getStoredObject();
        }
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
