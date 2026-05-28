package com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller;

import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.assembler.WangqiDocumentInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.request.WangqiDocumentRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.wangqi.controller.response.WangqiDocumentResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "古籍模块-王圻文档", description = "王圻文档")
@SysLogger(module = {"古籍", "王圻文档"})
@RequestMapping("/api/classics/wangqi/documents")
@WrappedApiController
public class WangqiDocumentAdminController {
    private final WangqiDocumentApplicationService service;

    public WangqiDocumentAdminController(WangqiDocumentApplicationService service) {
        this.service = service;
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
    @GetMapping("{id}")
    public WangqiDocumentResponse get(@PathVariable Long id) {
        return WangqiDocumentInterfaceAssembler.toResponse(service.get(WangqiDocumentIdCodec.toDomain(id)));
    }

    @Operation(summary = "查询王圻时间线", description = "classics:wangqi:view")
    @ApiImplicitParams({})
    @HasPermission("classics:wangqi:view")
    @SysLogger(value = "时间线")
    @PostMapping("timeline")
    public List<WangqiDocumentResponse> timeline(@Valid @RequestBody WangqiDocumentRequest request) {
        return service.listTimeline(WangqiDocumentInterfaceAssembler.toQuery(request)).stream()
                .map(WangqiDocumentInterfaceAssembler::toResponse)
                .toList();
    }

    @Operation(summary = "保存王圻文档", description = "classics:wangqi:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:wangqi:edit")
    @SysLogger(value = "保存")
    @PostMapping("save")
    public WangqiDocumentResponse save(@Valid @RequestBody WangqiDocumentRequest request) {
        WangqiDocumentId id = service.save(WangqiDocumentInterfaceAssembler.toSaveCommand(request));
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
}
