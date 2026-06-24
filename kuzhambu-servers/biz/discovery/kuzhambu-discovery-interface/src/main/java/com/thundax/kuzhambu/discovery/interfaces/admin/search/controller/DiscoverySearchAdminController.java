package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.application.search.service.SearchIndexApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchAdminInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchIndexRebuildRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchLogGetRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchLogPageRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchLogDetailResponse;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response.DiscoverySearchLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "发现模块-搜索分析", description = "Discovery 搜索分析接口")
@SysLogger(module = {"发现", "搜索分析"})
@RequestMapping("/api/discovery/search-admin")
@WrappedApiController
public class DiscoverySearchAdminController {

    private final SearchApplicationService searchApplicationService;
    private final SearchIndexApplicationService searchIndexApplicationService;

    public DiscoverySearchAdminController(
            SearchApplicationService searchApplicationService,
            SearchIndexApplicationService searchIndexApplicationService) {
        this.searchApplicationService = searchApplicationService;
        this.searchIndexApplicationService = searchIndexApplicationService;
    }

    @Operation(summary = "分页查询搜索日志", description = "Discovery 搜索日志分页")
    @HasPermission("discovery:search:view")
    @IgnoreSysLogger
    @PostMapping("logs/page")
    public PageResponse<DiscoverySearchLogResponse> pageLogs(
            @Valid @RequestBody DiscoverySearchLogPageRequest request) {
        return PageResponseHelper.fromPageResult(
                searchApplicationService.pageLogs(DiscoverySearchAdminInterfaceAssembler.toQuery(request)),
                DiscoverySearchAdminInterfaceAssembler::toResponse);
    }

    @Operation(summary = "获取搜索日志详情", description = "Discovery 搜索日志详情")
    @HasPermission("discovery:search:view")
    @IgnoreSysLogger
    @PostMapping("logs/get")
    public DiscoverySearchLogDetailResponse getLog(@Valid @RequestBody DiscoverySearchLogGetRequest request) {
        return DiscoverySearchAdminInterfaceAssembler.toDetailResponse(
                searchApplicationService.getLog(request.getSearchLogId()));
    }

    @Operation(summary = "手动重建搜索索引", description = "Discovery 搜索索引全量重建")
    @HasPermission("discovery:search:edit")
    @IgnoreSysLogger
    @PostMapping("index/rebuild")
    public Integer rebuildIndex(@Valid @RequestBody DiscoverySearchIndexRebuildRequest request) {
        return searchIndexApplicationService.rebuildIndex();
    }
}
