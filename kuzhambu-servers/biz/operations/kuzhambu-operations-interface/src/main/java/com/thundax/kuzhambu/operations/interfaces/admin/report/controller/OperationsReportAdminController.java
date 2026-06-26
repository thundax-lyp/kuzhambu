package com.thundax.kuzhambu.operations.interfaces.admin.report.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.operations.application.report.service.ReportApplicationService;
import com.thundax.kuzhambu.operations.interfaces.admin.report.assembler.OperationsReportInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportGenerateRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportAdminResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "运营模块-报表", description = "Operations 报表接口")
@SysLogger(module = {"运营", "报表"})
@RequestMapping("/api/operations/report")
@WrappedApiController
public class OperationsReportAdminController {

    private final ReportApplicationService reportApplicationService;

    public OperationsReportAdminController(ReportApplicationService reportApplicationService) {
        this.reportApplicationService = reportApplicationService;
    }

    @Operation(summary = "发起报表生成", description = "operations:report:generate")
    @HasPermission("operations:report:generate")
    @IgnoreSysLogger
    @PostMapping("generate")
    public OperationsReportAdminResponses.GenerateResponse generate(
            @Valid @RequestBody OperationsReportGenerateRequest request) {
        return OperationsReportInterfaceAssembler.toResponse(
                reportApplicationService.generate(OperationsReportInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "分页查询报表任务", description = "operations:report:view")
    @HasPermission("operations:report:view")
    @IgnoreSysLogger
    @PostMapping("page")
    public PageResponse<OperationsReportAdminResponses.PageResponse> page(
            @Valid @RequestBody OperationsReportPageRequest request) {
        return PageResponseHelper.fromPageResult(
                reportApplicationService.page(
                        OperationsReportInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                OperationsReportInterfaceAssembler::toResponse);
    }

    @Operation(summary = "获取报表任务详情", description = "operations:report:view")
    @HasPermission("operations:report:view")
    @IgnoreSysLogger
    @PostMapping("detail")
    public OperationsReportAdminResponses.DetailResponse detail(
            @Valid @RequestBody OperationsReportDetailRequest request) {
        return OperationsReportInterfaceAssembler.toDetailResponse(
                reportApplicationService.detail(OperationsReportInterfaceAssembler.toQuery(request)));
    }
}
