package com.thundax.kuzhambu.storage.interfaces.admin.object.controller;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.storage.application.command.AbortMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.CompleteMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.InitMultipartUploadCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.command.UploadMultipartPartCommand;
import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery;
import com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;
import com.thundax.kuzhambu.storage.application.service.StorageContentApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageMultipartUploadApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageObjectApplicationService;
import com.thundax.kuzhambu.storage.application.service.StorageUploadApplicationService;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadPart;
import com.thundax.kuzhambu.storage.domain.object.model.entity.MultipartUploadSession;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartNumber;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartPartSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageByteSize;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageMimeType;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import com.thundax.kuzhambu.storage.interfaces.admin.object.assembler.StorageInterfaceAssembler;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.AbortMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.CompleteMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.InitMultipartUploadRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.StorageDeleteRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.StoragePageRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.StorageSortRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.request.UploadMultipartPartRequest;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.AbortMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.CompleteMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.InitMultipartUploadResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.StorageObjectResponse;
import com.thundax.kuzhambu.storage.interfaces.admin.object.controller.response.UploadMultipartPartResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "存储模块-存储对象", description = "存储对象")
@SysLogger(module = {"存储", "对象"})
@RequestMapping(value = "/api/storage/object")
@WrappedApiController
public class StorageObjectController {

    private static final String CONTENT_PATH_PREFIX = "/api/storage/object/";
    private static final String CONTENT_PATH_SUFFIX = "/content";
    private static final List<String> ALLOWED_UPLOAD_SUFFIXES = List.of(
            "jpg", "jpeg", "png", "gif", "webp", "pdf", "txt", "md", "csv", "json", "html", "zip", "docx", "xlsx",
            "pptx");
    private static final List<String> UPLOAD_VALIDATION_FAILURE_MESSAGES =
            List.of("文件不能为空", "文件大小超过限制", "无效的后缀名", "文件大小与声明大小不一致");

    private final StorageObjectApplicationService storageObjectApplicationService;
    private final StorageContentApplicationService storageContentApplicationService;
    private final StorageUploadApplicationService storageUploadApplicationService;
    private final StorageMultipartUploadApplicationService storageMultipartUploadApplicationService;

    @Autowired
    public StorageObjectController(
            StorageObjectApplicationService storageObjectApplicationService,
            StorageContentApplicationService storageContentApplicationService,
            StorageUploadApplicationService storageUploadApplicationService,
            StorageMultipartUploadApplicationService storageMultipartUploadApplicationService) {
        this.storageObjectApplicationService = storageObjectApplicationService;
        this.storageContentApplicationService = storageContentApplicationService;
        this.storageUploadApplicationService = storageUploadApplicationService;
        this.storageMultipartUploadApplicationService = storageMultipartUploadApplicationService;
    }

    @Operation(summary = "获取存储对象分页列表", description = "storage:object:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:view")
    @SysLogger(value = "分页")
    @PostMapping(value = "page")
    public PageResponse<StorageObjectResponse> page(@Valid @RequestBody StoragePageRequest request) {
        ListStorageObjectsQuery query = StorageInterfaceAssembler.toListQuery(request);
        return PageResponseHelper.fromPageResult(
                storageObjectApplicationService.page(query, PageInterfaceAssembler.toPageQuery(request)),
                StorageInterfaceAssembler::toResponse);
    }

    @Operation(summary = "排序存储对象", description = "storage:object:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:edit")
    @SysLogger(value = "排序")
    @PostMapping(value = "sort")
    public Boolean sort(@Valid @RequestBody StorageSortRequest request) {
        storageObjectApplicationService.sort(new StorageSortCommand(RequestListHelper.map(
                RequestListHelper.presentUnique(
                        request == null ? null : request.getOrderedIds(),
                        "orderedIds",
                        AdminResponseExceptions::invalidParameter),
                StoredObjectIdCodec::toDomain)));
        return true;
    }

