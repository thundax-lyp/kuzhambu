package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.assembler.DiscoverySearchAdminInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchClickRequest;
import com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request.DiscoverySearchRequest;
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
public class DiscoverySearchAdminQueryController {

    private final SearchApplicationService searchApplicationService;

    public DiscoverySearchAdminQueryController(SearchApplicationService searchApplicationService) {
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
        return DiscoverySearchAdminInterfaceAssembler.toSearchResponse(
                searchApplicationService.search(DiscoverySearchAdminInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "记录后台搜索点击", description = "后台搜索点击")
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
    public Boolean click(@Valid @RequestBody DiscoverySearchClickRequest request) {
        return searchApplicationService.recordClick(DiscoverySearchAdminInterfaceAssembler.toCommand(request));
    }
}
