package com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.portal.KnowledgePortalReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.assembler.KnowledgePortalLineageInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.portal.lineage.controller.response.KnowledgePortalLineageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-Portal 世系图", description = "Knowledge Portal 世系图只读接口")
@PublicApi
@RequestMapping("/api/portal/knowledge/lineage")
@WrappedApiController
public class KnowledgePortalLineageController {

    private final KnowledgePortalReadApplicationService knowledgePortalReadApplicationService;

    public KnowledgePortalLineageController(
            KnowledgePortalReadApplicationService knowledgePortalReadApplicationService) {
        this.knowledgePortalReadApplicationService = knowledgePortalReadApplicationService;
    }

    @Operation(summary = "获取知识门户世系图", description = "Portal 世系图")
    @GetMapping
    public KnowledgePortalLineageResponse getLineage(@Valid Query request) {
        return KnowledgePortalLineageInterfaceAssembler.toResponse(knowledgePortalReadApplicationService.getLineage(
                KnowledgePortalLineageInterfaceAssembler.toQuery(request)));
    }

    @Getter
    @Setter
    public static class Query {
        private Long versionId;
        private Long focusNodeId;
        private Long focusRelationId;
        private String keyword;
        private String nodeType;
        private String relationType;
        private String confirmationStatus;
        private Integer depth;
    }
}
