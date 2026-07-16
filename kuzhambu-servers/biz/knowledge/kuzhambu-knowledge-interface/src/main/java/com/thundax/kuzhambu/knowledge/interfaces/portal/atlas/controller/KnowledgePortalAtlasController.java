package com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.assembler.KnowledgePortalAtlasInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.request.KnowledgePortalAtlasQuery;
import com.thundax.kuzhambu.knowledge.interfaces.portal.atlas.controller.response.KnowledgePortalAtlasResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
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
    @PostJsonApiExempt(reason = "存量 GET JSON 数据接口，待迁移为 POST JSON")
    @GetMapping
    public KnowledgePortalAtlasResponse getAtlas(@Valid KnowledgePortalAtlasQuery request) {
        return KnowledgePortalAtlasInterfaceAssembler.toResponse(knowledgePortalReadApplicationService.getAtlas(
                new com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalAtlasQuery(
                        request.getLevel(),
                        request.getCategoryCode(),
                        request.getEntityId(),
                        request.getKnowledgeBase(),
                        request.getKeyword(),
                        request.getTag(),
                        request.getTimeRange())));
    }
}
