package com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller;

import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.assembler.SancaiInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntryPageRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.request.SancaiEntrySaveRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.sancai.controller.response.SancaiEntryResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块", description = "三才图会")
@SysLogger(module = {"古籍", "三才图会"})
@RequestMapping("/api/classics/sancai")
@WrappedApiController
public class SancaiAdminController {
    private final SancaiApplicationService service;

    public SancaiAdminController(SancaiApplicationService service) {
        this.service = service;
    }

    @HasPermission("classics:sancai:view")
    @PostMapping("entries/page")
    public PageResponse<SancaiEntryResponse> pageEntries(@Valid @RequestBody SancaiEntryPageRequest request) {
        return PageResponseHelper.fromPageResult(
                service.pageEntries(
                        SancaiInterfaceAssembler.toQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                SancaiInterfaceAssembler::toResponse);
    }

    @HasPermission("classics:sancai:view")
    @GetMapping("entries/{id}")
    public SancaiEntryResponse getEntry(@PathVariable Long id) {
        return SancaiInterfaceAssembler.toResponse(service.getEntry(id));
    }

    @HasPermission("classics:sancai:edit")
    @PostMapping("entries/save")
    public Long saveEntry(@Valid @RequestBody SancaiEntrySaveRequest request) {
        return service.saveEntry(SancaiInterfaceAssembler.toCommand(request));
    }

    @HasPermission("classics:sancai:delete")
    @PostMapping("entries/{id}/delete")
    public void deleteEntry(@PathVariable Long id) {
        service.deleteEntry(id);
    }
}
