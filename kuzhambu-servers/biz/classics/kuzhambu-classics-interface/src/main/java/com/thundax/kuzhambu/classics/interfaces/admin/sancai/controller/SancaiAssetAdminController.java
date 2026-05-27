package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller;

import com.thundax.kuzhambu.classics.application.sancai.service.SancaiAssetApplicationService;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler.SancaiAssetInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiAssetRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiAssetResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@SysLogger(module = {"古籍", "三才图会资产"})
@RequestMapping("/api/classics/sancai/assets")
@WrappedApiController
public class SancaiAssetAdminController {
    private final SancaiAssetApplicationService service;

    public SancaiAssetAdminController(SancaiAssetApplicationService service) {
        this.service = service;
    }

    @HasPermission("classics:sancai:edit")
    @PostMapping("drafts/save")
    public Long saveDraft(@Valid @RequestBody SancaiAssetRequest request) {
        return service.saveDraft(SancaiAssetInterfaceAssembler.toDraftCommand(request));
    }

    @HasPermission("classics:sancai:view")
    @GetMapping("drafts/latest/{entryId}")
    public SancaiAssetResponse latestDraft(@PathVariable Long entryId) {
        return SancaiAssetInterfaceAssembler.toDraftResponse(service.getLatestDraft(entryId));
    }

    @HasPermission("classics:sancai:edit")
    @PostMapping("images/save")
    public Long saveImage(@Valid @RequestBody SancaiAssetRequest request) {
        return service.saveImage(SancaiAssetInterfaceAssembler.toImageCommand(request));
    }

    @HasPermission("classics:sancai:view")
    @GetMapping("images/{entryId}")
    public List<SancaiAssetResponse> listImages(@PathVariable Long entryId) {
        return service.listImages(entryId).stream().map(SancaiAssetInterfaceAssembler::toImageResponse).toList();
    }

    @HasPermission("classics:sancai:edit")
    @PostMapping("showcases/request")
    public Long requestShowcase(@Valid @RequestBody SancaiAssetRequest request) {
        return service.requestShowcase(SancaiAssetInterfaceAssembler.toShowcaseCommand(request));
    }
}
