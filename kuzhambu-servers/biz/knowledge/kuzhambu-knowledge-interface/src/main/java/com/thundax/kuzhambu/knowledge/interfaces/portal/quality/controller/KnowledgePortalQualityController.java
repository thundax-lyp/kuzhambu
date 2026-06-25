package com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.quality.assembler.KnowledgePortalQualityInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.request.KnowledgePortalQualityQuery;
import com.thundax.kuzhambu.knowledge.interfaces.portal.quality.controller.response.KnowledgePortalQualityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-Portal 质量页", description = "Knowledge Portal 质量总览只读接口")
@PublicApi
@RequestMapping("/api/portal/knowledge/quality")
@WrappedApiController
public class KnowledgePortalQualityController {

    private final KnowledgePortalReadApplicationService knowledgePortalReadApplicationService;

    public KnowledgePortalQualityController(
            KnowledgePortalReadApplicationService knowledgePortalReadApplicationService) {
        this.knowledgePortalReadApplicationService = knowledgePortalReadApplicationService;
    }

    @Operation(summary = "获取知识门户质量页数据", description = "Portal 质量页")
    @GetMapping
    public KnowledgePortalQualityResponse getQuality(@Valid KnowledgePortalQualityQuery request) {
        return KnowledgePortalQualityInterfaceAssembler.toResponse(knowledgePortalReadApplicationService.getQuality());
    }
}
