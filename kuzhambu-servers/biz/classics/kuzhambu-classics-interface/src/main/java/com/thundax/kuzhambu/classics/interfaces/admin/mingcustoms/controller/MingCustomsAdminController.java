package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller;

import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler.MingCustomsInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SysLogger(module = {"古籍", "明代习俗"})
@RequestMapping("/api/classics/ming-customs")
@WrappedApiController
public class MingCustomsAdminController {
    private final MingCustomsApplicationService service;

    public MingCustomsAdminController(MingCustomsApplicationService service) {
        this.service = service;
    }

    @HasPermission("classics:mingcustoms:view")
    @PostMapping("page")
    public PageResponse<MingCustomsResponse> page(@Valid @RequestBody MingCustomsRequest request) {
        return PageResponseHelper.fromPageResult(
                service.page(
                        MingCustomsInterfaceAssembler.toQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                MingCustomsInterfaceAssembler::toResponse);
    }

    @HasPermission("classics:mingcustoms:view")
    @GetMapping("{id}")
    public MingCustomsResponse get(@PathVariable Long id) {
        return MingCustomsInterfaceAssembler.toResponse(service.get(id));
    }

    @HasPermission("classics:mingcustoms:edit")
    @PostMapping("save")
    public Long save(@Valid @RequestBody MingCustomsRequest request) {
        return service.save(MingCustomsInterfaceAssembler.toSaveCommand(request));
    }

    @HasPermission("classics:mingcustoms:edit")
    @PostMapping("{id}/keywords")
    public Long addKeyword(@PathVariable Long id, @Valid @RequestBody MingCustomsRequest request) {
        return service.addKeyword(MingCustomsInterfaceAssembler.toKeywordCommand(id, request));
    }

    @HasPermission("classics:mingcustoms:view")
    @GetMapping("keyword-cloud")
    public List<String> keywordCloud(@RequestParam(required = false) String visibility) {
        return service.listKeywordCloud(visibility);
    }

    @HasPermission("classics:mingcustoms:delete")
    @PostMapping("{id}/delete")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
