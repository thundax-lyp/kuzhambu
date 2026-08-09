package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeQualityReportApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler.KnowledgeQualityReportInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request.QualityReportRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.response.QualityReportResponses;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-质量报告", description = "Knowledge 质量报告")
@SysLogger(module = {"知识", "质量报告"})
@RequestMapping("/api/knowledge/quality/report")
@WrappedApiController
public class KnowledgeQualityReportController {

    private final KnowledgeQualityReportApplicationService qualityReportService;

    public KnowledgeQualityReportController(KnowledgeQualityReportApplicationService qualityReportService) {
        this.qualityReportService = qualityReportService;
    }

    @Operation(summary = "生成质量报告", description = "knowledge:quality-report:generate")
    @HasPermission("knowledge:quality-report:generate")
    @SysLogger("生成质量报告")
    @PostMapping("generate")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public QualityReportResponses.DetailResponse generate(
            @Valid @RequestBody QualityReportRequests.GenerateRequest request) {
        return KnowledgeQualityReportInterfaceAssembler.toResponse(
                qualityReportService.generateReport(KnowledgeQualityReportInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "分页查询质量报告", description = "knowledge:quality-report:view")
    @HasPermission("knowledge:quality-report:view")
    @SysLogger("分页查询质量报告")
    @PostMapping("page")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public PageResponse<QualityReportResponses.ReportResponse> page(
            @Valid @RequestBody QualityReportRequests.PageRequestBody request) {
        return PageResponseHelper.fromPageResult(
                qualityReportService.pageReports(
                        KnowledgeQualityReportInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeQualityReportInterfaceAssembler::toResponse);
    }

    @Operation(summary = "获取质量报告详情", description = "knowledge:quality-report:view")
    @HasPermission("knowledge:quality-report:view")
    @SysLogger("获取质量报告详情")
    @PostMapping("detail")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public QualityReportResponses.DetailResponse detail(
            @Valid @RequestBody QualityReportRequests.DetailRequest request) {
        return KnowledgeQualityReportInterfaceAssembler.toResponse(
                qualityReportService.detail(request == null ? null : request.getReportId()));
    }

    @Operation(summary = "获取最新质量报告", description = "knowledge:quality-report:view")
    @HasPermission("knowledge:quality-report:view")
    @SysLogger("获取最新质量报告")
    @PostMapping("latest")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public QualityReportResponses.DetailResponse latest(
            @Valid @RequestBody QualityReportRequests.LatestRequest request) {
        return KnowledgeQualityReportInterfaceAssembler.toResponse(
                qualityReportService.latest(request == null ? null : request.getGraphVersionId()));
    }

    @Operation(summary = "重提取低质量门类", description = "knowledge:graph:edit")
    @HasPermission("knowledge:graph:edit")
    @SysLogger("重提取低质量门类")
    @PostMapping("reextract-low-quality-category")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    public QualityReportResponses.ReextractResponse reextractLowQualityCategory(
            @Valid @RequestBody QualityReportRequests.ReextractRequest request) {
        return KnowledgeQualityReportInterfaceAssembler.toResponse(qualityReportService.reextractLowQualityCategory(
                KnowledgeQualityReportInterfaceAssembler.toCommand(request)));
    }
}
