package com.thundax.kuzhambu.classics.interfaces.admin.content.controller;

import com.thundax.kuzhambu.classics.application.content.command.ContentQaPairSortCommand;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagSortCommand;
import com.thundax.kuzhambu.classics.application.content.result.ClassicsExportJobResult;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentExportJobIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentQaPairIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.interfaces.admin.content.assembler.ClassicsContentInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentQaPairSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.request.ClassicsContentTagSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.content.controller.response.ClassicsContentResponse;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.storage.application.service.StorageApplicationService;
import com.thundax.kuzhambu.storage.application.service.content.StoredObjectContent;
import com.thundax.kuzhambu.storage.domain.object.codec.StoredObjectIdCodec;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "古籍模块-通用内容", description = "通用内容")
@SysLogger(module = {"古籍", "通用内容"})
@RequestMapping("/api/classics/content")
@WrappedApiController
public class ClassicsContentAdminController {
    private final ClassicsContentApplicationService service;
    private final StorageApplicationService storageApplicationService;

    public ClassicsContentAdminController(ClassicsContentApplicationService service) {
        this(service, null);
    }

    public ClassicsContentAdminController(
            ClassicsContentApplicationService service, StorageApplicationService storageApplicationService) {
        this.service = service;
        this.storageApplicationService = storageApplicationService;
    }

    @Operation(summary = "查询古籍内容标签", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "标签列表")
    @GetMapping("tags")
    public List<ClassicsContentResponse> listTags(@RequestParam String contentType, @RequestParam Long contentId) {
        ClassicsContentId contentIdValue = ClassicsContentIdCodec.toDomain(contentId);
        return service.listTags(contentType, contentIdValue).stream()
                .map(ClassicsContentInterfaceAssembler::toTagResponse)
                .toList();
    }

    @Operation(summary = "新增古籍内容标签", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "新增标签")
    @PostMapping("tags/add")
    public ClassicsContentResponse addTag(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentTagId id = service.addTag(ClassicsContentInterfaceAssembler.toTagCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "更新古籍内容标签", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "更新标签")
    @PostMapping("tags/update")
    public ClassicsContentResponse updateTag(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentTagId id = service.updateTag(ClassicsContentInterfaceAssembler.toTagCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "排序古籍内容标签", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "标签排序")
    @PostMapping("tags/sort")
    public Boolean sortTags(@Valid @RequestBody ClassicsContentTagSortRequest request) {
        service.sortTags(new ContentTagSortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        ClassicsContentTagIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "查询古籍内容问答", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "问答列表")
    @GetMapping("qa-pairs")
    public List<ClassicsContentResponse> listQaPairs(@RequestParam String contentType, @RequestParam Long contentId) {
        ClassicsContentId contentIdValue = ClassicsContentIdCodec.toDomain(contentId);
        return service.listQaPairs(contentType, contentIdValue).stream()
                .map(ClassicsContentInterfaceAssembler::toQaResponse)
                .toList();
    }

    @Operation(summary = "新增古籍内容问答", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "新增问答")
    @PostMapping("qa-pairs/add")
    public ClassicsContentResponse addQaPair(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentQaPairId id = service.addQaPair(ClassicsContentInterfaceAssembler.toQaCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "更新古籍内容问答", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "更新问答")
    @PostMapping("qa-pairs/update")
    public ClassicsContentResponse updateQaPair(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsContentQaPairId id = service.updateQaPair(ClassicsContentInterfaceAssembler.toQaCommand(request));
        return ClassicsContentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "排序古籍内容问答", description = "classics:content:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:content:edit")
    @SysLogger(value = "问答排序")
    @PostMapping("qa-pairs/sort")
    public Boolean sortQaPairs(@Valid @RequestBody ClassicsContentQaPairSortRequest request) {
        service.sortQaPairs(new ContentQaPairSortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        ClassicsContentQaPairIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "创建古籍内容导出任务", description = "classics:content:export")
    @ApiImplicitParams({})
    @HasPermission("classics:content:export")
    @SysLogger(value = "创建导出任务")
    @PostMapping("exports/create")
    public ClassicsContentResponse createExport(@Valid @RequestBody ClassicsContentRequest request) {
        ClassicsExportJobResult result =
                service.createExportJob(ClassicsContentInterfaceAssembler.toExportCommand(request));
        return ClassicsContentResponse.builder()
                .id(
                        result == null || result.getJobId() == null
                                ? null
                                : result.getJobId().value())
                .status(
                        result == null || result.getStatus() == null
                                ? null
                                : result.getStatus().name())
                .build();
    }

    @Operation(summary = "分页查询古籍内容导出任务", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "导出任务列表")
    @PostMapping("exports/page")
    public PageResponse<ClassicsContentResponse> pageExports(@Valid @RequestBody ClassicsContentRequest request) {
        PageQuery pageQuery = PageInterfaceAssembler.toPageQuery(request);
        return PageResponseHelper.fromPageResult(
                service.pageExportJobs(
                        request.getContentType(), request.getExportKind(), request.getStatus(), pageQuery),
                ClassicsContentInterfaceAssembler::toExportResponse);
    }

    @Operation(summary = "下载古籍内容导出文件", description = "classics:content:view")
    @ApiImplicitParams({})
    @HasPermission("classics:content:view")
    @SysLogger(value = "导出文件下载")
    @GetMapping("exports/{jobId}/content")
    public void downloadExportContent(
            @PathVariable("jobId") Long jobId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        ClassicsContentExportJob job = service.getExportJob(ClassicsContentExportJobIdCodec.toDomain(jobId));
        if (job == null
                || job.getStorageObjectId() == null
                || job.getStatus() != ClassicsExportStatus.COMPLETED
                || job.getExpiresAt() == null
                || job.getExpiresAt().before(new Date())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        StoredObjectContent content =
                storageApplicationService.openReadableContent(toStoredObjectId(job.getStorageObjectId()));
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

    private static String contentDisposition(String originalFilename, boolean download) {
        String disposition = download ? "attachment" : "inline";
        String filename = StringUtils.defaultIfBlank(FilenameUtils.getName(originalFilename), "file");
        String asciiFilename = filename.replace("\\", "").replace("\"", "");
        String encodedFilename =
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }

    private static StoredObjectId toStoredObjectId(StorageObjectId id) {
        return StoredObjectIdCodec.toDomain(StorageObjectIdCodec.toValue(id));
    }
}
