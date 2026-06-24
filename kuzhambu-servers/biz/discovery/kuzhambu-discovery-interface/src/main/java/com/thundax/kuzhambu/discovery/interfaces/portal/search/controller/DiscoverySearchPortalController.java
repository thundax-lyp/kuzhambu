package com.thundax.kuzhambu.discovery.interfaces.portal.search.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.discovery.application.search.service.SearchApplicationService;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.assembler.DiscoverySearchPortalInterfaceAssembler;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchClickRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request.DiscoverySearchRequest;
import com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response.DiscoverySearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "发现模块-Portal 搜索", description = "Discovery Portal 搜索接口")
@PublicApi
@RequestMapping("/api/portal/discovery/search")
@WrappedApiController
public class DiscoverySearchPortalController {

    private final SearchApplicationService searchApplicationService;

    public DiscoverySearchPortalController(SearchApplicationService searchApplicationService) {
        this.searchApplicationService = searchApplicationService;
    }

    @Operation(summary = "执行搜索", description = "Portal 搜索")
    @PostMapping("search")
    public DiscoverySearchResponse search(@Valid @RequestBody DiscoverySearchRequest request) {
        return DiscoverySearchPortalInterfaceAssembler.toResponse(
                searchApplicationService.search(DiscoverySearchPortalInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "记录点击", description = "Portal 搜索点击")
    @PostMapping("click")
    public Boolean click(@Valid @RequestBody DiscoverySearchClickRequest request) {
        return searchApplicationService.recordClick(DiscoverySearchPortalInterfaceAssembler.toCommand(request));
    }
}