    @Operation(summary = "删除存储对象", description = "storage:object:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:edit")
    @SysLogger(value = "删除")
    @PostMapping(value = "delete")
    public Boolean delete(@Valid @RequestBody StorageDeleteRequest request) {
        List<StoredObjectId> idList = new ArrayList<>();
        for (Long id : RequestListHelper.presentUnique(
                request == null ? null : request.getIds(), "ids", AdminResponseExceptions::invalidParameter)) {
            StoredObjectId storedObjectId = StoredObjectIdCodec.toDomain(id);
            StoredObject bean = storageObjectApplicationService.get(new GetStorageObjectQuery(storedObjectId));
            if (bean == null) {
                throw AdminResponseExceptions.objectNotFound();
            }
            idList.add(bean.getId());
        }

        idList.forEach(id -> storageObjectApplicationService.remove(new RemoveStorageObjectCommand(id)));
        return true;
    }

    @Operation(summary = "初始化分片上传", description = "storage:object:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:edit")
    @SysLogger(value = "分片上传-初始化")
    @PostMapping(value = "multipart/initiate")
    public InitMultipartUploadResponse initiate(@Valid @RequestBody InitMultipartUploadRequest request) {
        MultipartUploadSession session =
                storageMultipartUploadApplicationService.init(toInitMultipartUploadCommand(request));
        return StorageInterfaceAssembler.toResponse(session);
    }

    @Operation(summary = "上传分片", description = "storage:object:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:edit")
    @SysLogger(value = "分片上传-上传分片")
    @PostJsonApiExempt(reason = "分片文件上传必须使用 multipart/form-data 承载文件流")
    @PostMapping(value = "multipart/uploadPart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadMultipartPartResponse uploadPart(
            @Valid UploadMultipartPartRequest request, @RequestParam("file") MultipartFile file) {
        try {
            MultipartUploadPart part =
                    storageMultipartUploadApplicationService.uploadPart(toUploadMultipartPartCommand(request, file));
            return StorageInterfaceAssembler.toResponse(part);
        } catch (IOException exception) {
            throw AdminResponseExceptions.system(exception.getMessage());
        }
    }

    @Operation(summary = "完成分片上传", description = "storage:object:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:edit")
    @SysLogger(value = "分片上传-完成")
    @PostMapping(value = "multipart/complete")
    public CompleteMultipartUploadResponse complete(@Valid @RequestBody CompleteMultipartUploadRequest request) {
        StoredObject storage =
                storageMultipartUploadApplicationService.complete(toCompleteMultipartUploadCommand(request));
        applyDefaultAccessEndpoint(storage);
        return StorageInterfaceAssembler.toResponse(storage, request.getUploadId());
    }

    @Operation(summary = "中止分片上传", description = "storage:object:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:edit")
    @SysLogger(value = "分片上传-中止")
    @PostMapping(value = "multipart/abort")
    public AbortMultipartUploadResponse abort(@Valid @RequestBody AbortMultipartUploadRequest request) {
        storageMultipartUploadApplicationService.abort(toAbortMultipartUploadCommand(request));
        return StorageInterfaceAssembler.toResponse(request.getUploadId());
    }

