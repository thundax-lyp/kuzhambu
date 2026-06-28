package com.thundax.kuzhambu.classics.application.sancai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiDraftCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageSortCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryImageUploadCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiImageCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageContent;
import com.thundax.kuzhambu.classics.application.sancai.result.SancaiEntryImageResource;
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
import com.thundax.kuzhambu.storage.application.helper.StorageUploadResult;
import com.thundax.kuzhambu.storage.application.helper.StorageUploadStreamHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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
    private static final String SANCAI_IMAGE_CONTENT_PATH_PREFIX = "/api/classics/sancai/assets/images/";
    private static final String SANCAI_IMAGE_CONTENT_PATH_SEPARATOR = "/";
    private static final String SANCAI_IMAGE_CONTENT_PATH_SUFFIX = "/content";
    private static final List<String> ALLOWED_IMAGE_SUFFIXES = List.of("jpg", "jpeg", "png", "gif", "webp");

    private final SancaiAssetRepository repository;
    private final WorkerRenderClient workerRenderClient;
    private final StorageFacade storageFacade;
    private final StorageUploadStreamHelper storageUploadStreamHelper;
    private final StorageApplicationService storageApplicationService;
    private final ObjectMapper objectMapper;

    public SancaiAssetApplicationServiceImpl(
            SancaiAssetRepository repository,
            WorkerRenderClient workerRenderClient,
            StorageFacade storageFacade,
            StorageUploadStreamHelper storageUploadStreamHelper,
            StorageApplicationService storageApplicationService,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.workerRenderClient = workerRenderClient;
        this.storageFacade = storageFacade;
        this.storageUploadStreamHelper = storageUploadStreamHelper;
        this.storageApplicationService = storageApplicationService;
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
        SancaiEntryImage replacedImage = currentImageToReplace(command, entryId);
        validateImageUpload(command);

        UploadStorageObjectFacadeResponse uploadResponse =
                storageFacade.upload(UploadStorageObjectFacadeRequest.builder()
                        .inputStream(command.getInputStream())
                        .originalFilename(command.getOriginalFilename())
                        .contentType(command.getContentType())
                        .sizeBytes(command.getSize())
                        .allowedSuffixes(ALLOWED_IMAGE_SUFFIXES)
                        .ownerType(StorageOwnerType.CLASSICS_SANCAI_ENTRY_IMAGE.value())
                        .build());
        StoredObject storage = toStoredObject(uploadResponse);
        SancaiEntryImage image = new SancaiEntryImage();
        image.setEntryId(entryId);
        image.setStorageObjectId(StorageObjectIdCodec.toDomain(storage.getId().value()));
        image.setImageType(command.getImageType());
        image.setTitle(command.getTitle());
        image.setCurrentUsed(command.isCurrentUsed());
        image.setPriority(repository.maxPriority() + 1);
        SancaiEntryImageId imageId = repository.insertImage(image);
        image.setId(imageId);

        if (replacedImage != null) {
            replacedImage.setCurrentUsed(false);
            repository.updateImage(replacedImage);
        }
        ensureStorageOwner(storage, entryId, imageId);
        addStorageReference(storage.getId(), entryId, imageId);
        return toResource(image, storage);
    }

    @Override
    public SancaiEntryImageContent getImageContent(SancaiEntryId entryId, SancaiEntryImageId imageId) {
        SancaiEntryImage image = requireImage(entryId, imageId);
        StoredObjectId objectId = toStoredObjectId(image.getStorageObjectId());
        StorageQuery query = new StorageQuery();
        query.setId(objectId);
        query.setOwnerType(StorageOwnerType.CLASSICS_SANCAI_ENTRY_IMAGE);
        query.setOwnerId(imageOwnerId(entryId, imageId));
        if (!storageApplicationService.existsReadableContent(query)) {
            throw new BizException("三才图片不可读");
        }
        StoredObjectContent content = storageApplicationService.openReadableContent(objectId);
        return new SancaiEntryImageContent(entryId.value(), imageId.value(), objectId.value(), content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortImages(SancaiEntryImageSortCommand command) {
        SortDirection effectiveDirection =
                command == null || command.getSortDirection() == null ? SortDirection.ASC : command.getSortDirection();
        List<SancaiEntryImageId> orderedIdList =
                command == null || command.getOrderedIds() == null ? Collections.emptyList() : command.getOrderedIds();
        if (orderedIdList.isEmpty()) {
            throw sortEmptyInput();
        }

        List<SancaiEntryImage> currentImages = repository.listImages(effectiveDirection);
        if (currentImages == null || currentImages.isEmpty() || currentImages.size() != orderedIdList.size()) {
            throw sortMissingId();
        }

        Map<Long, Integer> indexById = new HashMap<>(currentImages.size());
        Map<Long, Integer> priorityById = new HashMap<>(currentImages.size());
        List<SancaiEntryImageId> currentOrderedIds = new ArrayList<>(currentImages.size());
        for (int i = 0; i < currentImages.size(); i++) {
            SancaiEntryImage image = currentImages.get(i);
            if (image == null || image.getId() == null) {
                throw sortDbFailure();
            }
            long imageId = image.getId().value();
            indexById.put(imageId, i);
            priorityById.put(imageId, image.getPriority());
            currentOrderedIds.add(image.getId());
        }

        for (SancaiEntryImageId orderedId : orderedIdList) {
            if (orderedId == null || orderedId.value() == null || !indexById.containsKey(orderedId.value())) {
                throw sortMissingId();
            }
        }

        int temporaryPriority = repository.maxPriority() + 1;
        for (int i = 0; i < currentOrderedIds.size(); i++) {
            SancaiEntryImageId targetId = orderedIdList.get(i);
            SancaiEntryImageId currentId = currentOrderedIds.get(i);
            if (targetId.equals(currentId)) {
                continue;
            }

            int targetIndex = indexById.get(targetId.value());
            int currentPriority = priorityById.get(currentId.value());
            int targetPriority = priorityById.get(targetId.value());

            updatePriorityOrThrow(targetId, temporaryPriority++);
            updatePriorityOrThrow(currentId, targetPriority);
            updatePriorityOrThrow(targetId, currentPriority);

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
    public void deleteImage(SancaiEntryImageId id) {
        SancaiEntryImage image = getImage(id);
        repository.deleteImageById(id);
        if (image != null && image.getStorageObjectId() != null) {
            storageApplicationService.changeReferenceStatus(new ChangeStorageReferenceStatusCommand(
                    toStoredObjectId(image.getStorageObjectId()), StoredObjectReferenceStatus.UNREFERENCED));
        }
    }

    @Override
    public List<SancaiEntryImage> listImages(SancaiEntryId entryId) {
        return repository.listImagesByEntryId(entryId, SortDirection.ASC);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SancaiVisualAssetId updateVisualAsset(SancaiVisualAsset visualAsset) {
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
    public SancaiShowcaseId requestShowcase(SancaiShowcaseCommand command) {
        SancaiShowcase showcase = command == null ? new SancaiShowcase() : command.toEntity();
        SancaiShowcaseId showcaseId = repository.insertShowcase(showcase);
        if (showcaseId == null) {
            return null;
        }
        try {
            if (workerRenderClient == null) {
                repository.markShowcaseFailed(showcaseId);
                return showcaseId;
            }
            WorkerRenderDtos.WorkerRenderRequest renderRequest = renderRequest(showcaseId, showcase);
            WorkerRenderDtos.WorkerRenderResponse response = workerRenderClient.renderSancaiShowcase(renderRequest);
            if (!isSuccess(response)) {
                repository.markShowcaseFailed(showcaseId);
                return showcaseId;
            }
            StorageUploadResult uploadResult = saveShowcaseArtifact(showcaseId, response);
            if (uploadResult.hasError()) {
                repository.markShowcaseFailed(showcaseId);
                return showcaseId;
            }
            StorageObjectId storageObjectId = toStorageObjectId(uploadResult);
            if (storageObjectId == null) {
                repository.markShowcaseFailed(showcaseId);
                return showcaseId;
            }
            int entryCount =
                    response.getSummary() == null || response.getSummary().getItemCount() == null
                            ? 0
                            : response.getSummary().getItemCount();
            repository.markShowcaseCompleted(showcaseId, storageObjectId, entryCount);
            return showcaseId;
        } catch (Exception ex) {
            repository.markShowcaseFailed(showcaseId);
            return showcaseId;
        }
    }

    @Override
    public PageResult<SancaiShowcase> pageShowcases(String status, PageQuery page) {
        IPage<SancaiShowcase> dataPage = repository.pageShowcases(status, page.getPageNo(), page.getPageSize());
        return PageResult.of(
                (int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), dataPage.getRecords());
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
        JsonNode payload = parsePayload(scopeJson);
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

    private StorageUploadResult saveShowcaseArtifact(
            SancaiShowcaseId showcaseId, WorkerRenderDtos.WorkerRenderResponse response) {
        WorkerRenderDtos.Artifact artifact = response == null ? null : response.getArtifact();
        byte[] content = artifactContent(artifact);
        return storageUploadStreamHelper.uploadServerArtifact(
                new ByteArrayInputStream(content),
                filenameHint(showcaseId, artifact),
                artifact == null ? null : artifact.getContentType(),
                content.length);
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
        if ("TEXT".equalsIgnoreCase(artifact.getEncoding())) {
            return artifact.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        if ("BASE64".equalsIgnoreCase(artifact.getEncoding())) {
            return Base64.getDecoder().decode(artifact.getContent());
        }
        return artifact.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private StorageObjectId toStorageObjectId(StorageUploadResult uploadResult) {
        return uploadResult == null
                        || uploadResult.getStorage() == null
                        || uploadResult.getStorage().getId() == null
                ? null
                : StorageObjectId.of(uploadResult.getStorage().getId().value());
    }

    private static StoredObject toStoredObject(UploadStorageObjectFacadeResponse response) {
        if (response == null) {
            return null;
        }
        StoredObject storage = new StoredObject();
        storage.setId(response.getStorageObjectId() == null ? null : StoredObjectId.of(response.getStorageObjectId()));
        storage.setOriginalFilename(response.getOriginalFilename());
        storage.setContentType(response.getContentType());
        storage.setName(response.getName());
        storage.setExtendName(response.getExtendName());
        storage.setMimeType(response.getMimeType());
        storage.setBucketName(response.getBucketName());
        storage.setObjectKey(response.getObjectKey());
        storage.setSize(response.getSizeBytes());
        storage.setAccessEndpoint(response.getAccessEndpoint());
        storage.setObjectStatus(
                StringUtils.isBlank(response.getObjectStatus())
                        ? null
                        : StoredObjectStatus.from(response.getObjectStatus()));
        storage.setReferenceStatus(
                StringUtils.isBlank(response.getReferenceStatus())
                        ? null
                        : StoredObjectReferenceStatus.from(response.getReferenceStatus()));
        storage.setRemarks(response.getRemarks());
        return storage;
    }

    private static boolean isSuccess(WorkerRenderDtos.WorkerRenderResponse response) {
        return response != null && "SUCCEEDED".equalsIgnoreCase(response.getStatus()) && response.getArtifact() != null;
    }

    private void updatePriorityOrThrow(SancaiEntryImageId id, int priority) {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(id);
        image.setPriority(priority);
        if (repository.updatePriority(image) != 1) {
            throw sortDbFailure();
        }
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

    private SancaiEntryImage currentImageToReplace(SancaiEntryImageUploadCommand command, SancaiEntryId entryId) {
        if (command == null || !command.isCurrentUsed()) {
            return null;
        }
        if (command.getReplaceImageId() == null) {
            throw new BizException("替换当前图片时必须指定 replaceImageId");
        }
        SancaiEntryImage image = requireImage(entryId, SancaiEntryImageIdCodec.toDomain(command.getReplaceImageId()));
        if (!image.isCurrentUsed()) {
            throw new BizException("被替换图片不是当前使用图");
        }
        return image;
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

    private static void validateImageUpload(SancaiEntryImageUploadCommand command) {
        if (command == null || command.getEntryId() == null) {
            throw new BizException("三才条目不能为空");
        }
        if (!StringUtils.startsWithIgnoreCase(command.getContentType(), "image/")) {
            throw new BizException("三才图片内容类型无效");
        }
    }

    private void ensureStorageOwner(StoredObject storage, SancaiEntryId entryId, SancaiEntryImageId imageId) {
        ChangeStorageCommand command = new ChangeStorageCommand();
        command.setId(storage.getId());
        command.setOriginalFilename(storage.getOriginalFilename());
        command.setContentType(storage.getContentType());
        command.setName(storage.getName());
        command.setExtendName(storage.getExtendName());
        command.setMimeType(storage.getMimeType());
        command.setOwnerType(StorageOwnerType.CLASSICS_SANCAI_ENTRY_IMAGE);
        command.setOwnerId(imageOwnerId(entryId, imageId));
        command.setBucketName(storage.getBucketName());
        command.setObjectKey(storage.getObjectKey());
        command.setSize(storage.getSize());
        command.setAccessEndpoint(storage.getAccessEndpoint());
        command.setObjectStatus(storage.getObjectStatus());
        command.setReferenceStatus(storage.getReferenceStatus());
        command.setRemarks(storage.getRemarks());
        storageApplicationService.change(command);
    }

    private void addStorageReference(StoredObjectId objectId, SancaiEntryId entryId, SancaiEntryImageId imageId) {
        StoredObjectReference reference = new StoredObjectReference(
                objectId,
                imageOwnerId(entryId, imageId),
                StorageOwnerType.CLASSICS_SANCAI_ENTRY_IMAGE,
                "usage=SANCAI_ENTRY_IMAGE;entryId=" + entryId.value() + ";imageId=" + imageId.value(),
                StoredObjectReferenceStatus.REFERENCED);
        storageApplicationService.addReferences(new AddStorageReferencesCommand(List.of(reference)));
        storageApplicationService.changeReferenceStatus(
                new ChangeStorageReferenceStatusCommand(objectId, StoredObjectReferenceStatus.REFERENCED));
    }

    private static SancaiEntryImageResource toResource(SancaiEntryImage image, StoredObject storage) {
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
                storage.getId() == null ? null : storage.getId().value(),
                storage.getOriginalFilename(),
                storage.getContentType(),
                storage.getSize(),
                contentUrl,
                contentUrl + "?download=true");
    }

    private static StoredObjectId toStoredObjectId(StorageObjectId id) {
        return StoredObjectIdCodec.toDomain(StorageObjectIdCodec.toValue(id));
    }

    static String imageOwnerId(SancaiEntryId entryId, SancaiEntryImageId imageId) {
        if (entryId == null || imageId == null) {
            return null;
        }
        return IMAGE_OWNER_ID_PREFIX + entryId.value() + IMAGE_OWNER_ID_SEPARATOR + imageId.value();
    }
}
