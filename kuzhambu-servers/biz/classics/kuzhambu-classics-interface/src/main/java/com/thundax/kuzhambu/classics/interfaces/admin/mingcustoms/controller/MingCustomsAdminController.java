package com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller;

import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.command.MingCustomsKeywordSortCommand;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsKeywordIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.assembler.MingCustomsInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.request.MingCustomsKeywordSortRequest;
import com.thundax.kuzhambu.classics.interfaces.admin.mingcustoms.controller.response.MingCustomsResponse;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.exception.AdminResponseExceptions;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.request.RequestListHelper;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "古籍模块-明代习俗", description = "明代习俗")
@SysLogger(module = {"古籍", "明代习俗"})
@RequestMapping("/api/classics/ming-customs")
@WrappedApiController
public class MingCustomsAdminController {
    private final MingCustomsApplicationService service;

    public MingCustomsAdminController(MingCustomsApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询明代习俗", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "分页查询")
    @PostMapping("page")
    public PageResponse<MingCustomsResponse> page(@Valid @RequestBody MingCustomsRequest request) {
        return PageResponseHelper.fromPageResult(
                service.page(
                        MingCustomsInterfaceAssembler.toQuery(request), PageInterfaceAssembler.toPageQuery(request)),
                MingCustomsInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查看明代习俗", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "详情")
    @GetMapping("{id}")
    public MingCustomsResponse get(@PathVariable Long id) {
        return MingCustomsInterfaceAssembler.toResponse(service.get(MingCustomsEntryIdCodec.toDomain(id)));
    }

    @Operation(summary = "保存明代习俗", description = "classics:mingcustoms:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:edit")
    @SysLogger(value = "保存")
    @PostMapping("save")
    public MingCustomsResponse save(@Valid @RequestBody MingCustomsRequest request) {
        MingCustomsEntryId id = service.save(MingCustomsInterfaceAssembler.toSaveCommand(request));
        return MingCustomsResponse.builder().id(id == null ? null : id.value()).build();
    }

    @Operation(summary = "新增明代习俗关键词", description = "classics:mingcustoms:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:edit")
    @SysLogger(value = "新增关键词")
    @PostMapping("keywords")
    public MingCustomsResponse addKeyword(@Valid @RequestBody MingCustomsRequest request) {
        return MingCustomsResponse.builder()
                .id(MingCustomsKeywordIdCodec.toValue(
                        service.addKeyword(MingCustomsInterfaceAssembler.toKeywordCommand(request.getId(), request))))
                .build();
    }

    @Operation(summary = "排序明代习俗关键词", description = "classics:mingcustoms:edit")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:edit")
    @SysLogger(value = "关键词排序")
    @PostMapping("keywords/sort")
    public Boolean sortKeywords(@Valid @RequestBody MingCustomsKeywordSortRequest request) {
        service.sortKeywords(new MingCustomsKeywordSortCommand(
                RequestListHelper.map(
                        RequestListHelper.presentUnique(
                                request == null ? null : request.getOrderedIds(),
                                "orderedIds",
                                AdminResponseExceptions::invalidParameter),
                        MingCustomsKeywordIdCodec::toDomain),
                request == null ? null : request.getSortDirection()));
        return true;
    }

    @Operation(summary = "查询明代习俗关键词云", description = "classics:mingcustoms:view")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:view")
    @SysLogger(value = "关键词云")
    @GetMapping("keyword-cloud")
    public List<String> keywordCloud(@RequestParam(required = false) String visibility) {
        return service.listKeywordCloud(visibility);
    }

    @Operation(summary = "删除明代习俗", description = "classics:mingcustoms:delete")
    @ApiImplicitParams({})
    @HasPermission("classics:mingcustoms:delete")
    @SysLogger(value = "删除")
    @PostMapping("delete")
    public void delete(@Valid @RequestBody MingCustomsRequest request) {
        service.delete(MingCustomsEntryIdCodec.toDomain(request.getId()));
    }

}
