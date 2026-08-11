package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.discovery.application.search.query.SearchEventQuery;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchStatisticsInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchEventGetRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchEventPageRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchIndexRebuildRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchStatisticsSummaryRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchEventDetailResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchEventResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchStatisticsSummaryResponse;
import com.thundax.kuzhambu.discovery.interfaces.common.DiscoveryInterfaceIdCodec;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "发现模块-检索统计", description = "Discovery 检索统计接口")
@SysLogger(module = {"发现", "检索统计"})
@RequestMapping("/api/discovery/search-statistics")
@WrappedApiController
public class DiscoverySearchStatisticsController {

    private final SearchApplicationService searchApplicationService;
    private final SearchIndexApplicationService searchIndexApplicationService;

    public DiscoverySearchStatisticsController(
            SearchApplicationService searchApplicationService,
            SearchIndexApplicationService searchIndexApplicationService) {
        this.searchApplicationService = searchApplicationService;
        this.searchIndexApplicationService = searchIndexApplicationService;
    }

    @Operation(summary = "分页查询检索统计事件", description = "Discovery 检索统计事件分页")
    @HasPermission("discovery:search:view")
    @IgnoreSysLogger
    @PostMapping("events/page")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public PageResponse<DiscoverySearchEventResponse> pageEvents(
            @Valid @RequestBody DiscoverySearchEventPageRequest request) {
        return PageResponseHelper.fromPageResult(
                searchApplicationService.pageEvents(
                        DiscoverySearchStatisticsInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                DiscoverySearchStatisticsInterfaceAssembler::toResponse);
    }

    @Operation(summary = "获取检索统计事件详情", description = "Discovery 检索统计事件详情")
    @HasPermission("discovery:search:view")
    @IgnoreSysLogger
    @PostMapping("events/get")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoverySearchEventDetailResponse getEvent(@Valid @RequestBody DiscoverySearchEventGetRequest request) {
        return DiscoverySearchStatisticsInterfaceAssembler.toDetailResponse(
                searchApplicationService.getEvent(new SearchEventQuery(
                        DiscoveryInterfaceIdCodec.toLongValue(request.getId()), null, null, null, null, null, null)));
    }

    @Operation(summary = "获取检索统计摘要", description = "Discovery 检索统计摘要")
    @HasPermission("discovery:search:view")
    @IgnoreSysLogger
    @PostMapping("summary")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public DiscoverySearchStatisticsSummaryResponse getStatisticsSummary(
            @Valid @RequestBody DiscoverySearchStatisticsSummaryRequest request) {
        return DiscoverySearchStatisticsInterfaceAssembler.toResponse(searchApplicationService.getStatisticsSummary(
                DiscoverySearchStatisticsInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "手动重建搜索索引", description = "Discovery 搜索索引全量重建")
    @HasPermission("discovery:search:edit")
    @IgnoreSysLogger
    @PostMapping("index/rebuild")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public Integer rebuildIndex(@Valid @RequestBody DiscoverySearchIndexRebuildRequest request) {
        return searchIndexApplicationService.rebuildIndex();
    }
}
