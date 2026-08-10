package com.thundax.kuzhambu.classics.application.content.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.MarkAiCandidateAppliedFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RejectAiCandidateFacadeRequest;
import com.thundax.kuzhambu.ai.facade.request.RequirePendingAiCandidateFacadeRequest;
import com.thundax.kuzhambu.classics.application.content.assembler.ClassicsContentApplicationAssembler;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchApplyContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentCommand;
import com.thundax.kuzhambu.classics.application.content.command.AiCandidateBatchRejectContentItemCommand;
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
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentPermissionSupport;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsContentSnapshotAssembler;
import com.thundax.kuzhambu.classics.application.content.support.ClassicsTagBindingSupport;
import com.thundax.kuzhambu.classics.application.content.support.MingCustomsVersionSnapshot;
import com.thundax.kuzhambu.classics.application.content.support.SancaiEntryVersionSnapshot;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteGuard;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteOperation;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationItemResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsBatchOperationResult;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.support.SancaiEntryVersionRestorer;
import com.thundax.kuzhambu.classics.application.wangqi.support.WangqiDocumentVersionRestorer;
import com.thundax.kuzhambu.classics.domain.common.client.WorkerRenderClient;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
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
import com.thundax.kuzhambu.classics.domain.content.support.ClassicsContentVersioningSupport;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.core.sort.SortablePrioritySwapSupport;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private static final String EXPORT_OWNER_TYPE = "CLASSICS_CONTENT_EXPORT_JOB";
    private static final String EXPORT_OWNER_ID_PREFIX = "export-job:";
    private static final long MAX_RENDER_ARTIFACT_SIZE_BYTES = 50L * 1024L * 1024L;

    private final ClassicsContentRepository repository;
    private final WangqiDocumentVersionRestorer wangqiDocumentVersionRestorer;
    private final SancaiEntryVersionRestorer sancaiEntryVersionRestorer;
    private final SancaiAssetApplicationService sancaiAssetApplicationService;
    private final SancaiRepository sancaiRepository;
    private final WorkerRenderClient workerRenderClient;
    private final StorageFacade storageFacade;
    private final ObjectMapper objectMapper;
    private final ClassicsContentVersioningSupport versioningSupport = new ClassicsContentVersioningSupport();
    private final ClassicsContentSnapshotAssembler snapshotAssembler = new ClassicsContentSnapshotAssembler();
    private final AiFacade aiFacade;
    private final ClassicsAiCandidatePayloadParser aiCandidatePayloadParser;
    private final ClassicsTagBindingSupport tagBindingSupport;
    private final ClassicsPublicationWriteGuard publicationWriteGuard;
    private static final String DEFAULT_REJECT_ERROR_TYPE = "USER_REJECTED";
    private static final String DEFAULT_REJECT_ERROR_MESSAGE = "用户已批量拒绝该 AI 候选";
    private static final String FAILURE_PERMISSION_DENIED = "PERMISSION_DENIED";
    private static final String FAILURE_CANDIDATE_NOT_PENDING = "CANDIDATE_NOT_PENDING";
    private static final String FAILURE_CANDIDATE_TARGET_MISMATCH = "CANDIDATE_TARGET_MISMATCH";
    private static final String FAILURE_UNSUPPORTED_CAPABILITY = "UNSUPPORTED_CAPABILITY";
    private static final String FAILURE_CONTENT_NOT_FOUND = "CONTENT_NOT_FOUND";
    private static final String FAILURE_VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String FAILURE_UNKNOWN = "UNKNOWN_FAILURE";

    private static final String AI_CAPABILITY_CLASSICS_TRANSLATE = "CLASSICS_TRANSLATE";
    private static final String AI_CAPABILITY_CLASSICS_SUMMARY = "CLASSICS_SUMMARY";
    private static final String AI_CAPABILITY_CLASSICS_TAGS = "CLASSICS_TAG_EXTRACT";
    private static final String AI_CAPABILITY_CLASSICS_QA = "CLASSICS_QA";
    private static final String AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE = "CLASSICS_IMAGE_DESCRIBE";
    private static final String AI_CAPABILITY_CLASSICS_IMAGE_PROMPT_FUSION = "CLASSICS_IMAGE_PROMPT_FUSION";
    private static final String AI_CAPABILITY_CLASSICS_VISUAL_DESCRIBE = "CLASSICS_VISUAL_DESCRIBE";
    private static final String AI_CAPABILITY_CLASSICS_IMAGE_GENERATE = "CLASSICS_IMAGE_GENERATE";

    private static final String APPLIED_STATUS = "APPLIED";
    private static final String REJECTED_STATUS = "REJECTED";
    private static final String TAG_APPLY_MODE_APPEND = "APPEND";
    private static final String TAG_APPLY_MODE_COVER = "COVER";

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
            SancaiRepository sancaiRepository,
            ClassicsPublicationWriteGuard publicationWriteGuard) {
        this.repository = repository;
        this.wangqiDocumentVersionRestorer = wangqiDocumentVersionRestorer;
        this.sancaiEntryVersionRestorer = sancaiEntryVersionRestorer;
        this.sancaiAssetApplicationService = sancaiAssetApplicationService;
        this.sancaiRepository = sancaiRepository;
        this.workerRenderClient = workerRenderClient;
        this.storageFacade = storageFacade;
        this.aiFacade = aiFacade;
        this.tagBindingSupport = tagBindingSupport;
        this.publicationWriteGuard = publicationWriteGuard;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.aiCandidatePayloadParser = new ClassicsAiCandidatePayloadParser(this.objectMapper);
    }

    @Override
    public List<ClassicsContentTag> listTags(String contentType, ClassicsContentId contentId) {
        requireTagScope(contentType, contentId);
        return repository.listTags(contentType, contentId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortTags(ContentTagSortCommand command) {
        List<ClassicsContentTagId> orderedIdList = command == null ? null : command.orderedIds();
        List<ClassicsContentTag> tags = repository.listTags(SortDirection.ASC);
        tags.stream()
                .filter(Objects::nonNull)
                .filter(tag -> tag.getContentType() != null && tag.getContentId() != null)
                .map(tag -> new ContentRef(tag.getContentType(), tag.getContentId()))
                .distinct()
                .forEach(ref -> requireWritable(ref.contentType(), ref.contentId()));
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                tags,
                ClassicsContentTag::getId,
                ClassicsContentTagId::value,
                ClassicsContentTag::getPriority,
                () -> repository.maxTagPriority(null, null),
                this::updateTagPriorityOrThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentTagId addTag(ContentTagCommand command) {
        command = validateTagCommand(command, false);
        ClassicsContentId contentId = ClassicsContentIdCodec.toDomain(command.contentId());
        ClassicsContentType contentType = command.contentType();
        requireWritable(contentType, contentId);
        int nextPriority = repository.maxTagPriority(null, null) + 1;
        ClassicsContentTag tag;
        if (tagBindingSupport == null) {
            tag = ClassicsContentApplicationAssembler.toTag(command);
        } else if (command.source() == ClassicsContentSource.AI) {
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
        command = validateTagCommand(command, true);
        ClassicsContentType contentType = command.contentType();
        ClassicsContentId contentId = ClassicsContentIdCodec.toDomain(command.contentId());
        requireWritable(contentType, contentId);
        ClassicsContentTag existing =
                repository.getTagById(command == null ? null : ClassicsContentTagIdCodec.toDomain(command.id()));
        if (existing == null) {
            throw new BizException("古籍内容标签不存在");
        }
        if (existing.getContentType() != contentType || !contentId.equals(existing.getContentId())) {
            throw new BizException("古籍内容标签不属于当前内容");
        }
        ClassicsContentTag tag = tagBindingSupport == null
                ? ClassicsContentApplicationAssembler.toTag(command)
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
        if (id == null) {
            throw new BizException("古籍内容标签 id 不能为空");
        }
        ClassicsContentTag existing = repository.getTagById(id);
        if (existing == null) {
            return;
        }
        requireWritable(existing.getContentType(), existing.getContentId());
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
        ClassicsContentQaPair qaPair = ClassicsContentApplicationAssembler.toQaPair(command);
        requireWritable(qaPair.getContentType(), qaPair.getContentId());
        qaPair.setId(null);
        qaPair.setPriority(repository.maxQaPairPriority() + 1);
        ClassicsContentQaPairId createdId = repository.insertQaPair(qaPair);
        versionAndPublishContentSync(
                qaPair.getContentType(), qaPair.getContentId(), ClassicsContentChangeType.QA_CHANGED, "新增问答对");
        return createdId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentQaPairId updateQaPair(ContentQaPairCommand command) {
        ClassicsContentQaPair qaPair = ClassicsContentApplicationAssembler.toQaPair(command);
        requireWritable(qaPair.getContentType(), qaPair.getContentId());
        repository.updateQaPair(qaPair);
        versionAndPublishContentSync(
                qaPair.getContentType(), qaPair.getContentId(), ClassicsContentChangeType.QA_CHANGED, "更新问答对");
        return qaPair.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortQaPairs(ContentQaPairSortCommand command) {
        sortQaPairs(command, repository.listQaPairs(SortDirection.ASC));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortQaPairs(String contentType, ClassicsContentId contentId, ContentQaPairSortCommand command) {
        sortQaPairs(command, repository.listQaPairs(contentType, contentId, SortDirection.ASC));
    }

    private void sortQaPairs(ContentQaPairSortCommand command, List<ClassicsContentQaPair> currentQaPairs) {
        currentQaPairs.stream()
                .filter(Objects::nonNull)
                .filter(pair -> pair.getContentType() != null && pair.getContentId() != null)
                .map(pair -> new ContentRef(pair.getContentType(), pair.getContentId()))
                .distinct()
                .forEach(ref -> requireWritable(ref.contentType(), ref.contentId()));
        List<ClassicsContentQaPairId> orderedIdList = command == null ? null : command.orderedIds();
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                currentQaPairs,
                ClassicsContentQaPair::getId,
                ClassicsContentQaPairId::value,
                ClassicsContentQaPair::getPriority,
                repository::maxQaPairPriority,
                this::updateQaPairPriorityOrThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQaPair(ClassicsContentQaPairId id) {
        ClassicsContentQaPair existing = repository.getQaPairById(id);
        if (existing != null) {
            requireWritable(existing.getContentType(), existing.getContentId());
        }
        repository.deleteQaPairById(id);
        if (existing != null && existing.getContentType() != null && existing.getContentId() != null) {
            versionAndPublishContentSync(
                    existing.getContentType(), existing.getContentId(), ClassicsContentChangeType.QA_CHANGED, "删除问答对");
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
        repository.lockContentForVersion(content.contentType(), content.contentId());
        if (!versioningSupport.needsVersion(content)) {
            return repository.latestVersion(content.contentType(), content.contentId());
        }

        ClassicsContentVersion version = versioningSupport.newVersion(
                content,
                versioningSupport.nextVersionNo(repository.latestVersionNo(content.contentType(), content.contentId())),
                Instant.now(),
                snapshotJson(content),
                changeType,
                changeSummary);
        ClassicsContentVersionId versionId = repository.insertVersion(version);
        version.setId(versionId);
        versioningSupport.markVersioned(content, version);
        return version;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsContentVersion applyAiResult(Versionable content, String changeSummary) {
        if (content != null) {
            requireWritable(content.contentType(), content.contentId());
        }
        return ensureVersioned(content, ClassicsContentChangeType.AI_APPLIED, changeSummary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiCandidateApplyContentResult applyAiCandidate(AiCandidateApplyContentCommand command) {
        if (command == null) {
            throw new BizException("AI候选应用参数不能为空");
        }
        if (command.candidateId() == null
                || command.contentType() == null
                || command.contentId() == null
                || StringUtils.isBlank(command.capability())
                || StringUtils.isBlank(command.resultFormat())
                || StringUtils.isBlank(command.resultPayload())) {
            throw new BizException("AI候选应用参数不完整");
        }

        ClassicsContentType contentType = command.contentType();
        ClassicsContentId contentId = ClassicsContentIdCodec.toDomain(command.contentId());
        requireWritable(contentType, contentId);

        if (aiFacade == null) {
            throw new BizException("AI候选服务未就绪");
        }
        aiFacade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                .candidateId(command.candidateId())
                .contentType(command.contentType().value())
                .contentId(command.contentId())
                .objectId(command.objectId())
                .capability(command.capability())
                .build());

        String capability = command.capability();
        String changeSummary = resolveChangeSummary(capability, command.changeSummary());
        Versionable content = null;

        if (contentType == ClassicsContentType.SANCAI_ENTRY) {
            SancaiEntry entry = repository.getSancaiEntryForAiApply(contentId);
            if (entry == null) {
                throw new BizException("三才内容不存在: " + contentId.value());
            }
            if (AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE.equals(capability)) {
                applySancaiImageAnalysisCandidate(contentId, command);
            } else if (AI_CAPABILITY_CLASSICS_VISUAL_DESCRIBE.equals(capability)) {
                applySancaiVisualDescriptionCandidate(contentId, command);
            } else if (AI_CAPABILITY_CLASSICS_IMAGE_PROMPT_FUSION.equals(capability)) {
                applySancaiFusionCandidate(contentId, command);
            } else if (AI_CAPABILITY_CLASSICS_IMAGE_GENERATE.equals(capability)) {
                SancaiVisualAsset generatedAsset = applySancaiImageGenCandidate(contentId, command);
                markAiCandidateApplied(command);
                return new AiCandidateApplyContentResult(
                        contentType,
                        contentId.value(),
                        generatedAsset.getId() == null
                                ? null
                                : generatedAsset.getId().value(),
                        generatedAsset.getVersionNo());
            } else if (AI_CAPABILITY_CLASSICS_TRANSLATE.equals(capability)) {
                entry.setTranslationText(aiCandidatePayloadParser.parseText(command.resultPayload()));
                entry.setTranslationStatus(SancaiEntryTranslationStatus.READY);
                touchContentUpdatedAt(ClassicsContentType.SANCAI_ENTRY, entry);
                ensureUpdate(repository.updateSancaiEntryAiFields(entry), "更新三才内容失败");
            } else if (isSummaryTagsOrQaCapability(capability)) {
                applySummaryTagsAndQaFromAiCandidate(contentType, entry, capability, command, "更新三才内容失败");
            } else {
                throw new BizException("不支持的 AI 候选能力: " + capability);
            }
            if (!isVisualAssetCapability(capability)) {
                content = entry;
            }
        } else if (contentType == ClassicsContentType.WANGQI_DOCUMENT) {
            WangqiDocument document = repository.getWangqiDocumentForAiApply(contentId);
            if (document == null) {
                throw new BizException("王圻文档不存在: " + contentId.value());
            }
            if (isSummaryTagsOrQaCapability(capability)) {
                applySummaryTagsAndQaFromAiCandidate(contentType, document, capability, command, "更新王圻文档失败");
            } else {
                throw new BizException("不支持的 AI 候选能力: " + capability);
            }
            content = document;
        } else if (contentType == ClassicsContentType.MING_CUSTOMS) {
            MingCustomsEntry entry = repository.getMingCustomsEntryForAiApply(contentId);
            if (entry == null) {
                throw new BizException("明代习俗不存在: " + contentId.value());
            }
            if (isSummaryTagsOrQaCapability(capability)) {
                applySummaryTagsAndQaFromAiCandidate(contentType, entry, capability, command, "更新明代习俗失败");
            } else {
                throw new BizException("不支持的 AI 候选能力: " + capability);
            }
            content = entry;
        } else {
            throw new BizException("未知的内容类型: " + contentType);
        }

        if (content == null) {
            if (isVisualAssetCapability(capability)) {
                markAiCandidateApplied(command);
                return new AiCandidateApplyContentResult(contentType, contentId.value(), null, null);
            }
            throw new BizException("内容不存在");
        }
        ClassicsContentVersion version = applyAiResult(content, changeSummary);
        persistVersionMarkers(content);
        markAiCandidateApplied(command);
        return new AiCandidateApplyContentResult(
                contentType,
                contentId.value(),
                version == null
                        ? null
                        : version.getId() == null ? null : version.getId().value(),
                version == null ? null : version.getVersionNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult applyAiCandidates(AiCandidateBatchApplyContentCommand command) {
        if (command == null || command.items() == null || command.items().isEmpty()) {
            throw new BizException("批量应用AI候选参数为空");
        }

        List<ClassicsBatchOperationItemResult> successes = new ArrayList<>();
        List<ClassicsBatchOperationItemResult> failures = new ArrayList<>();

        for (AiCandidateApplyContentCommand item : command.items()) {
            ClassicsContentType contentType = item == null ? null : item.contentType();
            Long contentId = item == null ? null : item.contentId();
            Long candidateId = item == null ? null : item.candidateId();
            Long objectId = item == null ? null : item.objectId();
            String capability = item == null ? null : item.capability();

            if (!hasEditPermission(contentType)) {
                failures.add(ClassicsBatchOperationItemResult.failureForCandidate(
                        typeValue(contentType),
                        contentId,
                        FAILURE_PERMISSION_DENIED,
                        "当前用户无权编辑该内容",
                        candidateId,
                        objectId,
                        capability));
                continue;
            }

            if (aiFacade == null) {
                failures.add(ClassicsBatchOperationItemResult.failureForCandidate(
                        typeValue(contentType),
                        contentId,
                        FAILURE_UNKNOWN,
                        "AI候选服务未就绪",
                        candidateId,
                        objectId,
                        capability));
                continue;
            }

            try {
                if (item != null) {
                    requirePendingAiCandidate(item);
                }

                AiCandidateApplyContentResult result = applyAiCandidate(item);
                successes.add(ClassicsBatchOperationItemResult.successForCandidate(
                        typeValue(contentType),
                        contentId,
                        result == null ? null : result.getVersionId(),
                        APPLIED_STATUS,
                        candidateId,
                        objectId,
                        capability));
            } catch (RuntimeException ex) {
                failures.add(ClassicsBatchOperationItemResult.failureForCandidate(
                        typeValue(contentType),
                        contentId,
                        resolveApplyFailureCode(ex),
                        ex.getMessage(),
                        candidateId,
                        objectId,
                        capability));
            }
        }

        return ClassicsBatchOperationResult.of(successes, failures);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassicsBatchOperationResult rejectAiCandidates(AiCandidateBatchRejectContentCommand command) {
        if (command == null || command.items() == null || command.items().isEmpty()) {
            throw new BizException("批量拒绝AI候选参数为空");
        }

        List<ClassicsBatchOperationItemResult> successes = new ArrayList<>();
        List<ClassicsBatchOperationItemResult> failures = new ArrayList<>();
        String errorType = resolveRejectErrorType(command.errorType());
        String errorMessage = resolveRejectErrorMessage(command.errorMessage());

        for (AiCandidateBatchRejectContentItemCommand item : command.items()) {
            ClassicsContentType contentType = item == null ? null : item.contentType();
            Long contentId = item == null ? null : item.contentId();
            Long candidateId = item == null ? null : item.candidateId();
            Long objectId = item == null ? null : item.objectId();
            String capability = item == null ? null : item.capability();

            if (!hasEditPermission(contentType)) {
                failures.add(ClassicsBatchOperationItemResult.failureForCandidate(
                        typeValue(contentType),
                        contentId,
                        FAILURE_PERMISSION_DENIED,
                        "当前用户无权编辑该内容",
                        candidateId,
                        objectId,
                        capability));
                continue;
            }

            if (aiFacade == null) {
                failures.add(ClassicsBatchOperationItemResult.failureForCandidate(
                        typeValue(contentType),
                        contentId,
                        FAILURE_UNKNOWN,
                        "AI候选服务未就绪",
                        candidateId,
                        objectId,
                        capability));
                continue;
            }

            try {
                AiCandidateFacadeDto candidate = requirePendingAiCandidate(item);
                aiFacade.rejectCandidate(RejectAiCandidateFacadeRequest.builder()
                        .candidateId(candidate.getCandidateId())
                        .errorType(errorType)
                        .errorMessage(errorMessage)
                        .build());
                successes.add(ClassicsBatchOperationItemResult.successForCandidate(
                        typeValue(contentType),
                        contentId,
                        candidate.getCandidateId(),
                        REJECTED_STATUS,
                        candidateId,
                        objectId,
                        capability));
            } catch (RuntimeException ex) {
                failures.add(ClassicsBatchOperationItemResult.failureForCandidate(
                        typeValue(contentType),
                        contentId,
                        resolveRejectFailureCode(ex),
                        ex.getMessage(),
                        candidateId,
                        objectId,
                        capability));
            }
        }

        return ClassicsBatchOperationResult.of(successes, failures);
    }

    private boolean hasEditPermission(ClassicsContentType contentType) {
        if (contentType == null) {
            return false;
        }
        return ClassicsContentPermissionSupport.canEdit(contentType, KuzhambuContextHolder.currentAuthorities());
    }

    private AiCandidateFacadeDto requirePendingAiCandidate(AiCandidateApplyContentCommand command) {
        return aiFacade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                .candidateId(command.candidateId())
                .contentType(
                        command.contentType() == null
                                ? null
                                : command.contentType().value())
                .contentId(command.contentId())
                .objectId(command.objectId())
                .capability(command.capability())
                .build());
    }

    private AiCandidateFacadeDto requirePendingAiCandidate(AiCandidateBatchRejectContentItemCommand item) {
        return aiFacade.requirePendingCandidate(RequirePendingAiCandidateFacadeRequest.builder()
                .candidateId(item.candidateId())
                .contentType(
                        item.contentType() == null ? null : item.contentType().value())
                .contentId(item.contentId())
                .objectId(item.objectId())
                .capability(item.capability())
                .build());
    }

    private String typeValue(ClassicsContentType contentType) {
        return contentType == null ? null : contentType.value();
    }

    private String resolveApplyFailureCode(RuntimeException ex) {
        if (ex instanceof DomainException) {
            return resolveFailureCodeFromDomainException((DomainException) ex);
        }
        if (ex instanceof BizException) {
            BizException bizException = (BizException) ex;
            if (FAILURE_PERMISSION_DENIED.equals(bizException.getCode())) {
                return FAILURE_PERMISSION_DENIED;
            }
            return resolveFailureCodeFromMessage(bizException.getMessage());
        }
        return FAILURE_UNKNOWN;
    }

    private String resolveRejectFailureCode(RuntimeException ex) {
        if (ex instanceof DomainException) {
            return resolveFailureCodeFromDomainException((DomainException) ex);
        }
        if (ex instanceof BizException) {
            BizException bizException = (BizException) ex;
            if (FAILURE_PERMISSION_DENIED.equals(bizException.getCode())) {
                return FAILURE_PERMISSION_DENIED;
            }
            return resolveFailureCodeFromMessage(bizException.getMessage());
        }
        return FAILURE_UNKNOWN;
    }

    private String resolveFailureCodeFromDomainException(DomainException ex) {
        String messageKey = ex.getMessageKey();
        return switch (messageKey) {
            case "ai.candidate.not-pending" -> FAILURE_CANDIDATE_NOT_PENDING;
            case "ai.candidate.target-mismatch" -> FAILURE_CANDIDATE_TARGET_MISMATCH;
            case "ai.candidate.not-found" -> FAILURE_CONTENT_NOT_FOUND;
            default -> FAILURE_UNKNOWN;
        };
    }

    private String resolveFailureCodeFromMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return FAILURE_UNKNOWN;
        }
        if (message.contains("不支持的 AI 候选能力")) {
            return FAILURE_UNSUPPORTED_CAPABILITY;
        }
        if (message.contains("不存在") && message.contains("内容") && !message.contains("候选")) {
            return FAILURE_CONTENT_NOT_FOUND;
        }
        if (message.contains("参数")
                || message.contains("不能为空")
                || message.contains("不完整")
                || message.contains("解析")
                || message.contains("无效")
                || message.contains("缺少")) {
            return FAILURE_VALIDATION_FAILED;
        }
        if (message.contains("三才视觉资产") && (message.contains("不存在") || message.contains("标识不存在"))) {
            return FAILURE_CANDIDATE_TARGET_MISMATCH;
        }
        return FAILURE_UNKNOWN;
    }

    private String resolveRejectErrorType(String errorType) {
        return StringUtils.isBlank(errorType) ? DEFAULT_REJECT_ERROR_TYPE : errorType;
    }

    private String resolveRejectErrorMessage(String errorMessage) {
        return StringUtils.isBlank(errorMessage) ? DEFAULT_REJECT_ERROR_MESSAGE : errorMessage;
    }

    private SancaiVisualAsset findVisualAsset(ClassicsContentId contentId, Long objectId) {
        if (sancaiAssetApplicationService == null || contentId == null || objectId == null) {
            return null;
        }
        return sancaiAssetApplicationService.listVisualAssets(SancaiEntryIdCodec.toDomain(contentId.value())).stream()
                .filter(visualAsset -> visualAsset != null
                        && visualAsset.getId() != null
                        && objectId.equals(visualAsset.getId().value()))
                .findFirst()
                .orElse(null);
    }

    private void applySancaiImageAnalysisCandidate(
            ClassicsContentId contentId, AiCandidateApplyContentCommand command) {
        SancaiVisualAsset visualAsset = requireSancaiVisualAsset(contentId, command);
        visualAsset.setImageAnalysisMarkdown(parseRequiredCandidateText(command, "图片理解"));
        sancaiAssetApplicationService.updateVisualAsset(visualAsset);
    }

    private void applySancaiVisualDescriptionCandidate(
            ClassicsContentId contentId, AiCandidateApplyContentCommand command) {
        SancaiVisualAsset visualAsset = requireSancaiVisualAsset(contentId, command);
        visualAsset.setVisualDescription(parseRequiredCandidateText(command, "视觉描述"));
        sancaiAssetApplicationService.updateVisualAsset(visualAsset);
    }

    private void applySancaiFusionCandidate(ClassicsContentId contentId, AiCandidateApplyContentCommand command) {
        SancaiVisualAsset visualAsset = requireSancaiVisualAsset(contentId, command);
        sancaiAssetApplicationService.applyFusionDescription(
                SancaiEntryIdCodec.toDomain(contentId.value()),
                visualAsset.getId(),
                parseRequiredCandidateText(command, "信息融合"));
    }

    private SancaiVisualAsset applySancaiImageGenCandidate(
            ClassicsContentId contentId, AiCandidateApplyContentCommand command) {
        SancaiVisualAsset visualAsset = requireSancaiVisualAsset(contentId, command);
        return sancaiAssetApplicationService.createGeneratedVisualAssetVersion(
                SancaiEntryIdCodec.toDomain(contentId.value()),
                visualAsset.getId(),
                StorageObjectIdCodec.toDomain(parseGeneratedStorageObjectId(command)));
    }

    private SancaiVisualAsset requireSancaiVisualAsset(
            ClassicsContentId contentId, AiCandidateApplyContentCommand command) {
        if (command == null || command.objectId() == null) {
            throw new BizException("三才视觉资产候选应用参数不完整");
        }
        if (sancaiAssetApplicationService == null) {
            throw new BizException("三才图片服务未就绪");
        }
        SancaiVisualAsset visualAsset = findVisualAsset(contentId, command.objectId());
        if (visualAsset == null) {
            throw new BizException("三才视觉资产不存在: " + command.objectId());
        }
        if (visualAsset.getId() == null) {
            throw new BizException("三才视觉资产标识不存在: " + command.objectId());
        }
        return visualAsset;
    }

    private String parseRequiredCandidateText(AiCandidateApplyContentCommand command, String capabilityLabel) {
        try {
            return aiCandidatePayloadParser.parseText(command.resultPayload());
        } catch (BizException ex) {
            throw new BizException("AI候选" + capabilityLabel + "结果不可用: " + ex.getMessage());
        }
    }

    private Long parseGeneratedStorageObjectId(AiCandidateApplyContentCommand command) {
        try {
            return aiCandidatePayloadParser.parseStorageObjectId(command.resultPayload());
        } catch (BizException ex) {
            throw new BizException("AI候选生图结果不可用: " + ex.getMessage());
        }
    }

    private void applySummaryTagsAndQaFromAiCandidate(
            ClassicsContentType contentType,
            Versionable content,
            String capability,
            AiCandidateApplyContentCommand command,
            String updateFailureMessage) {
        String resultPayload = command == null ? null : command.resultPayload();
        String summary = resolveSummaryIfPresent(resultPayload);
        List<String> tags = aiCandidatePayloadParser.parseTagsIfPresent(resultPayload);
        List<AiCandidateQaPairPayload> qaPairs = aiCandidatePayloadParser.parseQaPairsIfPresent(resultPayload);

        if (summary == null && tags.isEmpty() && qaPairs.isEmpty()) {
            throw new BizException("AI候选内容为空");
        }

        if (summary != null) {
            if (content instanceof SancaiEntry entry) {
                entry.setSummary(summary);
            } else if (content instanceof WangqiDocument document) {
                document.setSummary(summary);
            } else if (content instanceof MingCustomsEntry entry) {
                entry.setSummary(summary);
            }
        }

        if (AI_CAPABILITY_CLASSICS_TAGS.equals(capability)) {
            if (tags == null || tags.isEmpty()) {
                throw new BizException("AI候选标签为空");
            }
            applyTags(contentType, content, tags, command);
        } else if (AI_CAPABILITY_CLASSICS_QA.equals(capability)) {
            if (qaPairs == null || qaPairs.isEmpty()) {
                throw new BizException("AI候选问答为空");
            }
            applyQaPairs(contentType, content, qaPairs);
        } else {
            if (!tags.isEmpty()) {
                applyTags(contentType, content, tags, command);
            }
            if (!qaPairs.isEmpty()) {
                applyQaPairs(contentType, content, qaPairs);
            }
        }

        touchContentUpdatedAt(contentType, content);
        if (content instanceof SancaiEntry entry) {
            ensureUpdate(repository.updateSancaiEntryAiFields(entry), updateFailureMessage);
        } else if (content instanceof WangqiDocument document) {
            ensureUpdate(repository.updateWangqiDocumentAiFields(document), updateFailureMessage);
        } else if (content instanceof MingCustomsEntry entry) {
            ensureUpdate(repository.updateMingCustomsEntryAiFields(entry), updateFailureMessage);
        }
    }

    private boolean shouldAppendAiTags(AiCandidateApplyContentCommand command) {
        return command != null && TAG_APPLY_MODE_APPEND.equalsIgnoreCase(command.tagApplyMode());
    }

    private boolean shouldCoverContentTags(AiCandidateApplyContentCommand command) {
        return command != null && TAG_APPLY_MODE_COVER.equalsIgnoreCase(command.tagApplyMode());
    }

    private String resolveSummaryIfPresent(String resultPayload) {
        try {
            return aiCandidatePayloadParser.parseSummary(resultPayload);
        } catch (BizException ex) {
            if ("AI候选内容为空".equals(ex.getMessage())) {
                return null;
            }
            if ("AI候选摘要格式错误".equals(ex.getMessage())) {
                throw ex;
            }
            return null;
        }
    }

    private void markAiCandidateApplied(AiCandidateApplyContentCommand command) {
        aiFacade.markCandidateApplied(MarkAiCandidateAppliedFacadeRequest.builder()
                .candidateId(command.candidateId())
                .resultFormat(command.resultFormat())
                .resultPayload(command.resultPayload())
                .appliedAt(Instant.now())
                .build());
    }

    private String resolveChangeSummary(String capability, String changeSummary) {
        if (StringUtils.isNotBlank(changeSummary)) {
            return changeSummary;
        }
        return switch (capability) {
            case AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE -> "AI 应用：图片理解";
            case AI_CAPABILITY_CLASSICS_IMAGE_GENERATE -> "AI 应用：生图";
            case AI_CAPABILITY_CLASSICS_VISUAL_DESCRIBE -> "AI 应用：视觉描述";
            case AI_CAPABILITY_CLASSICS_IMAGE_PROMPT_FUSION -> "AI 应用：信息融合";
            case AI_CAPABILITY_CLASSICS_TRANSLATE -> "AI 应用：译文";
            case AI_CAPABILITY_CLASSICS_SUMMARY -> "AI 应用：摘要";
            case AI_CAPABILITY_CLASSICS_TAGS -> "AI 应用：标签";
            case AI_CAPABILITY_CLASSICS_QA -> "AI 应用：问答对";
            default -> throw new BizException("不支持的 AI 候选能力: " + capability);
        };
    }

    private boolean isSummaryTagsOrQaCapability(String capability) {
        return AI_CAPABILITY_CLASSICS_SUMMARY.equals(capability)
                || AI_CAPABILITY_CLASSICS_TAGS.equals(capability)
                || AI_CAPABILITY_CLASSICS_QA.equals(capability);
    }

    private boolean isVisualAssetCapability(String capability) {
        return AI_CAPABILITY_CLASSICS_IMAGE_DESCRIBE.equals(capability)
                || AI_CAPABILITY_CLASSICS_VISUAL_DESCRIBE.equals(capability)
                || AI_CAPABILITY_CLASSICS_IMAGE_PROMPT_FUSION.equals(capability)
                || AI_CAPABILITY_CLASSICS_IMAGE_GENERATE.equals(capability);
    }

    private void applyTags(
            ClassicsContentType contentType,
            Versionable content,
            List<String> tags,
            AiCandidateApplyContentCommand command) {
        ClassicsContentId contentId = content == null ? null : content.contentId();
        if (contentId == null) {
            throw new BizException("标签应用目标内容不存在");
        }
        if (tags == null || tags.isEmpty()) {
            throw new BizException("AI候选标签为空");
        }
        boolean appendMode = shouldAppendAiTags(command);
        boolean coverMode = shouldCoverContentTags(command);
        Set<String> existingTagNames = new HashSet<>();
        List<ClassicsContentTag> currentTags =
                new ArrayList<>(repository.listTags(contentType.value(), contentId, SortDirection.ASC));
        if (appendMode) {
            currentTags.stream()
                    .filter(Objects::nonNull)
                    .filter(tag -> tag.getStatus() == null || tag.getStatus() == ClassicsContentTagStatus.ACTIVE)
                    .map(ClassicsContentTag::getTagNameSnapshot)
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(existingTagNames::add);
        } else if (coverMode) {
            for (ClassicsContentTag tag : currentTags) {
                if (tagBindingSupport != null) {
                    tagBindingSupport.removeTagRef(tag);
                }
                if (tag != null && tag.getId() != null) {
                    repository.deleteTagById(contentType.value(), contentId, tag.getId());
                }
            }
        } else {
            if (tagBindingSupport != null) {
                currentTags.stream()
                        .filter(tag -> tag != null && tag.getSource() == ClassicsContentSource.AI)
                        .forEach(tagBindingSupport::removeTagRef);
            }
            repository.deleteAiTags(contentType.value(), contentId);
        }
        for (String tagName : tags) {
            if (StringUtils.isBlank(tagName)) {
                continue;
            }
            String normalizedTagName = tagName.trim();
            if (appendMode && !existingTagNames.add(normalizedTagName.toLowerCase())) {
                continue;
            }
            insertTagWithoutVersion(new ContentTagCommand(
                    null,
                    contentType,
                    contentId.value(),
                    null,
                    normalizedTagName,
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
        Set<String> existingQaPairKeys =
                repository.listQaPairs(contentType.value(), contentId, SortDirection.ASC).stream()
                        .filter(Objects::nonNull)
                        .map(pair -> qaPairKey(pair.getQuestion(), pair.getAnswer()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        for (AiCandidateQaPairPayload pair : qaPairs) {
            if (pair == null || StringUtils.isBlank(pair.getQuestion()) || StringUtils.isBlank(pair.getAnswer())) {
                continue;
            }
            String question = pair.getQuestion().trim();
            String answer = pair.getAnswer().trim();
            if (!existingQaPairKeys.add(qaPairKey(question, answer))) {
                continue;
            }
            insertQaPairWithoutVersion(new ContentQaPairCommand(
                    null, contentType, contentId.value(), question, answer, ClassicsContentSource.AI));
        }
        touchContentUpdatedAt(contentType, content);
    }

    private String qaPairKey(String question, String answer) {
        if (StringUtils.isBlank(question) || StringUtils.isBlank(answer)) {
            return null;
        }
        return question.trim().toLowerCase() + "\n" + answer.trim().toLowerCase();
    }

    private ClassicsContentTagId insertTagWithoutVersion(ContentTagCommand command) {
        ClassicsContentId contentId = ClassicsContentIdCodec.toDomain(command.contentId());
        int nextPriority = repository.maxTagPriority(null, null) + 1;
        ClassicsContentTag tag;
        if (tagBindingSupport == null) {
            tag = ClassicsContentApplicationAssembler.toTag(command);
        } else if (command.source() == ClassicsContentSource.AI) {
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
        ClassicsContentQaPair qaPair = ClassicsContentApplicationAssembler.toQaPair(command);
        qaPair.setId(null);
        qaPair.setPriority(repository.maxQaPairPriority() + 1);
        return repository.insertQaPair(qaPair);
    }

    private void touchContentUpdatedAt(ClassicsContentType contentType, Versionable content) {
        if (content == null) {
            return;
        }
        Instant now = Instant.now();
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
        versionAndPublishContentSync(contentType, contentId, ClassicsContentChangeType.MANUAL_SAVE, changeSummary);
    }

    private void versionAndPublishContentSync(
            ClassicsContentType contentType,
            ClassicsContentId contentId,
            ClassicsContentChangeType changeType,
            String changeSummary) {
        Versionable content = loadContentForGovernance(contentType, contentId);
        if (content == null) {
            return;
        }
        touchContentUpdatedAt(contentType, content);
        ensureVersioned(content, changeType, changeSummary);
        persistVersionMarkers(content);
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
        requireWritable(version.getContentType(), version.getContentId());
        if (version.getContentType() == ClassicsContentType.WANGQI_DOCUMENT) {
            Versionable restored = wangqiDocumentVersionRestorer.restoreSnapshot(version);
            ClassicsContentVersion restoredVersion = createRestoredVersion(restored, version);
            wangqiDocumentVersionRestorer.markVersioned((WangqiDocument) restored);
            return restoredVersion;
        }
        if (version.getContentType() == ClassicsContentType.MING_CUSTOMS) {
            Versionable restored = restoreMingCustomsFromSnapshot(version);
            ClassicsContentVersion restoredVersion = createRestoredVersion(restored, version);
            persistVersionMarkers(restored);
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

    private void requireWritable(ClassicsContentType contentType, ClassicsContentId contentId) {
        publicationWriteGuard.requireWritable(contentType, contentId, ClassicsPublicationWriteOperation.EDIT);
    }

    private record ContentRef(ClassicsContentType contentType, ClassicsContentId contentId) {}

    private Versionable restoreMingCustomsFromSnapshot(ClassicsContentVersion version) {
        MingCustomsEntry entry = repository.getMingCustomsEntryForAiApply(version.getContentId());
        if (entry == null) {
            throw new BizException("明代习俗不存在");
        }

        MingCustomsVersionSnapshot snapshot = parseMingCustomsVersionSnapshot(version);
        if (!ClassicsContentType.MING_CUSTOMS.value().equals(snapshot.contentType())
                || !Objects.equals(version.getContentId(), ClassicsContentIdCodec.toDomain(snapshot.contentId()))) {
            throw new BizException("历史版本快照不属于当前明代习俗条目");
        }

        entry.setTitle(snapshot.title());
        entry.setCategory(snapshot.category());
        entry.setChapter(snapshot.chapter());
        entry.setSection(snapshot.section());
        entry.setSummary(snapshot.summary());
        entry.setContentFormat(resolveMingCustomsContentFormat(snapshot.contentFormat()));
        entry.setContent(snapshot.content());
        entry.setOriginalExcerpts(snapshot.originalExcerpts());
        touchContentUpdatedAt(ClassicsContentType.MING_CUSTOMS, entry);
        restoreMingCustomsTags(entry, snapshot);
        restoreMingCustomsQaPairs(entry, snapshot);
        return entry;
    }

    private MingCustomsVersionSnapshot parseMingCustomsVersionSnapshot(ClassicsContentVersion version) {
        try {
            return MingCustomsVersionSnapshot.from(objectMapper.readTree(version.getSnapshotJson()));
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "CLASSICS-13005", "classics.content.version.snapshot-invalid", "历史版本快照不可解析", exception);
        }
    }

    private MingCustomsContentFormat resolveMingCustomsContentFormat(String value) {
        return StringUtils.isBlank(value) ? null : MingCustomsContentFormat.from(value);
    }

    private void restoreMingCustomsTags(MingCustomsEntry entry, MingCustomsVersionSnapshot snapshot) {
        if (entry == null || snapshot == null || entry.contentId() == null) {
            return;
        }
        repository
                .listTags(ClassicsContentType.MING_CUSTOMS.value(), entry.contentId(), SortDirection.ASC)
                .forEach(tag -> {
                    removeTagRefIfExists(tag);
                    repository.deleteTagById(ClassicsContentType.MING_CUSTOMS.value(), entry.contentId(), tag.getId());
                });

        if (snapshot.tags() == null) {
            return;
        }

        int priority = repository.maxTagPriority(null, null) + 1;
        for (int i = 0; i < snapshot.tags().size(); i++) {
            insertMingTagFromSnapshot(snapshot.tags().get(i), entry, priority++);
        }
    }

    private void restoreMingCustomsQaPairs(MingCustomsEntry entry, MingCustomsVersionSnapshot snapshot) {
        if (entry == null || entry.contentId() == null) {
            return;
        }
        repository
                .listQaPairs(ClassicsContentType.MING_CUSTOMS.value(), entry.contentId(), SortDirection.ASC)
                .forEach(pair -> repository.deleteQaPairById(pair.getId()));

        if (snapshot == null || snapshot.qaPairs() == null) {
            return;
        }
        int priority = repository.maxQaPairPriority() + 1;
        for (int i = 0; i < snapshot.qaPairs().size(); i++) {
            insertMingQaPairFromSnapshot(snapshot.qaPairs().get(i), entry, priority++);
        }
    }

    private void insertMingTagFromSnapshot(
            MingCustomsVersionSnapshot.MingCustomsTagSnapshot snapshot, MingCustomsEntry entry, int fallbackPriority) {
        if (snapshot == null || entry == null || entry.contentId() == null) {
            return;
        }
        int priority = fallbackPriority;
        if (tagBindingSupport == null) {
            ContentTagCommand command = new ContentTagCommand(
                    null,
                    ClassicsContentType.MING_CUSTOMS,
                    entry.contentId().value(),
                    snapshot.tagId(),
                    snapshot.tagNameSnapshot(),
                    parseSource(snapshot.source()),
                    parseTagStatus(snapshot.status()));
            ClassicsContentTag tag = ClassicsContentApplicationAssembler.toTag(command);
            tag.setPriority(priority);
            tag.setId(null);
            repository.insertTag(tag);
            return;
        }
        if (snapshot.tagId() == null && StringUtils.isBlank(snapshot.tagNameSnapshot())) {
            return;
        }
        ClassicsContentTag tag = commandForRestoredTag(snapshot, entry, priority);
        if (tag == null) {
            return;
        }
        repository.insertTag(tag);
        tagBindingSupport.syncTagRef(tag);
    }

    private ClassicsContentTag commandForRestoredTag(
            MingCustomsVersionSnapshot.MingCustomsTagSnapshot snapshot, MingCustomsEntry entry, int priority) {
        ClassicsContentSource source = parseSource(snapshot.source());
        ClassicsContentTagStatus status = parseTagStatus(snapshot.status());
        ContentTagCommand command = new ContentTagCommand(
                null,
                ClassicsContentType.MING_CUSTOMS,
                entry.contentId().value(),
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

    private void insertMingQaPairFromSnapshot(
            MingCustomsVersionSnapshot.MingCustomsQaPairSnapshot snapshot,
            MingCustomsEntry entry,
            int fallbackPriority) {
        if (snapshot == null || entry == null || entry.contentId() == null) {
            return;
        }
        if (StringUtils.isBlank(snapshot.question()) && StringUtils.isBlank(snapshot.answer())) {
            return;
        }
        ContentQaPairCommand command = new ContentQaPairCommand(
                null,
                ClassicsContentType.MING_CUSTOMS,
                entry.contentId().value(),
                snapshot.question(),
                snapshot.answer(),
                parseSource(snapshot.source()));
        ClassicsContentQaPair qaPair = ClassicsContentApplicationAssembler.toQaPair(command);
        qaPair.setId(null);
        qaPair.setPriority(fallbackPriority);
        repository.insertQaPair(qaPair);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ClassicsExportJobResult createExportJob(ContentExportCommand command) {
        requirePrivateExportPermission(command);
        ClassicsContentExportJob job = ClassicsContentApplicationAssembler.toExportJob(command);
        if (job.getRequestedAt() == null) {
            job.setRequestedAt(Instant.now());
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
                            ? payloadItemCount(job.getScopeJson())
                            : response.getSummary().getItemCount();
            repository.markExportJobCompleted(
                    jobId,
                    storageObjectId,
                    job.getRequestedAt().plus(Duration.ofDays(EXPORT_EXPIRES_DAYS)),
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
                || job.getExpiresAt().isBefore(Instant.now())
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExportJob(ClassicsContentExportJobId id) {
        if (id == null) {
            return;
        }
        ClassicsContentExportJob job = repository.getExportJobById(id);
        if (job == null) {
            return;
        }
        unbindExportArtifactOwner(id);
        repository.deleteExportJobById(id);
        removeStorageObjectIfUnreferenced(job.getStorageObjectId());
    }

    private static ContentTagCommand validateTagCommand(ContentTagCommand command, boolean requireId) {
        if (command == null) {
            throw new BizException("古籍内容标签参数不能为空");
        }
        if (requireId && command.id() == null) {
            throw new BizException("古籍内容标签 id 不能为空");
        }
        requireTagScope(
                command.contentType() == null ? null : command.contentType().value(),
                ClassicsContentIdCodec.toDomain(command.contentId()));
        String tagName = StringUtils.trimToNull(command.tagNameSnapshot());
        if (tagName == null) {
            throw new BizException("古籍内容标签名称不能为空");
        }
        return new ContentTagCommand(
                command.id(),
                command.contentType(),
                command.contentId(),
                command.tagId(),
                tagName,
                command.source() == null ? ClassicsContentSource.MANUAL : command.source(),
                command.status() == null ? ClassicsContentTagStatus.ACTIVE : command.status());
    }

    private static void requireTagScope(String contentType, ClassicsContentId contentId) {
        if (StringUtils.isBlank(contentType)) {
            throw new BizException("古籍内容标签 contentType 不能为空");
        }
        if (contentId == null || contentId.value() == null) {
            throw new BizException("古籍内容标签 contentId 不能为空");
        }
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
        repository.lockContentForVersion(content.contentType(), content.contentId());
        ClassicsContentVersion version = versioningSupport.newVersion(
                content,
                versioningSupport.nextVersionNo(repository.latestVersionNo(content.contentType(), content.contentId())),
                Instant.now(),
                snapshotJson(content),
                ClassicsContentChangeType.HISTORY_RESTORED,
                "恢复历史版本 v" + restoredFrom.getVersionNo());
        ClassicsContentVersionId versionId = repository.insertVersion(version);
        version.setId(versionId);
        versioningSupport.markVersioned(content, version);
        return version;
    }

    private String snapshotJson(Versionable content) {
        if (content instanceof WangqiDocument) {
            List<ClassicsContentTag> tags = repository.listTags(
                    ClassicsContentType.WANGQI_DOCUMENT.value(), content.contentId(), SortDirection.ASC);
            List<ClassicsContentQaPair> qaPairs = repository.listQaPairs(
                    ClassicsContentType.WANGQI_DOCUMENT.value(), content.contentId(), SortDirection.ASC);
            return snapshotAssembler.toSnapshotJson(content, tags, qaPairs);
        }
        if (content instanceof MingCustomsEntry) {
            List<ClassicsContentTag> tags = repository.listTags(
                    ClassicsContentType.MING_CUSTOMS.value(), content.contentId(), SortDirection.ASC);
            List<ClassicsContentQaPair> qaPairs = repository.listQaPairs(
                    ClassicsContentType.MING_CUSTOMS.value(), content.contentId(), SortDirection.ASC);
            return snapshotAssembler.toSnapshotJson(content, tags, qaPairs);
        }
        if (content instanceof SancaiEntry entry && sancaiAssetApplicationService != null) {
            List<SancaiEntryImage> images = sancaiAssetApplicationService.listImages(entry.getId());
            return sancaiSnapshotJson(entry, images);
        }
        return snapshotAssembler.toSnapshotJson(content);
    }

    private String sancaiSnapshotJson(SancaiEntry entry, List<SancaiEntryImage> images) {
        SancaiVolume volume = sancaiRepository == null || entry.getVolumeId() == null
                ? null
                : sancaiRepository.getVolumeById(entry.getVolumeId());
        SancaiCategory category = sancaiRepository == null || volume == null || volume.getCategoryId() == null
                ? null
                : sancaiRepository.getCategoryById(volume.getCategoryId());
        List<ClassicsContentTag> tags =
                repository.listTags(ClassicsContentType.SANCAI_ENTRY.value(), entry.contentId(), SortDirection.ASC);
        List<ClassicsContentQaPair> qaPairs =
                repository.listQaPairs(ClassicsContentType.SANCAI_ENTRY.value(), entry.contentId(), SortDirection.ASC);
        return snapshotAssembler.toSancaiSnapshotJson(
                entry,
                volume == null ? null : volume.getTitle(),
                category == null || category.getId() == null
                        ? null
                        : category.getId().value(),
                category == null ? null : category.getTitle(),
                images.stream().map(this::toImageResource).toList(),
                tags,
                qaPairs);
    }

    private void removeTagRefIfExists(ClassicsContentTag tag) {
        if (tagBindingSupport == null || tag == null) {
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
        request.setInput(renderInput(job));
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

    private WorkerRenderDtos.Input renderInput(ClassicsContentExportJob job) {
        WorkerRenderDtos.Input input = new WorkerRenderDtos.Input();
        input.setSnapshotId(null);
        input.setContentType("CLASSICS_EXPORT_SNAPSHOT");
        input.setPayloadJson(renderPayloadJson(job));
        return input;
    }

    private String renderPayloadJson(ClassicsContentExportJob job) {
        JsonNode payload = normalizePayload(job);
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

    private JsonNode normalizePayload(ClassicsContentExportJob job) {
        JsonNode payload = parsePayload(job == null ? null : job.getScopeJson());
        if (payload.isObject()) {
            ensurePayloadDefaults((ObjectNode) payload, job);
            return payload;
        }
        ObjectNode wrapped = objectMapper.createObjectNode();
        wrapped.put("title", DEFAULT_TITLE);
        appendExportContext(wrapped, job);
        ArrayNode items = objectMapper.createArrayNode();
        items.add(payload == null ? "" : payload.asText());
        wrapped.set("items", items);
        return wrapped;
    }

    private void ensurePayloadDefaults(ObjectNode payload, ClassicsContentExportJob job) {
        if (!payload.has("title")) {
            payload.put("title", DEFAULT_TITLE);
        }
        appendExportContext(payload, job);
        if (!payload.has("items")) {
            payload.set("items", objectMapper.createArrayNode());
        }
        normalizeSancaiExportImages(payload, job);
    }

    private void appendExportContext(ObjectNode payload, ClassicsContentExportJob job) {
        if (job == null) {
            return;
        }
        if (!payload.has("contentType") && job.getContentType() != null) {
            payload.put("contentType", job.getContentType().value());
        }
        if (!payload.has("scopeType") && job.getScopeType() != null) {
            payload.put("scopeType", job.getScopeType().value());
        }
    }

    private int payloadItemCount(String scopeJson) {
        JsonNode payload = parsePayload(scopeJson);
        if (payload != null
                && payload.isObject()
                && payload.get("items") != null
                && payload.get("items").isArray()) {
            return payload.get("items").size();
        }
        return 0;
    }

    private void normalizeSancaiExportImages(ObjectNode payload, ClassicsContentExportJob job) {
        if (job == null || job.getContentType() != ClassicsContentType.SANCAI_ENTRY) {
            return;
        }
        JsonNode items = payload.get("items");
        if (items == null || !items.isArray()) {
            payload.set("items", objectMapper.createArrayNode());
            return;
        }
        for (JsonNode item : items) {
            if (item instanceof ObjectNode itemObject) {
                itemObject.set("images", normalizeSancaiExportImageArray(itemObject.get("images")));
            }
        }
    }

    private ArrayNode normalizeSancaiExportImageArray(JsonNode images) {
        ArrayNode normalized = objectMapper.createArrayNode();
        if (images == null || !images.isArray()) {
            return normalized;
        }
        List<ObjectNode> imageObjects = new ArrayList<>();
        for (JsonNode image : images) {
            ObjectNode imageObject =
                    image instanceof ObjectNode object ? object.deepCopy() : objectMapper.createObjectNode();
            normalizeSancaiExportImage(imageObject);
            imageObjects.add(imageObject);
        }
        imageObjects.stream()
                .sorted(Comparator.comparingInt(ClassicsContentApplicationServiceImpl::imagePriority))
                .forEach(normalized::add);
        return normalized;
    }

    private static void normalizeSancaiExportImage(ObjectNode image) {
        putNullIfMissing(image, "imageId");
        putNullIfMissing(image, "storageObjectId");
        putNullIfMissing(image, "imageType");
        putNullIfMissing(image, "title");
        putBooleanIfMissing(image, "currentUsed", false);
        putIntIfMissing(image, "priority", 0);
        putNullIfMissing(image, "originalFilename");
        putNullIfMissing(image, "contentType");
        putNullIfMissing(image, "size");
    }

    private static int imagePriority(ObjectNode image) {
        JsonNode priority = image.get("priority");
        return priority == null || !priority.canConvertToInt() ? 0 : priority.asInt();
    }

    private static void putNullIfMissing(ObjectNode object, String fieldName) {
        if (!object.has(fieldName)) {
            object.putNull(fieldName);
        }
    }

    private static void putBooleanIfMissing(ObjectNode object, String fieldName, boolean value) {
        if (!object.has(fieldName) || object.get(fieldName).isNull()) {
            object.put(fieldName, value);
        }
    }

    private static void putIntIfMissing(ObjectNode object, String fieldName, int value) {
        if (!object.has(fieldName) || object.get(fieldName).isNull()) {
            object.put(fieldName, value);
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
                        .ownerType(EXPORT_OWNER_TYPE)
                        .ownerId(exportOwnerId(jobId))
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
                .ownerId(exportOwnerId(jobId))
                .ownerType(EXPORT_OWNER_TYPE)
                .ownerParams("usage=CLASSICS_EXPORT_JOB;jobId=" + jobId.value())
                .build());
    }

    private void unbindExportArtifactOwner(ClassicsContentExportJobId jobId) {
        if (storageFacade == null || jobId == null) {
            return;
        }
        storageFacade.unbindOwner(UnbindStorageOwnerFacadeRequest.builder()
                .ownerType(EXPORT_OWNER_TYPE)
                .ownerId(exportOwnerId(jobId))
                .build());
    }

    private void removeStorageObjectIfUnreferenced(StorageObjectId storageObjectId) {
        if (storageFacade == null || storageObjectId == null || storageObjectId.value() == null) {
            return;
        }
        try {
            storageFacade.remove(RemoveStorageFacadeRequest.builder()
                    .storageObjectId(storageObjectId.value())
                    .build());
        } catch (BizException ignored) {
            // Other business references keep the object alive; deleting the Classics record still succeeds.
        }
    }

    private static String exportOwnerId(ClassicsContentExportJobId jobId) {
        return EXPORT_OWNER_ID_PREFIX + (jobId == null ? "unknown" : jobId.value());
    }

    private StorageObjectId toStorageObjectId(UploadStorageFacadeResponse uploadResponse) {
        return uploadResponse == null || uploadResponse.getStorageObjectId() == null
                ? null
                : StorageObjectIdCodec.toDomain(uploadResponse.getStorageObjectId());
    }

    private byte[] artifactContent(WorkerRenderDtos.Artifact artifact) {
        if (artifact == null
                || artifact.getContent() == null
                || artifact.getContent().isBlank()) {
            return new byte[0];
        }
        validateArtifactSizeBeforeDecode(artifact);
        byte[] content;
        if ("TEXT".equalsIgnoreCase(artifact.getEncoding())) {
            content = artifact.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } else if ("BASE64".equalsIgnoreCase(artifact.getEncoding())) {
            content = Base64.getDecoder().decode(artifact.getContent());
        } else {
            throw new BizException("暂不支持的导出产物编码: " + artifact.getEncoding());
        }
        validateArtifactSize(content.length);
        return content;
    }

    private static void validateArtifactSizeBeforeDecode(WorkerRenderDtos.Artifact artifact) {
        if (artifact.getSizeBytes() != null) {
            validateArtifactSize(artifact.getSizeBytes());
        }
        if ("BASE64".equalsIgnoreCase(artifact.getEncoding())
                && artifact.getContent().length() > (MAX_RENDER_ARTIFACT_SIZE_BYTES * 4 / 3 + 4)) {
            throw new BizException("导出产物超过大小限制");
        }
    }

    private static void validateArtifactSize(long sizeBytes) {
        if (sizeBytes > MAX_RENDER_ARTIFACT_SIZE_BYTES) {
            throw new BizException("导出产物超过大小限制");
        }
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

    private static BizException sortDbFailure() {
        return new BizException(
                ErrorCode.SORT_DB_FAILURE.getCode(),
                ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                ErrorCode.SORT_DB_FAILURE.getMessage());
    }

    private static void requirePrivateExportPermission(ContentExportCommand command) {
        if (command == null || !containsPrivateContent(command.visibilityRiskStatus())) {
            return;
        }
        if (command.contentType() == null
                || !ClassicsContentPermissionSupport.canExport(command.contentType(), command.operatorPermissions())) {
            throw permissionDenied();
        }
    }

    private static boolean containsPrivateContent(SancaiVisibilityRiskStatus visibilityRiskStatus) {
        return visibilityRiskStatus != null && visibilityRiskStatus != SancaiVisibilityRiskStatus.PUBLIC_ONLY;
    }

    private static BizException permissionDenied() {
        return new BizException("PERMISSION_DENIED");
    }
}
