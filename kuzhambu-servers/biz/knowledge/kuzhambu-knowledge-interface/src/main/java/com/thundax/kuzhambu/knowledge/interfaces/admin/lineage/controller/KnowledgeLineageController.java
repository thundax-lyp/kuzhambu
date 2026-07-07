package com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.lineage.service.KnowledgeLineageReadApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.assembler.KnowledgeLineageInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller.request.LineageCanvasRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.lineage.controller.response.LineageCanvasResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-世系图浏览", description = "正式世系图画布读取")
@SysLogger(module = {"知识", "世系图浏览"})
@RequestMapping("/api/knowledge/lineage")
@WrappedApiController
public class KnowledgeLineageController {

    private final KnowledgeLineageReadApplicationService lineageReadApplicationService;

    public KnowledgeLineageController(KnowledgeLineageReadApplicationService lineageReadApplicationService) {
        this.lineageReadApplicationService = lineageReadApplicationService;
    }

    @Operation(summary = "读取世系画布", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "读取世系画布")
    @PostMapping("canvas")
    public LineageCanvasResponse getCanvas(@Valid @RequestBody LineageCanvasRequest request) {
        return KnowledgeLineageInterfaceAssembler.toResponse(
                lineageReadApplicationService.getCanvas(KnowledgeLineageInterfaceAssembler.toQuery(request)));
    }
}
