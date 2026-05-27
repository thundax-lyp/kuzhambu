package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller;

import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler.SancaiAssetInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiAssetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiAssetResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块", description = "三才图会资产")
@SysLogger(module = {"古籍", "三才图会资产"})
@RequestMapping("/api/classics/sancai/assets")
@WrappedApiController
public class SancaiAssetAdminController {
    private final SancaiAssetApplicationService service;

    public SancaiAssetAdminController(SancaiAssetApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "保存三才图会草稿", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "保存草稿")
    @PostMapping("drafts/save")
    public SancaiAssetResponse saveDraft(@Valid @RequestBody SancaiAssetRequest request) {
        Long id = service.saveDraft(SancaiAssetInterfaceAssembler.toDraftCommand(request));
        return SancaiAssetResponse.builder().id(id).build();
    }

    @Operation(summary = "查看三才图会最新草稿", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "最新草稿")
    @GetMapping("drafts/latest/{entryId}")
    public SancaiAssetResponse latestDraft(@PathVariable Long entryId) {
        return SancaiAssetInterfaceAssembler.toDraftResponse(service.getLatestDraft(entryId));
    }

    @Operation(summary = "保存三才图会图片", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "保存图片")
    @PostMapping("images/save")
    public SancaiAssetResponse saveImage(@Valid @RequestBody SancaiAssetRequest request) {
        Long id = service.saveImage(SancaiAssetInterfaceAssembler.toImageCommand(request));
        return SancaiAssetResponse.builder().id(id).build();
    }

    @Operation(summary = "查询三才图会图片", description = "classics:sancai:view")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:view")
    @SysLogger(value = "图片列表")
    @GetMapping("images/{entryId}")
    public List<SancaiAssetResponse> listImages(@PathVariable Long entryId) {
        return service.listImages(entryId).stream()
                .map(SancaiAssetInterfaceAssembler::toImageResponse)
                .toList();
    }

    @Operation(summary = "创建三才图会静态展示任务", description = "classics:sancai:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:sancai:edit")
    @SysLogger(value = "创建展示任务")
    @PostMapping("showcases/request")
    public SancaiAssetResponse requestShowcase(@Valid @RequestBody SancaiAssetRequest request) {
        Long id = service.requestShowcase(SancaiAssetInterfaceAssembler.toShowcaseCommand(request));
        return SancaiAssetResponse.builder().id(id).build();
    }
}
