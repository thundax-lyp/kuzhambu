package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.result.ClassicsStoredContentResult;
import com.thundax.kuzhambu.classics.application.wangqi.result.WangqiDocumentSourceFile;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.assembler.WangqiDocumentInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request.WangqiDocumentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request.WangqiDocumentVersionRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentSourceFileResponse;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentVersionResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
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
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "古籍模块-王圻文档", description = "王圻文档")
@SysLogger(module = {"古籍", "王圻文档"})
@RequestMapping("/api/classics/wangqi/documents")
@WrappedApiController
public class WangqiDocumentAdminController {
    private final WangqiDocumentApplicationService service;
    private final ClassicsContentApplicationService contentService;

    public WangqiDocumentAdminController(
            WangqiDocumentApplicationService service, ClassicsContentApplicationService contentService) {
        this.service = service;
        this.contentService = contentService;
    }

    @Operation(summary = "分页查询王圻文档", description = "classics:wangqi:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "分页查询")
    @PostMapping("page")
    public PageResponse<WangqiDocumentResponse> page(@Valid @RequestBody WangqiDocumentRequest request) {
        return PageResponseHelper.fromPageResult(
                service.page(
                        WangqiDocumentInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentAuthorities()),
                        PageInterfaceAssembler.toPageQuery(request)),
                WangqiDocumentInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查看王圻文档", description = "classics:wangqi:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "详情")
    @PostMapping("get")
    public WangqiDocumentResponse get(@Valid @RequestBody WangqiDocumentRequest request) {
        return WangqiDocumentInterfaceAssembler.toResponse(
                service.get(WangqiDocumentIdCodec.toDomain(request.getId())));
    }

    @Operation(summary = "查询王圻时间线", description = "classics:wangqi:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "时间线")
    @PostMapping("timeline/list")
    public List<WangqiDocumentResponse> listTimeline(@Valid @RequestBody WangqiDocumentRequest request) {
        return service
                .listTimeline(
                        WangqiDocumentInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentAuthorities()))
                .stream()
                .map(WangqiDocumentInterfaceAssembler::toResponse)
                .toList();
    }

    @Operation(summary = "新增王圻文档", description = "classics:wangqi:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:edit")
    @SysLogger(value = "新增")
    @PostMapping("add")
    public WangqiDocumentResponse add(@Valid @RequestBody WangqiDocumentRequest request) {
        WangqiDocumentId id = service.add(WangqiDocumentInterfaceAssembler.toCommand(request));
        return WangqiDocumentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "更新王圻文档", description = "classics:wangqi:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:edit")
    @SysLogger(value = "更新")
    @PostMapping("update")
    public WangqiDocumentResponse update(@Valid @RequestBody WangqiDocumentRequest request) {
        WangqiDocumentId id = service.update(WangqiDocumentInterfaceAssembler.toCommand(request));
        return WangqiDocumentResponse.builder()
                .id(id == null ? null : id.value())
                .build();
    }

    @Operation(summary = "删除王圻文档", description = "classics:wangqi:delete")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:delete")
    @SysLogger(value = "删除")
    @PostMapping("delete")
    public void delete(@Valid @RequestBody WangqiDocumentRequest request) {
        service.delete(WangqiDocumentIdCodec.toDomain(request.getId()));
    }

    @Operation(summary = "上传王圻原始文件", description = "classics:wangqi:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:edit")
    @SysLogger(value = "原始文件上传")
    @PostJsonApiExempt(reason = "文件上传必须使用 multipart/form-data 承载文件流")
    @PostMapping(value = "{id}/source-file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public WangqiDocumentSourceFileResponse uploadSourceFile(
            @PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            WangqiDocumentSourceFile result =
                    service.changeSourceFile(WangqiDocumentInterfaceAssembler.toSourceFileCommand(id, file));
            return WangqiDocumentInterfaceAssembler.toSourceFileResponse(result);
        } catch (IOException exception) {
            throw new BizException("王圻原始文件上传失败：" + exception.getMessage());
        }
    }

