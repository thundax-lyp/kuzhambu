package com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.home.assembler.KnowledgePortalHomeInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.request.KnowledgePortalHomeQuery;
import com.thundax.kuzhambu.knowledge.interfaces.portal.home.controller.response.KnowledgePortalHomeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-Portal 首页", description = "Knowledge Portal 首页只读接口")
@PublicApi
@RequestMapping("/api/portal/knowledge/home")
@WrappedApiController
public class KnowledgePortalHomeController {

    private final KnowledgePortalReadApplicationService knowledgePortalReadApplicationService;

    public KnowledgePortalHomeController(KnowledgePortalReadApplicationService knowledgePortalReadApplicationService) {
        this.knowledgePortalReadApplicationService = knowledgePortalReadApplicationService;
    }

    @Operation(summary = "获取知识门户首页", description = "Portal 首页")
    @PostMapping("get")
    public KnowledgePortalHomeResponse getHome(@Valid @RequestBody KnowledgePortalHomeQuery request) {
        return KnowledgePortalHomeInterfaceAssembler.toResponse(knowledgePortalReadApplicationService.getHome());
    }
}
