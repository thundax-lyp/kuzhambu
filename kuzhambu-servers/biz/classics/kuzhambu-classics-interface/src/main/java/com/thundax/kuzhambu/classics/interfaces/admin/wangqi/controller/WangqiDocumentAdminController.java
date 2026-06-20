package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
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
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentVersionResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
    @ApiImplicitParams({})
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "分页查询")
    @PostMapping("page")
    public PageResponse<WangqiDocumentResponse> page(@Valid @RequestBody WangqiDocumentRequest request) {
        return PageResponseHelper.fromPageResult(
                service.page(
                        WangqiDocumentInterfaceAssembler.toQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                WangqiDocumentInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查看王圻文档", description = "classics:wangqi:view")
    @ApiImplicitParams({})
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "详情")
    @PostMapping("{id}/get")
    public WangqiDocumentResponse get(@Valid @RequestBody WangqiDocumentRequest request) {
        return WangqiDocumentInterfaceAssembler.toResponse(
                service.get(WangqiDocumentIdCodec.toDomain(request.getId())));
    }

    @Operation(summary = "查询王圻时间线", description = "classics:wangqi:view")
    @ApiImplicitParams({})
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "时间线")
    @PostMapping("timeline/list")
    public List<WangqiDocumentResponse> listTimeline(@Valid @RequestBody WangqiDocumentRequest request) {
        return service.listTimeline(WangqiDocumentInterfaceAssembler.toQuery(request)).stream()
                .map(WangqiDocumentInterfaceAssembler::toResponse)
                .toList();
    }

    @Operation(summary = "新增王圻文档", description = "classics:wangqi:edit")
    @ApiImplicitParams({})
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
    @ApiImplicitParams({})
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
    @ApiImplicitParams({})
    @HasPermission("classics:wangqi:delete")
    @SysLogger(value = "删除")
    @PostMapping("delete")
    public void delete(@Valid @RequestBody WangqiDocumentRequest request) {
        service.delete(WangqiDocumentIdCodec.toDomain(request.getId()));
    }

    @Operation(summary = "查询王圻文档版本", description = "classics:wangqi:view")
    @ApiImplicitParams({})
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "版本列表")
    @PostMapping("versions/list")
    public List<WangqiDocumentVersionResponse> listVersions(@Valid @RequestBody WangqiDocumentVersionRequest request) {
        return contentService
                .listVersions(
                        ClassicsContentType.WANGQI_DOCUMENT.value(), ClassicsContentIdCodec.toDomain(request.getId()))
                .stream()
                .map(WangqiDocumentInterfaceAssembler::toVersionResponse)
                .toList();
    }

    @Operation(summary = "查看王圻文档版本", description = "classics:wangqi:view")
    @ApiImplicitParams({})
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "版本详情")
    @PostMapping("versions/get")
    public WangqiDocumentVersionResponse getVersion(@Valid @RequestBody WangqiDocumentVersionRequest request) {
        return WangqiDocumentInterfaceAssembler.toVersionResponse(
                ownedVersion(request.getId(), request.getVersionId()));
    }

    @Operation(summary = "恢复王圻文档版本", description = "classics:wangqi:edit")
    @ApiImplicitParams({})
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
}
