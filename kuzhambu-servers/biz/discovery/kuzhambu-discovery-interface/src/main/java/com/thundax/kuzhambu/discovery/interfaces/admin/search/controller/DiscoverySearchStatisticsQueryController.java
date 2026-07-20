package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchClickEventRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchPreviewRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchPreviewResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "发现模块-后台检索", description = "Discovery 后台检索接口")
@SysLogger(module = {"发现", "检索"})
@RequestMapping("/api/discovery/search")
@WrappedApiController
public class DiscoverySearchStatisticsQueryController {

    private final SearchApplicationService searchApplicationService;

    public DiscoverySearchStatisticsQueryController(SearchApplicationService searchApplicationService) {
        this.searchApplicationService = searchApplicationService;
    }

    @Operation(summary = "执行后台检索", description = "后台检索公开已发布内容")
    @HasPermission("discovery:search:view")
    @IgnoreSysLogger
    @PostMapping("search")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoverySearchResponse search(@Valid @RequestBody DiscoverySearchRequest request) {
        return DiscoverySearchStatisticsInterfaceAssembler.toSearchResponse(
                searchApplicationService.search(DiscoverySearchStatisticsInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "预览后台搜索命中", description = "读取搜索索引中的命中内容")
    @HasPermission("discovery:search:view")
    @IgnoreSysLogger
    @PostMapping("preview")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoverySearchPreviewResponse preview(@Valid @RequestBody DiscoverySearchPreviewRequest request) {
        return DiscoverySearchStatisticsInterfaceAssembler.toResponse(
                searchApplicationService.getPreview(DiscoverySearchStatisticsInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "记录后台检索点击事件", description = "后台检索点击事件")
    @HasPermission("discovery:search:view")
    @IgnoreSysLogger
    @PostMapping("click")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public Boolean click(@Valid @RequestBody DiscoverySearchClickEventRequest request) {
        return searchApplicationService.recordClick(DiscoverySearchStatisticsInterfaceAssembler.toCommand(request));
    }
}