    @Operation(summary = "查看王圻原始文件", description = "classics:wangqi:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "原始文件详情")
    @PostMapping("source-file/get")
    public WangqiDocumentSourceFileResponse getSourceFile(@Valid @RequestBody WangqiDocumentRequest request) {
        return WangqiDocumentInterfaceAssembler.toSourceFileResponse(
                service.getSourceFile(WangqiDocumentIdCodec.toDomain(request.getId())));
    }

    @Operation(summary = "读取王圻原始文件内容", description = "classics:wangqi:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "原始文件读取")
    @PostJsonApiExempt(reason = "文件内容需要浏览器直链预览或下载")
    @GetMapping("{id}/source-file/content")
    public void downloadSourceFile(
            @PathVariable("id") Long id,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        ClassicsStoredContentResult content;
        try {
            content = service.getSourceFileContent(WangqiDocumentIdCodec.toDomain(id));
        } catch (BizException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(
                StringUtils.defaultIfBlank(content.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
        if (content.getSize() != null) {
            response.setContentLengthLong(content.getSize());
        }
        response.setHeader(
                "Content-Disposition",
                contentDisposition(content.getOriginalFilename(), Boolean.TRUE.equals(download)));
        try (InputStream inputStream = content.getInputStream()) {
            inputStream.transferTo(response.getOutputStream());
        }
    }

    @Operation(summary = "查询王圻文档版本", description = "classics:wangqi:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "版本列表")
    @PostMapping("versions/list")
    public List<WangqiDocumentVersionResponse> listVersions(@Valid @RequestBody WangqiDocumentVersionRequest request) {
        Long documentId = requireParameter(request == null ? null : request.getId(), "id");
        return contentService
                .listVersions(WangqiDocumentInterfaceAssembler.toContentObjectQuery(
                        ClassicsContentType.WANGQI_DOCUMENT.value(), documentId))
                .stream()
                .map(WangqiDocumentInterfaceAssembler::toVersionResponse)
                .toList();
    }

    @Operation(summary = "查看王圻文档版本", description = "classics:wangqi:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "版本详情")
    @PostMapping("versions/get")
    public WangqiDocumentVersionResponse getVersion(@Valid @RequestBody WangqiDocumentVersionRequest request) {
        return WangqiDocumentInterfaceAssembler.toVersionResponse(
                ownedVersion(request.getId(), request.getVersionId()));
    }

    @Operation(summary = "恢复王圻文档版本", description = "classics:wangqi:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("classics:wangqi:edit")
    @SysLogger(value = "版本恢复")
    @PostMapping("versions/reset")
    public WangqiDocumentVersionResponse resetVersion(@Valid @RequestBody WangqiDocumentVersionRequest request) {
        ownedVersion(request.getId(), request.getVersionId());
        return WangqiDocumentInterfaceAssembler.toVersionResponse(
                contentService.restoreHistoryVersion(ClassicsContentVersionIdCodec.toDomain(request.getVersionId())));
    }

    private ClassicsContentVersion ownedVersion(Long documentId, Long versionId) {
        ClassicsContentVersion version = contentService.getVersion(ClassicsContentVersionIdCodec.toDomain(versionId));
        if (version == null
                || version.getContentType() != ClassicsContentType.WANGQI_DOCUMENT
                || !ClassicsContentIdCodec.toDomain(documentId).equals(version.getContentId())) {
            throw new BizException("王圻版本不属于当前文档");
        }
        return version;
    }

    private static Long requireParameter(Long value, String fieldName) {
        if (value == null) {
            throw AdminResponseExceptions.invalidParameter(fieldName);
        }
        return value;
    }

    private static String contentDisposition(String originalFilename, boolean download) {
        String disposition = download ? "attachment" : "inline";
        String filename = StringUtils.defaultIfBlank(fileName(originalFilename), "file");
        String asciiFilename = filename.replace("\\", "").replace("\"", "");
        String encodedFilename =
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }

    private static String fileName(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        String normalized = path.replace('\\', '/');
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }
}