    @Operation(summary = "上传存储对象", description = "storage:object:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:edit")
    @SysLogger(value = "上传")
    @PostJsonApiExempt(reason = "文件上传必须使用 multipart/form-data 承载文件流")
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StorageObjectResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "ownerType", required = false) String ownerType,
            @RequestParam(value = "ownerId", required = false) String ownerId) {
        try {
            StoredObject storage = storageUploadApplicationService.upload(new UploadStorageObjectCommand(
                    file == null ? null : file.getInputStream(),
                    file == null ? null : file.getOriginalFilename(),
                    file == null ? null : file.getContentType(),
                    file == null ? null : new StorageByteSize(file.getSize()),
                    ALLOWED_UPLOAD_SUFFIXES,
                    StorageOwnerRef.ofNullable(ownerTypeFrom(ownerType), StringUtils.trimToNull(ownerId)),
                    null,
                    null,
                    null));
            applyDefaultAccessEndpoint(storage);
            return StorageInterfaceAssembler.toResponse(storage);
        } catch (BizException exception) {
            if (!isUploadValidationFailure(exception)) {
                throw AdminResponseExceptions.system(exception.getMessage());
            }
            throw AdminResponseExceptions.invalidParameter(exception.getMessage());
        } catch (IOException exception) {
            throw AdminResponseExceptions.system(exception.getMessage());
        }
    }

    private boolean isUploadValidationFailure(BizException exception) {
        return exception.getCode() == null
                && UPLOAD_VALIDATION_FAILURE_MESSAGES.contains(exception.getDefaultMessage());
    }

    @Operation(summary = "读取存储对象内容", description = "storage:object:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission(value = "storage:object:view")
    @SysLogger(value = "读取内容")
    @PostJsonApiExempt(reason = "文件内容需要浏览器直链预览或下载")
    @GetMapping(value = "{id}/content")
    public void content(
            @PathVariable("id") String id,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        StoredObjectContentResult content = storageContentApplicationService.openReadableContent(
                new OpenReadableStorageContentQuery(StoredObjectIdCodec.toDomain(id)));
        StoredObject storage = content.getStorage();
        response.setContentType(
                StringUtils.defaultIfBlank(storage.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
        if (storage.getSize() != null) {
            response.setContentLengthLong(storage.getSize());
        }
        response.setHeader(
                "Content-Disposition",
                contentDisposition(storage.getOriginalFilename(), Boolean.TRUE.equals(download)));
        try (InputStream inputStream = content.getInputStream()) {
            inputStream.transferTo(response.getOutputStream());
        }
    }

    private static StorageOwnerType ownerTypeFrom(String value) {
        return StringUtils.isBlank(value) ? null : StorageOwnerType.from(value);
    }

    private static void applyDefaultAccessEndpoint(StoredObject storage) {
        if (storage != null && storage.getId() != null && StringUtils.isBlank(storage.getAccessEndpoint())) {
            storage.setAccessEndpoint(
                    CONTENT_PATH_PREFIX + StoredObjectIdCodec.toStringValue(storage.getId()) + CONTENT_PATH_SUFFIX);
        }
    }

    private static String contentDisposition(String originalFilename, boolean download) {
        String disposition = download ? "attachment" : "inline";
        String filename = StringUtils.defaultIfBlank(FilenameUtils.getName(originalFilename), "file");
        String asciiFilename = filename.replace("\\", "").replace("\"", "");
        String encodedFilename =
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }

    private InitMultipartUploadCommand toInitMultipartUploadCommand(InitMultipartUploadRequest request) {
        return request == null
                ? null
                : new InitMultipartUploadCommand(
                        toMultipartUploadId(request.getUploadId()),
                        StorageOwnerRef.ofNullable(
                                ownerTypeFrom(request.getOwnerType()), StringUtils.trimToNull(request.getOwnerId())),
                        request.getBusinessType(),
                        request.getOriginalFilename(),
                        toStorageMimeType(request.getMimeType()),
                        null,
                        null,
                        null,
                        toStorageByteSize(request.getTotalSize()),
                        toMultipartPartSize(request.getPartSize()));
    }

    private UploadMultipartPartCommand toUploadMultipartPartCommand(
            UploadMultipartPartRequest request, MultipartFile file) throws IOException {
        if (request == null) {
            return null;
        }
        return new UploadMultipartPartCommand(
                toMultipartUploadId(request.getUploadId()),
                toMultipartPartNumber(request.getPartNumber()),
                request.getEtag(),
                toStorageByteSize(request.getSize()),
                file == null ? null : file.getInputStream());
    }

    private CompleteMultipartUploadCommand toCompleteMultipartUploadCommand(CompleteMultipartUploadRequest request) {
        return request == null
                ? null
                : new CompleteMultipartUploadCommand(
                        toMultipartUploadId(request.getUploadId()), null, null, null, null);
    }

    private AbortMultipartUploadCommand toAbortMultipartUploadCommand(AbortMultipartUploadRequest request) {
        return request == null ? null : new AbortMultipartUploadCommand(toMultipartUploadId(request.getUploadId()));
    }

    private static MultipartUploadId toMultipartUploadId(String value) {
        return StringUtils.isBlank(value) ? null : new MultipartUploadId(value);
    }

    private static MultipartPartNumber toMultipartPartNumber(Integer value) {
        return value == null ? null : new MultipartPartNumber(value);
    }

    private static StorageByteSize toStorageByteSize(Long value) {
        return value == null ? null : new StorageByteSize(value);
    }

    private static MultipartPartSize toMultipartPartSize(Long value) {
        return value == null ? null : new MultipartPartSize(value);
    }

    private static StorageMimeType toStorageMimeType(String value) {
        return StringUtils.isBlank(value) ? null : new StorageMimeType(value);
    }
}
