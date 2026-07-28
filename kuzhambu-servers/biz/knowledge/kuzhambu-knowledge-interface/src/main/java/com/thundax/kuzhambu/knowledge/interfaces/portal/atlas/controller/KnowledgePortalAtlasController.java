package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.assembler.KnowledgePortalAtlasInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request.KnowledgePortalAtlasQuery;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.response.KnowledgePortalAtlasResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-Portal 浏览页", description = "Knowledge Portal 图谱浏览只读接口")
@PublicApi
@RequestMapping("/api/portal/knowledge/atlas")
@WrappedApiController
public class KnowledgePortalAtlasController {

    private final KnowledgePortalReadApplicationService knowledgePortalReadApplicationService;

    public KnowledgePortalAtlasController(KnowledgePortalReadApplicationService knowledgePortalReadApplicationService) {
        this.knowledgePortalReadApplicationService = knowledgePortalReadApplicationService;
    }

    @Operation(summary = "获取知识门户浏览页数据", description = "Portal 浏览页")
    @PostMapping("get")
    public KnowledgePortalAtlasResponse getAtlas(@Valid @RequestBody KnowledgePortalAtlasQuery request) {
        KnowledgePortalAtlasQuery effectiveRequest = request == null ? new KnowledgePortalAtlasQuery() : request;
        return KnowledgePortalAtlasInterfaceAssembler.toResponse(knowledgePortalReadApplicationService.getAtlas(
                new com.thundax.kuzhambu.knowledge.application.portal.query.KnowledgePortalAtlasQuery(
                        effectiveRequest.getLevel(),
                        effectiveRequest.getCategoryCode(),
                        effectiveRequest.getEntityId(),
                        effectiveRequest.getKnowledgeBase(),
                        effectiveRequest.getKeyword(),
                        effectiveRequest.getTag(),
                        effectiveRequest.getTimeRange())));
    }
}
