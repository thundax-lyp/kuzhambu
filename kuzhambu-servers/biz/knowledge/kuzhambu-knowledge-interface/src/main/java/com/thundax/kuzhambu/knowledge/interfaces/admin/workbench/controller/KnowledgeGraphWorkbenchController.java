package com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.workbench.service.KnowledgeGraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.assembler.KnowledgeGraphWorkbenchInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.request.KnowledgeGraphWorkbenchRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.workbench.controller.response.KnowledgeGraphWorkbenchResponses;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-图谱工作台", description = "稿件驱动的知识图谱工作台")
@SysLogger(module = {"知识", "图谱工作台"})
@RequestMapping("/api/knowledge/graph-workbench")
@WrappedApiController
public class KnowledgeGraphWorkbenchController {

    private final KnowledgeGraphWorkbenchApplicationService workbenchApplicationService;

    public KnowledgeGraphWorkbenchController(KnowledgeGraphWorkbenchApplicationService workbenchApplicationService) {
        this.workbenchApplicationService = workbenchApplicationService;
    }

    @Operation(summary = "查询稿件树", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "查询稿件树")
    @PostMapping("manuscript-tree")
    public List<KnowledgeGraphWorkbenchResponses.ManuscriptTreeNodeResponse> listManuscriptTree(
            @Valid @RequestBody KnowledgeGraphWorkbenchRequests.ManuscriptTreeRequest request) {
        return KnowledgeGraphWorkbenchInterfaceAssembler.toTreeResponses(workbenchApplicationService.listManuscriptTree(
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getParentKey(),
                request == null ? null : request.getKeyword(),
                request == null ? null : request.getGraphStatus()));
    }

    @Operation(summary = "查询稿件图谱详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "查询稿件图谱详情")
    @PostMapping("manuscript/get")
    public KnowledgeGraphWorkbenchResponses.ManuscriptDetailResponse getManuscript(
            @Valid @RequestBody KnowledgeGraphWorkbenchRequests.ManuscriptRequest request) {
        return KnowledgeGraphWorkbenchInterfaceAssembler.toResponse(workbenchApplicationService.getManuscript(
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId()));
    }

    @Operation(summary = "按稿件抽取图谱", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "按稿件抽取图谱")
    @PostMapping("manuscript/extract")
    public GraphExtractionResponses.TaskResponse extractManuscript(
            @Valid @RequestBody KnowledgeGraphWorkbenchRequests.ManuscriptExtractRequest request) {
        return KnowledgeGraphWorkbenchInterfaceAssembler.toTaskResponse(workbenchApplicationService.extractManuscript(
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getTaskType(),
                currentActorId()));
    }

    @Operation(summary = "查询稿件最新候选", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "查询稿件最新候选")
    @PostMapping("candidate/get")
    public KnowledgeGraphWorkbenchResponses.CandidateSummaryResponse getLatestCandidate(
            @Valid @RequestBody KnowledgeGraphWorkbenchRequests.CandidateRequest request) {
        return KnowledgeGraphWorkbenchInterfaceAssembler.toResponse(workbenchApplicationService.getLatestCandidate(
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getTaskType()));
    }

    @Operation(summary = "应用稿件候选", description = "knowledge:graph:apply")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:apply")
    @SysLogger(value = "应用稿件候选")
    @PostMapping("candidate/apply")
    public KnowledgeGraphWorkbenchResponses.CandidateApplyResponse applyCandidate(
            @Valid @RequestBody KnowledgeGraphWorkbenchRequests.CandidateApplyRequest request) {
        return KnowledgeGraphWorkbenchInterfaceAssembler.toResponse(
                workbenchApplicationService.applyCandidate(request == null ? null : request.getTaskId()));
    }

    private Long currentActorId() {
        String subjectId = KuzhambuContextHolder.currentSubjectId();
        if (subjectId == null || subjectId.trim().isEmpty()) {
            throw new BizException("Current subject is required");
        }
        try {
            return Long.valueOf(subjectId);
        } catch (NumberFormatException ex) {
            throw new BizException("Current subject is not a numeric user id");
        }
    }
}
