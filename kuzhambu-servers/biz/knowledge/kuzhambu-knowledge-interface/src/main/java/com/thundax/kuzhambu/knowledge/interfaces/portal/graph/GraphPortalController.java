package com.thundax.kuzhambu.knowledge.interfaces.portal.graph;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPortalApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.assembler.GraphPortalInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request.GraphPortalMaterialRequest;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.response.GraphPortalMaterialResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识门户-图谱素材", description = "门户已发布素材图谱查询")
@PublicApi
@RequestMapping("/api/portal/knowledge/graph")
@WrappedApiController
public class GraphPortalController {
    private final GraphPortalApplicationService service;

    public GraphPortalController(GraphPortalApplicationService service) {
        this.service = service;
    }

    @Operation(summary = "查询门户素材已发布图谱", description = "公开访问")
    @PostMapping("material/get")
    public GraphPortalMaterialResponse getMaterial(@Valid @RequestBody GraphPortalMaterialRequest request) {
        return GraphPortalInterfaceAssembler.toResponse(
                service.getMaterialGraph(GraphPortalInterfaceAssembler.toQuery(request)));
    }
}
