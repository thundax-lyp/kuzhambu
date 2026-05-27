package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller;

import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.assembler.WangqiDocumentInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request.WangqiDocumentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@SysLogger(module = {"古籍", "王圻文档"})
@RequestMapping("/api/classics/wangqi/documents")
@WrappedApiController
public class WangqiDocumentAdminController {
    private final WangqiDocumentApplicationService service;
    public WangqiDocumentAdminController(WangqiDocumentApplicationService service) { this.service = service; }
    @HasPermission("classics:wangqi:view") @PostMapping("page") public PageResponse<WangqiDocumentResponse> page(@Valid @RequestBody WangqiDocumentRequest request) { return PageResponseHelper.fromPageResult(service.page(WangqiDocumentInterfaceAssembler.toQuery(request), PageInterfaceAssembler.toPageQuery(request)), WangqiDocumentInterfaceAssembler::toResponse); }
    @HasPermission("classics:wangqi:view") @GetMapping("{id}") public WangqiDocumentResponse get(@PathVariable Long id) { return WangqiDocumentInterfaceAssembler.toResponse(service.get(id)); }
    @HasPermission("classics:wangqi:view") @PostMapping("timeline") public List<WangqiDocumentResponse> timeline(@Valid @RequestBody WangqiDocumentRequest request) { return service.listTimeline(WangqiDocumentInterfaceAssembler.toQuery(request)).stream().map(WangqiDocumentInterfaceAssembler::toResponse).toList(); }
    @HasPermission("classics:wangqi:edit") @PostMapping("save") public Long save(@Valid @RequestBody WangqiDocumentRequest request) { return service.save(WangqiDocumentInterfaceAssembler.toSaveCommand(request)); }
    @HasPermission("classics:wangqi:delete") @PostMapping("{id}/delete") public void delete(@PathVariable Long id) { service.delete(id); }
}
