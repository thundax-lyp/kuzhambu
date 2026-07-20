package com.thundax.kuzhambu.classics.application.sancai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiShowcaseJobResult;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.domain.common.client.WorkerRenderClient;
import com.thundax.kuzhambu.classics.domain.common.client.dto.WorkerRenderDtos;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryDraftId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.exception.ErrorCode;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.common.core.sort.SortablePrioritySwapSupport;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.BindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.MarkStorageUsageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.RemoveStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UnbindStorageOwnerFacadeRequest;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class SancaiAssetApplicationServiceImpl implements SancaiAssetApplicationService {

    private static final String IMAGE_OWNER_ID_PREFIX = "entry:";
    private static final String IMAGE_OWNER_ID_SEPARATOR = ":image:";
    private static final String SHOWCASE_RENDER_OPERATION = "SANCAI_SHOWCASE";
    private static final String SHOWCASE_RENDER_TYPE = "SANCAI_SHOWCASE";
    private static final String SHOWCASE_RENDER_TEMPLATE_ID = "sancai-showcase-default";
    private static final String SHOWCASE_RENDER_TEMPLATE_VERSION = "2026.06.01";
    private static final String SHOWCASE_RENDER_OUTPUT_FORMAT = "HTML";
    private static final String SHOWCASE_RENDER_OUTPUT_FILENAME = "showcase.html";
    private static final String SHOWCASE_RENDER_CONTENT_TYPE = "SANCAI_SHOWCASE_SNAPSHOT";
    private static final String SHOWCASE_RENDER_LOCALE = "zh-CN";
    private static final String SHOWCASE_ARTIFACT_CONTENT_TYPE = "text/html";
    private static final String SHOWCASE_FAILURE_WORKER_UNAVAILABLE = "WORKER_UNAVAILABLE";
    private static final String SHOWCASE_FAILURE_RENDER_FAILED = "RENDER_OUTPUT_FAILURE";
    private static final String SHOWCASE_FAILURE_STORAGE_FAILED = "STORAGE_WRITE_FAILURE";
    private static final String SHOWCASE_FAILURE_INTERNAL = "INTERNAL_FAILURE";
    private static final String SHOWCASE_FAILURE_PRIVATE_UNCONFIRMED = "VISIBILITY_RISK_UNCONFIRMED";
    private static final int SHOWCASE_FAILURE_MESSAGE_MAX_LENGTH = 512;
    private static final long MAX_SHOWCASE_ARTIFACT_SIZE_BYTES = 50L * 1024L * 1024L;
    private static final String SANCAI_IMAGE_CONTENT_PATH_PREFIX = "/api/classics/sancai/assets/images/";
    private static final String SANCAI_IMAGE_CONTENT_PATH_SEPARATOR = "/";
    private static final String SANCAI_IMAGE_CONTENT_PATH_SUFFIX = "/content";
    private static final String IMAGE_OWNER_TYPE = "CLASSICS_SANCAI_ENTRY_IMAGE";
    private static final String SHOWCASE_OWNER_TYPE = "CLASSICS_SANCAI_SHOWCASE";
    private static final String SHOWCASE_OWNER_ID_PREFIX = "showcase:";
    private static final List<String> ALLOWED_IMAGE_SUFFIXES = List.of("jpg", "jpeg", "png", "gif", "webp");

    private final SancaiAssetRepository repository;
    private final WorkerRenderClient workerRenderClient;
    private final StorageFacade storageFacade;
    private final ObjectMapper objectMapper;

    public SancaiAssetApplicationServiceImpl(
            SancaiAssetRepository repository,
            WorkerRenderClient workerRenderClient,
            StorageFacade storageFacade,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.workerRenderClient = workerRenderClient;
        this.storageFacade = storageFacade;
        this.objectMapper = objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiEntryDraftId updateDraft(SancaiDraftCommand command) {
        SancaiEntryDraft draft = new SancaiEntryDraft();
        draft.setEntryId(SancaiEntryIdCodec.toDomain(command.getEntryId()));
        draft.setAutosavedAt(command.getAutosavedAt() == null ? new Date() : command.getAutosavedAt());
        draft.setDraftJson(command.getDraftJson());
        return repository.insertDraft(draft);
    }

    @Override
    public SancaiEntryDraft getLatestDraft(SancaiEntryId entryId) {
        return repository.getLatestDraftByEntryId(entryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiEntryImageId updateImage(SancaiImageCommand command) {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(SancaiEntryImageIdCodec.toDomain(command.getId()));
        image.setEntryId(SancaiEntryIdCodec.toDomain(command.getEntryId()));
        image.setStorageObjectId(command.getStorageObjectId());
        image.setImageType(command.getImageType());
        image.setTitle(command.getTitle());
        image.setCurrentUsed(command.isCurrentUsed());
        if (image.getId() == null) {
            image.setPriority(repository.maxPriority() + 1);
            return repository.insertImage(image);
        }
        repository.updateImage(image);
        return image.getId();
    }

    @Override
    public SancaiEntryImage getImage(SancaiEntryImageId id) {
        return id == null ? null : repository.getImageById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiEntryImageResource uploadImage(SancaiEntryImageUploadCommand command) {
        SancaiEntryId entryId = SancaiEntryIdCodec.toDomain(command == null ? null : command.getEntryId());
        validateImageUpload(command);

        UploadStorageFacadeResponse uploadResponse = storageFacade.upload(UploadStorageFacadeRequest.builder()
                .inputStream(command.getInputStream())
                .originalFilename(command.getOriginalFilename())
                .contentType(command.getContentType())
                .sizeBytes(command.getSize())
                .allowedSuffixes(ALLOWED_IMAGE_SUFFIXES)
                .ownerType(IMAGE_OWNER_TYPE)
                .build());
        SancaiEntryImage image = new SancaiEntryImage();
        image.setEntryId(entryId);
        image.setStorageObjectId(
                uploadResponse == null || uploadResponse.getStorageObjectId() == null
                        ? null
                        : StorageObjectId.of(uploadResponse.getStorageObjectId()));
        image.setImageType(command.getImageType());
        image.setTitle(command.getTitle());
        image.setCurrentUsed(command.isCurrentUsed());
        image.setPriority(repository.maxPriority() + 1);
        clearCurrentImagesIfNeeded(command, entryId);
        SancaiEntryImageId imageId = repository.insertImage(image);
        image.setId(imageId);

        bindStorageOwner(uploadResponse == null ? null : uploadResponse.getStorageObjectId(), entryId, imageId);
        return toResource(image, uploadResponse);
    }

    @Override
    public SancaiEntryImageContent getImageContent(SancaiEntryId entryId, SancaiEntryImageId imageId) {
        SancaiEntryImage image = requireImage(entryId, imageId);
        OpenStorageFacadeRequest request = OpenStorageFacadeRequest.builder()
                .storageObjectId(StorageObjectIdCodec.toValue(image.getStorageObjectId()))
                .ownerType(IMAGE_OWNER_TYPE)
                .ownerId(imageOwnerId(entryId, imageId))
                .build();
        if (storageFacade == null || !storageFacade.exists(request)) {
            throw new BizException("三才图片不可读");
        }
        OpenStorageFacadeResponse response = storageFacade.open(request);
        ClassicsStoredContentResult content = toStoredContentResult(response);
        if (content == null) {
            throw new BizException("三才图片不可读");
        }
        return new SancaiEntryImageContent(
                entryId.value(), imageId.value(), StorageObjectIdCodec.toValue(image.getStorageObjectId()), content);
    }

    @Override
    public ClassicsStoredContentResult getVisualAssetSourceContent(
            SancaiEntryId entryId, SancaiVisualAssetId visualAssetId) {
        return openVisualAssetContent(entryId, visualAssetId, true);
    }

    @Override
    public ClassicsStoredContentResult getVisualAssetGeneratedContent(
            SancaiEntryId entryId, SancaiVisualAssetId visualAssetId) {
        return openVisualAssetContent(entryId, visualAssetId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortImages(SancaiEntryImageSortCommand command) {
        List<SancaiEntryImageId> orderedIdList = command == null ? null : command.getOrderedIds();
        SortablePrioritySwapSupport.sort(
                orderedIdList,
                repository.listImages(SortDirection.ASC),
                SancaiEntryImage::getId,
                SancaiEntryImageId::value,
                SancaiEntryImage::getPriority,
                repository::maxPriority,
                this::updatePriorityOrThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useImage(SancaiEntryId entryId, SancaiEntryImageId imageId) {
        requireImage(entryId, imageId);
        repository.clearCurrentImagesByEntryId(entryId);
        if (repository.markImageCurrent(entryId, imageId) != 1) {
            throw new BizException("三才当前使用图片切换失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(SancaiEntryImageId id) {
        SancaiEntryImage image = getImage(id);
        if (image == null) {
            throw new BizException("三才图片不存在");
        }
        repository.deleteImageById(id);
        unbindStorageOwner(image);
        if (storageFacade != null && image.getStorageObjectId() != null) {
            storageFacade.markUnused(MarkStorageUsageFacadeRequest.builder()
                    .storageObjectId(image.getStorageObjectId().value())
                    .build());
        }
        useFirstRemainingImageIfNeeded(image);
    }

    @Override
    public List<SancaiEntryImage> listImages(SancaiEntryId entryId) {
        return repository.listImagesByEntryId(entryId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiVisualAssetId updateVisualAsset(SancaiVisualAsset visualAsset) {
        validateVisualWeights(visualAsset);
        if (visualAsset.getId() == null) {
            return repository.insertVisualAsset(visualAsset);
        }
        repository.updateVisualAsset(visualAsset);
        return visualAsset.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId) {
        repository.updateCurrentVisualAsset(entryId, visualAssetId);
    }

    @Override
    public List<SancaiVisualAsset> listVisualAssets(SancaiEntryId entryId) {
        return repository.listVisualAssetsByEntryId(entryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyFusionDescription(
            SancaiEntryId entryId, SancaiVisualAssetId visualAssetId, String fusionDescription) {
        if (entryId == null || visualAssetId == null || StringUtils.isBlank(fusionDescription)) {
            throw new BizException("三才信息融合写回参数不完整");
        }
        SancaiVisualAsset currentAsset = requireVisualAsset(entryId, visualAssetId);
        validateVisualWeights(currentAsset, "三才信息融合写回");
        if (StringUtils.isBlank(currentAsset.getImageAnalysisMarkdown())) {
            throw new BizException("三才信息融合写回失败：视觉资产缺少图片理解结果");
        }
        repository.updateVisualAssetFusionDescription(visualAssetId, fusionDescription.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiVisualAsset createGeneratedVisualAssetVersion(
            SancaiEntryId entryId, SancaiVisualAssetId visualAssetId, StorageObjectId generatedImageStorageObjectId) {
        if (entryId == null || visualAssetId == null || generatedImageStorageObjectId == null) {
            throw new BizException("三才生图版本参数不完整");
        }
        SancaiVisualAsset currentAsset = requireVisualAsset(entryId, visualAssetId);
        validateVisualWeights(currentAsset, "三才生图版本创建");
        if (currentAsset.getSourceImageStorageObjectId() == null) {
            throw new BizException("三才生图版本创建失败：视觉资产缺少原图");
        }
        if (StringUtils.isBlank(currentAsset.getVisualDescription())) {
            throw new BizException("三才生图版本创建失败：视觉资产缺少视觉描述");
        }

        SancaiVisualAsset nextAsset = new SancaiVisualAsset();
        nextAsset.setEntryId(entryId);
        nextAsset.setVersionNo(repository.maxVisualAssetVersionNo(entryId) + 1);
        nextAsset.setStatus(SancaiVisualAssetStatus.READY);
        nextAsset.setSourceImageStorageObjectId(currentAsset.getSourceImageStorageObjectId());
        nextAsset.setGeneratedImageStorageObjectId(generatedImageStorageObjectId);
        nextAsset.setCurrentUsed(false);
        nextAsset.setTextWeight(currentAsset.getTextWeight());
        nextAsset.setImageWeight(currentAsset.getImageWeight());
        nextAsset.setImageAnalysisMarkdown(currentAsset.getImageAnalysisMarkdown());
        nextAsset.setFusionDescription(currentAsset.getFusionDescription());
        nextAsset.setVisualDescription(currentAsset.getVisualDescription());
        nextAsset.setGenerationParamsJson(currentAsset.getGenerationParamsJson());
        nextAsset.setId(repository.insertVisualAsset(nextAsset));
        return nextAsset;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SancaiShowcaseId requestShowcase(SancaiShowcaseCommand command) {
        SancaiShowcaseJobResult result = requestShowcaseJob(command);
        return result == null ? null : result.getShowcaseId();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SancaiShowcaseJobResult requestShowcaseJob(SancaiShowcaseCommand command) {
        validateShowcaseCommand(command);
        SancaiShowcase showcase = command == null ? new SancaiShowcase() : command.toEntity();
        showcase.setStatus(SancaiShowcaseStatus.REQUESTED);
        showcase.setStorageObjectId(null);
        SancaiShowcaseId showcaseId = repository.insertShowcase(showcase);
        if (showcaseId == null) {
            return null;
        }
        showcase.setId(showcaseId);
        try {
            showcase.setStatus(SancaiShowcaseStatus.PROCESSING);
            repository.updateShowcase(showcase);
            if (workerRenderClient == null) {
                markShowcaseFailed(showcaseId, SHOWCASE_FAILURE_WORKER_UNAVAILABLE, "三才静态展示 worker 不可用");
                return failedShowcaseJob(showcaseId, SHOWCASE_FAILURE_WORKER_UNAVAILABLE, "三才静态展示 worker 不可用");
            }
            WorkerRenderDtos.WorkerRenderRequest renderRequest = renderRequest(showcaseId, showcase);
            WorkerRenderDtos.WorkerRenderResponse response = workerRenderClient.renderSancaiShowcase(renderRequest);
            if (!isSuccess(response)) {
                String failureType = workerFailureType(response);
                String failureMessage = workerFailureMessage(response);
                markShowcaseFailed(showcaseId, failureType, failureMessage);
                return failedShowcaseJob(showcaseId, failureType, failureMessage);
            }
            WorkerRenderDtos.Artifact artifact = response.getArtifact();
            byte[] content = artifactContent(artifact);
            validateShowcaseArtifact(artifact, content);
            UploadStorageFacadeResponse uploadResponse = saveShowcaseArtifact(showcaseId, artifact, content);
            if (uploadResponse == null) {
                markShowcaseFailed(showcaseId, SHOWCASE_FAILURE_STORAGE_FAILED, "三才静态展示产物保存失败");
                return failedShowcaseJob(showcaseId, SHOWCASE_FAILURE_STORAGE_FAILED, "三才静态展示产物保存失败");
            }
            StorageObjectId storageObjectId = toStorageObjectId(uploadResponse);
            if (storageObjectId == null) {
                markShowcaseFailed(showcaseId, SHOWCASE_FAILURE_STORAGE_FAILED, "三才静态展示产物保存失败");
                return failedShowcaseJob(showcaseId, SHOWCASE_FAILURE_STORAGE_FAILED, "三才静态展示产物保存失败");
            }
            bindShowcaseArtifactOwner(showcaseId, storageObjectId.value());
            int entryCount =
                    response.getSummary() == null || response.getSummary().getItemCount() == null
                            ? 0
                            : response.getSummary().getItemCount();
            int assetCount = assetCountFromPayload(showcase.getScopeJson());
            String filename = filenameHint(showcaseId, artifact);
            String contentType = artifact.getContentType();
            Long sizeBytes = artifact.getSizeBytes() == null ? (long) content.length : artifact.getSizeBytes();
            String sha256 = normalizedSha256(artifact.getSha256());
            repository.markShowcaseCompleted(
                    showcaseId, storageObjectId, entryCount, assetCount, filename, contentType, sizeBytes, sha256);
            return new SancaiShowcaseJobResult(
                    showcaseId,
                    SancaiShowcaseStatus.COMPLETED,
                    storageObjectId,
                    filename,
                    sizeBytes,
                    sha256,
                    null,
                    null);
        } catch (Exception ex) {
            markShowcaseFailed(showcaseId, SHOWCASE_FAILURE_INTERNAL, "三才静态展示生成失败");
            return failedShowcaseJob(showcaseId, SHOWCASE_FAILURE_INTERNAL, "三才静态展示生成失败");
        }
    }

    @Override
    public ClassicsStoredContentResult getShowcaseContent(StorageObjectId storageObjectId) {
        OpenStorageFacadeResponse response = storageFacade == null
                ? null
                : storageFacade.open(OpenStorageFacadeRequest.builder()
                        .storageObjectId(StorageObjectIdCodec.toValue(storageObjectId))
                        .build());
        ClassicsStoredContentResult content = toStoredContentResult(response);
        if (content == null) {
            throw new BizException("三才展示产物不存在");
        }
        return content;
    }

    @Override
    public ClassicsStoredContentResult getShowcaseContent(SancaiShowcaseId showcaseId) {
        SancaiShowcase showcase = repository.getShowcaseById(showcaseId);
        if (showcase == null || showcase.getStorageObjectId() == null) {
            throw new BizException("三才展示产物不存在");
        }
        return getShowcaseContent(showcase.getStorageObjectId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShowcase(SancaiShowcaseId showcaseId) {
        if (showcaseId == null) {
            return;
        }
        SancaiShowcase showcase = repository.getShowcaseById(showcaseId);
        if (showcase == null) {
            return;
        }
        unbindShowcaseArtifactOwner(showcaseId);
        repository.deleteShowcaseById(showcaseId);
        removeStorageObjectIfUnreferenced(showcase.getStorageObjectId());
    }

    @Override
    public PageResult<SancaiShowcase> pageShowcases(String status, PageQuery page) {
        return repository.pageShowcases(status, page.getPageNo(), page.getPageSize());
    }

    @Override
    public PageResult<SancaiShowcase> pageShowcases(
            String keyword,
            String status,
            String visibilityRiskStatus,
            Date requestedAtStart,
            Date requestedAtEnd,
            PageQuery page) {
        return repository.pageShowcases(
                keyword,
                status,
                visibilityRiskStatus,
                requestedAtStart,
                requestedAtEnd,
                page.getPageNo(),
                page.getPageSize());
    }

    private WorkerRenderDtos.WorkerRenderRequest renderRequest(SancaiShowcaseId showcaseId, SancaiShowcase showcase) {
        WorkerRenderDtos.WorkerRenderRequest request = new WorkerRenderDtos.WorkerRenderRequest();
        request.setRequestId("sancai-showcase-" + (showcaseId == null ? "unknown" : showcaseId.value()));
        request.setTraceId(request.getRequestId());
        request.setCallerDomain("CLASSICS");
        request.setOperation(SHOWCASE_RENDER_OPERATION);
        request.setRenderType(SHOWCASE_RENDER_TYPE);
        request.setTemplate(renderTemplate());
        request.setOutput(renderOutput(showcaseId, showcase));
        request.setInput(renderInput(showcase));
        request.setOptions(renderOptions());
        return request;
    }

    private WorkerRenderDtos.Template renderTemplate() {
        WorkerRenderDtos.Template template = new WorkerRenderDtos.Template();
        template.setTemplateId(SHOWCASE_RENDER_TEMPLATE_ID);
        template.setTemplateVersion(SHOWCASE_RENDER_TEMPLATE_VERSION);
        return template;
    }

    private WorkerRenderDtos.Output renderOutput(SancaiShowcaseId showcaseId, SancaiShowcase showcase) {
        WorkerRenderDtos.Output output = new WorkerRenderDtos.Output();
        output.setFormat(SHOWCASE_RENDER_OUTPUT_FORMAT);
        output.setFilenameHint(showcaseOutputFilename(showcaseId, showcase));
        output.setLocale(SHOWCASE_RENDER_LOCALE);
        return output;
    }

    private String showcaseOutputFilename(SancaiShowcaseId showcaseId, SancaiShowcase showcase) {
        if (showcaseId == null) {
            return SHOWCASE_RENDER_OUTPUT_FILENAME;
        }
        return "sancai-showcase-" + showcaseId.value()
                + "-"
                + (showcase == null || showcase.getStatus() == null
                        ? System.currentTimeMillis()
                        : showcase.getStatus().name().toLowerCase())
                + ".html";
    }

    private WorkerRenderDtos.Input renderInput(SancaiShowcase showcase) {
        WorkerRenderDtos.Input input = new WorkerRenderDtos.Input();
        input.setSnapshotId(null);
        input.setContentType(SHOWCASE_RENDER_CONTENT_TYPE);
        input.setPayloadJson(renderPayloadJson(showcase == null ? null : showcase.getScopeJson()));
        return input;
    }

    private WorkerRenderDtos.Options renderOptions() {
        WorkerRenderDtos.Options options = new WorkerRenderDtos.Options();
        options.setStream(false);
        options.setIncludeMetadata(true);
        return options;
    }

    private String renderPayloadJson(String scopeJson) {
        JsonNode payload = normalizeShowcasePayload(parsePayload(scopeJson));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private JsonNode parsePayload(String scopeJson) {
        try {
            if (scopeJson == null || scopeJson.isBlank()) {
                return defaultPayload();
            }
            JsonNode parsed = objectMapper.readTree(scopeJson);
            return parsed == null || !parsed.isObject() ? defaultPayload() : parsed;
        } catch (Exception ex) {
            return defaultPayload();
        }
    }

    private ObjectNode defaultPayload() {
        ObjectNode defaultPayload = objectMapper.createObjectNode();
        defaultPayload.put("title", "Sancai Showcase");
        defaultPayload.set("catalog", objectMapper.createArrayNode());
        defaultPayload.set("entries", objectMapper.createArrayNode());
        defaultPayload.set("assets", objectMapper.createArrayNode());
        defaultPayload.set("metadata", objectMapper.createObjectNode());
        return defaultPayload;
    }

    private JsonNode normalizeShowcasePayload(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            return defaultPayload();
        }
        ObjectNode payloadObject = (ObjectNode) payload;
        JsonNode entries = payloadObject.get("entries");
        if (entries == null || !entries.isArray()) {
            payloadObject.set("entries", objectMapper.createArrayNode());
            return payloadObject;
        }
        for (JsonNode entry : entries) {
            if (entry instanceof ObjectNode entryObject) {
                entryObject.set("images", normalizeShowcaseImages(entryObject.get("images")));
            }
        }
        return payloadObject;
    }

    private ArrayNode normalizeShowcaseImages(JsonNode images) {
        ArrayNode normalized = objectMapper.createArrayNode();
        if (images == null || !images.isArray()) {
            return normalized;
        }
        List<ObjectNode> imageObjects = new ArrayList<>();
        for (JsonNode image : images) {
            ObjectNode imageObject =
                    image instanceof ObjectNode object ? object.deepCopy() : objectMapper.createObjectNode();
            normalizeShowcaseImage(imageObject);
            imageObjects.add(imageObject);
        }
        imageObjects.stream()
                .sorted(Comparator.comparingInt(SancaiAssetApplicationServiceImpl::imagePriority))
                .forEach(normalized::add);
        return normalized;
    }

    private void normalizeShowcaseImage(ObjectNode image) {
        putTextIfMissing(image, "src", imageSource(image));
        putTextIfMissing(image, "alt", imageAlt(image));
        putTextIfMissing(image, "caption", image.path("title").asText(""));
        putBooleanIfMissing(image, "currentUsed", false);
        putIntIfMissing(image, "priority", 0);
    }

    private static String imageSource(ObjectNode image) {
        String source = textValue(image, "src");
        if (StringUtils.isNotBlank(source)) {
            return source;
        }
        source = textValue(image, "previewUrl");
        if (StringUtils.isNotBlank(source)) {
            return source;
        }
        JsonNode storageObject = image.get("storageObject");
        if (storageObject != null && storageObject.isObject()) {
            source = textValue((ObjectNode) storageObject, "previewUrl");
            if (StringUtils.isNotBlank(source)) {
                return source;
            }
        }
        return "";
    }

    private static String imageAlt(ObjectNode image) {
        String imageType = textValue(image, "imageType");
        return "GENERATED".equalsIgnoreCase(imageType) ? "三才图会生成图" : "三才图会原图";
    }

    private static int imagePriority(ObjectNode image) {
        JsonNode priority = image.get("priority");
        return priority == null || !priority.canConvertToInt() ? 0 : priority.asInt();
    }

    private static String textValue(ObjectNode object, String fieldName) {
        JsonNode value = object.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static void putTextIfMissing(ObjectNode object, String fieldName, String value) {
        if (!object.has(fieldName) || object.get(fieldName).isNull()) {
            object.put(fieldName, value == null ? "" : value);
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

    private void validateShowcaseCommand(SancaiShowcaseCommand command) {
        if (command == null) {
            return;
        }
        if (command.getVisibilityRiskStatus() == SancaiVisibilityRiskStatus.CONTAINS_PRIVATE
                && !command.isPrivateConfirmed()) {
            throw new BizException(
                    SHOWCASE_FAILURE_PRIVATE_UNCONFIRMED,
                    "classics.sancai.showcase.private.unconfirmed",
                    "包含私有内容的静态展示生成必须先确认风险");
        }
    }

    private UploadStorageFacadeResponse saveShowcaseArtifact(
            SancaiShowcaseId showcaseId, WorkerRenderDtos.Artifact artifact, byte[] content) {
        if (storageFacade == null) {
            return null;
        }
        return storageFacade.upload(UploadStorageFacadeRequest.builder()
                .inputStream(new ByteArrayInputStream(content))
                .originalFilename(filenameHint(showcaseId, artifact))
                .contentType(artifact == null ? null : artifact.getContentType())
                .sizeBytes((long) content.length)
                .ownerType(SHOWCASE_OWNER_TYPE)
                .ownerId(showcaseOwnerId(showcaseId))
                .build());
    }

    private void bindShowcaseArtifactOwner(SancaiShowcaseId showcaseId, Long storageObjectId) {
        if (storageFacade == null || showcaseId == null || storageObjectId == null) {
            return;
        }
        storageFacade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(storageObjectId))
                .ownerType(SHOWCASE_OWNER_TYPE)
                .ownerId(showcaseOwnerId(showcaseId))
                .ownerParams("usage=SANCAI_SHOWCASE;showcaseId=" + showcaseId.value())
                .build());
    }

    private void unbindShowcaseArtifactOwner(SancaiShowcaseId showcaseId) {
        if (storageFacade == null || showcaseId == null) {
            return;
        }
        storageFacade.unbindOwner(UnbindStorageOwnerFacadeRequest.builder()
                .ownerType(SHOWCASE_OWNER_TYPE)
                .ownerId(showcaseOwnerId(showcaseId))
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
            // Shared Storage objects remain available while other references exist.
        }
    }

    private static String showcaseOwnerId(SancaiShowcaseId showcaseId) {
        return SHOWCASE_OWNER_ID_PREFIX + (showcaseId == null ? "unknown" : showcaseId.value());
    }

    private String filenameHint(SancaiShowcaseId showcaseId, WorkerRenderDtos.Artifact artifact) {
        if (artifact == null
                || artifact.getFilename() == null
                || artifact.getFilename().isBlank()) {
            return "sancai-showcase-" + (showcaseId == null ? "unknown" : showcaseId.value()) + ".html";
        }
        return artifact.getFilename();
    }

    private byte[] artifactContent(WorkerRenderDtos.Artifact artifact) {
        if (artifact == null
                || artifact.getContent() == null
                || artifact.getContent().isBlank()) {
            return new byte[0];
        }
        validateShowcaseArtifactSizeBeforeDecode(artifact);
        byte[] content;
        if ("TEXT".equalsIgnoreCase(artifact.getEncoding())) {
            content = artifact.getContent().getBytes(StandardCharsets.UTF_8);
        } else if ("BASE64".equalsIgnoreCase(artifact.getEncoding())) {
            content = Base64.getDecoder().decode(artifact.getContent());
        } else {
            content = artifact.getContent().getBytes(StandardCharsets.UTF_8);
        }
        validateShowcaseArtifactSize(content.length);
        return content;
    }

    private static void validateShowcaseArtifactSizeBeforeDecode(WorkerRenderDtos.Artifact artifact) {
        if (artifact.getSizeBytes() != null) {
            validateShowcaseArtifactSize(artifact.getSizeBytes());
        }
        if ("BASE64".equalsIgnoreCase(artifact.getEncoding())
                && artifact.getContent().length() > (MAX_SHOWCASE_ARTIFACT_SIZE_BYTES * 4 / 3 + 4)) {
            throw new BizException("三才静态展示产物超过大小限制");
        }
    }

    private static void validateShowcaseArtifactSize(long sizeBytes) {
        if (sizeBytes > MAX_SHOWCASE_ARTIFACT_SIZE_BYTES) {
            throw new BizException("三才静态展示产物超过大小限制");
        }
    }

    private void validateShowcaseArtifact(WorkerRenderDtos.Artifact artifact, byte[] content) {
        if (artifact == null || content == null || content.length == 0) {
            throw new BizException("三才静态展示产物为空");
        }
        if (StringUtils.isBlank(artifact.getContentType())
                || !StringUtils.startsWithIgnoreCase(artifact.getContentType(), SHOWCASE_ARTIFACT_CONTENT_TYPE)) {
            throw new BizException("三才静态展示产物类型不正确");
        }
        if (artifact.getSizeBytes() != null && artifact.getSizeBytes() != content.length) {
            throw new BizException("三才静态展示产物大小校验失败");
        }
        String expectedSha256 = normalizedSha256(artifact.getSha256());
        if (StringUtils.isNotBlank(expectedSha256) && !expectedSha256.equals(sha256(content))) {
            throw new BizException("三才静态展示产物摘要校验失败");
        }
    }

    private StorageObjectId toStorageObjectId(UploadStorageFacadeResponse uploadResponse) {
        return uploadResponse == null || uploadResponse.getStorageObjectId() == null
                ? null
                : StorageObjectId.of(uploadResponse.getStorageObjectId());
    }

    private static boolean isSuccess(WorkerRenderDtos.WorkerRenderResponse response) {
        return response != null && "SUCCEEDED".equalsIgnoreCase(response.getStatus()) && response.getArtifact() != null;
    }

    private void markShowcaseFailed(SancaiShowcaseId showcaseId, String failureType, String failureMessage) {
        repository.markShowcaseFailed(showcaseId, failureType, abbreviateFailureMessage(failureMessage));
    }

    private SancaiShowcaseJobResult failedShowcaseJob(
            SancaiShowcaseId showcaseId, String failureType, String failureMessage) {
        return new SancaiShowcaseJobResult(
                showcaseId,
                SancaiShowcaseStatus.FAILED,
                null,
                null,
                null,
                null,
                failureType,
                abbreviateFailureMessage(failureMessage));
    }

    private static String workerFailureType(WorkerRenderDtos.WorkerRenderResponse response) {
        WorkerRenderDtos.WorkerRenderError error = response == null ? null : response.getError();
        return error == null || StringUtils.isBlank(error.getType()) ? SHOWCASE_FAILURE_RENDER_FAILED : error.getType();
    }

    private static String workerFailureMessage(WorkerRenderDtos.WorkerRenderResponse response) {
        WorkerRenderDtos.WorkerRenderError error = response == null ? null : response.getError();
        return error == null || StringUtils.isBlank(error.getMessage()) ? "三才静态展示渲染失败" : error.getMessage();
    }

    private static String abbreviateFailureMessage(String failureMessage) {
        return StringUtils.abbreviate(
                StringUtils.defaultIfBlank(failureMessage, "三才静态展示生成失败"), SHOWCASE_FAILURE_MESSAGE_MAX_LENGTH);
    }

    private int assetCountFromPayload(String scopeJson) {
        JsonNode payload = parsePayload(scopeJson);
        JsonNode assets = payload == null ? null : payload.get("assets");
        return assets == null || !assets.isArray() ? 0 : assets.size();
    }

    private static String normalizedSha256(String sha256) {
        if (StringUtils.isBlank(sha256)) {
            return null;
        }
        String trimmed = sha256.trim();
        return StringUtils.startsWithIgnoreCase(trimmed, "sha256:") ? trimmed.toLowerCase() : "sha256:" + trimmed;
    }

    private static String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return "sha256:" + HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException ex) {
            throw new BizException("三才静态展示产物摘要校验失败");
        }
    }

    private void updatePriorityOrThrow(SancaiEntryImageId id, int priority) {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(id);
        image.setPriority(priority);
        if (repository.updatePriority(image) != 1) {
            throw sortDbFailure();
        }
    }

    private static BizException sortDbFailure() {
        return new BizException(
                ErrorCode.SORT_DB_FAILURE.getCode(),
                ErrorCode.SORT_DB_FAILURE.getMessageKey(),
                ErrorCode.SORT_DB_FAILURE.getMessage());
    }

    private void clearCurrentImagesIfNeeded(SancaiEntryImageUploadCommand command, SancaiEntryId entryId) {
        if (command == null || !command.isCurrentUsed()) {
            return;
        }
        repository.clearCurrentImagesByEntryId(entryId);
    }

    private SancaiEntryImage requireImage(SancaiEntryId entryId, SancaiEntryImageId imageId) {
        SancaiEntryImage image = getImage(imageId);
        if (image == null) {
            throw new BizException("三才图片不存在");
        }
        if (entryId == null || image.getEntryId() == null || !entryId.equals(image.getEntryId())) {
            throw new BizException("三才图片不属于当前条目");
        }
        if (image.getStorageObjectId() == null) {
            throw new BizException("三才图片未关联 Storage 对象");
        }
        return image;
    }

    private SancaiVisualAsset requireVisualAsset(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId) {
        if (entryId == null || visualAssetId == null) {
            throw new BizException("三才视觉资产不存在");
        }
        SancaiVisualAsset visualAsset = repository.getVisualAssetById(visualAssetId);
        if (visualAsset == null || visualAsset.getEntryId() == null || !entryId.equals(visualAsset.getEntryId())) {
            throw new BizException("三才视觉资产不存在: " + visualAssetId.value());
        }
        return visualAsset;
    }

    private ClassicsStoredContentResult openVisualAssetContent(
            SancaiEntryId entryId, SancaiVisualAssetId visualAssetId, boolean sourceContent) {
        SancaiVisualAsset visualAsset = requireVisualAsset(entryId, visualAssetId);
        StorageObjectId storageObjectId = sourceContent
                ? visualAsset.getSourceImageStorageObjectId()
                : visualAsset.getGeneratedImageStorageObjectId();
        if (storageObjectId == null) {
            throw new BizException(sourceContent ? "三才视觉资产原图不存在" : "三才视觉资产生成图不存在");
        }
        OpenStorageFacadeResponse response = storageFacade == null
                ? null
                : storageFacade.open(OpenStorageFacadeRequest.builder()
                        .storageObjectId(StorageObjectIdCodec.toValue(storageObjectId))
                        .build());
        ClassicsStoredContentResult content = toStoredContentResult(response);
        if (content == null) {
            throw new BizException(sourceContent ? "三才视觉资产原图不可读" : "三才视觉资产生成图不可读");
        }
        return content;
    }

    private static void validateImageUpload(SancaiEntryImageUploadCommand command) {
        if (command == null || command.getEntryId() == null) {
            throw new BizException("三才条目不能为空");
        }
        if (!StringUtils.startsWithIgnoreCase(command.getContentType(), "image/")) {
            throw new BizException("三才图片内容类型无效");
        }
    }

    private static void validateVisualWeights(SancaiVisualAsset visualAsset) {
        validateVisualWeights(visualAsset, "三才视觉资产保存");
    }

    private static void validateVisualWeights(SancaiVisualAsset visualAsset, String actionLabel) {
        if (visualAsset == null) {
            throw new BizException(actionLabel + "失败：视觉资产不能为空");
        }
        if (visualAsset.getTextWeight() == null) {
            throw new BizException(actionLabel + "失败：文本权重不能为空");
        }
        if (visualAsset.getImageWeight() == null) {
            throw new BizException(actionLabel + "失败：图片权重不能为空");
        }
    }

    private void bindStorageOwner(Long storageObjectId, SancaiEntryId entryId, SancaiEntryImageId imageId) {
        if (storageFacade == null || storageObjectId == null) {
            return;
        }
        storageFacade.bindOwner(BindStorageOwnerFacadeRequest.builder()
                .storageObjectIds(List.of(storageObjectId))
                .ownerId(imageOwnerId(entryId, imageId))
                .ownerType(IMAGE_OWNER_TYPE)
                .ownerParams("usage=SANCAI_ENTRY_IMAGE;entryId=" + entryId.value() + ";imageId=" + imageId.value())
                .build());
    }

    private void unbindStorageOwner(SancaiEntryImage image) {
        if (storageFacade == null || image == null || image.getEntryId() == null || image.getId() == null) {
            return;
        }
        storageFacade.unbindOwner(UnbindStorageOwnerFacadeRequest.builder()
                .ownerType(IMAGE_OWNER_TYPE)
                .ownerId(imageOwnerId(image.getEntryId(), image.getId()))
                .build());
    }

    private void useFirstRemainingImageIfNeeded(SancaiEntryImage deletedImage) {
        if (deletedImage == null || !deletedImage.isCurrentUsed() || deletedImage.getEntryId() == null) {
            return;
        }
        List<SancaiEntryImage> remainingImages =
                repository.listImagesByEntryId(deletedImage.getEntryId(), SortDirection.ASC);
        if (remainingImages == null || remainingImages.isEmpty()) {
            return;
        }
        SancaiEntryImage nextImage = remainingImages.get(0);
        if (nextImage != null && nextImage.getId() != null) {
            repository.markImageCurrent(deletedImage.getEntryId(), nextImage.getId());
        }
    }

    private static SancaiEntryImageResource toResource(SancaiEntryImage image, UploadStorageFacadeResponse response) {
        Long entryId = image.getEntryId() == null ? null : image.getEntryId().value();
        Long imageId = image.getId() == null ? null : image.getId().value();
        String contentUrl = SANCAI_IMAGE_CONTENT_PATH_PREFIX
                + entryId
                + SANCAI_IMAGE_CONTENT_PATH_SEPARATOR
                + imageId
                + SANCAI_IMAGE_CONTENT_PATH_SUFFIX;
        return new SancaiEntryImageResource(
                entryId,
                imageId,
                response == null ? null : response.getStorageObjectId(),
                response == null ? null : response.getOriginalFilename(),
                response == null ? null : response.getContentType(),
                response == null ? null : response.getSizeBytes(),
                contentUrl,
                contentUrl + "?download=true");
    }

    private static ClassicsStoredContentResult toStoredContentResult(OpenStorageFacadeResponse response) {
        if (response == null || response.getStoredObject() == null || response.getInputStream() == null) {
            return null;
        }
        StorageObjectFacadeDto dto = response.getStoredObject();
        return new ClassicsStoredContentResult(
                dto.getId(), dto.getOriginalFilename(), dto.getContentType(), dto.getSize(), response.getInputStream());
    }

    static String imageOwnerId(SancaiEntryId entryId, SancaiEntryImageId imageId) {
        if (entryId == null || imageId == null) {
            return null;
        }
        return IMAGE_OWNER_ID_PREFIX + entryId.value() + IMAGE_OWNER_ID_SEPARATOR + imageId.value();
    }
}
