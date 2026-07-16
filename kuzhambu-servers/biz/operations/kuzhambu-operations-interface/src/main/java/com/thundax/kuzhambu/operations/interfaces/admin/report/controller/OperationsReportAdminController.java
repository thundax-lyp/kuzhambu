package com.thundax.kuzhambu.operations.interfaces.admin.report.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.IgnoreSysLogger;
import com.thundax.kuzhambu.common.web.annotation.PostJsonApiExempt;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportDetailQuery;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDownloadResult;
import com.thundax.kuzhambu.operations.application.report.service.ReportApplicationService;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.interfaces.admin.report.assembler.OperationsReportInterfaceAssembler;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportDetailRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportGenerateRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request.OperationsReportPageRequest;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportDetailResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportGenerateResponse;
import com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response.OperationsReportPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public OperationsReportGenerateResponse generate(@Valid @RequestBody OperationsReportGenerateRequest request) {
        return OperationsReportInterfaceAssembler.toResponse(
                reportApplicationService.generate(OperationsReportInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "分页查询报表任务", description = "operations:report:view")
    @HasPermission("operations:report:view")
    @IgnoreSysLogger
    @PostMapping("page")
    public PageResponse<OperationsReportPageResponse> page(@Valid @RequestBody OperationsReportPageRequest request) {
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
    public OperationsReportDetailResponse detail(@Valid @RequestBody OperationsReportDetailRequest request) {
        return OperationsReportInterfaceAssembler.toDetailResponse(
                reportApplicationService.detail(OperationsReportInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "下载报表产物", description = "operations:report:view")
    @HasPermission("operations:report:view")
    @SysLogger("下载报表")
    @PostJsonApiExempt(reason = "文件内容需要浏览器直链预览或下载")
    @GetMapping("{reportId}/content")
    public void content(
            @PathVariable("reportId") Long reportId,
            @RequestParam(value = "download", required = false) Boolean download,
            HttpServletResponse response)
            throws IOException {
        OperationsReportDownloadResult result =
                reportApplicationService.download(new OperationsReportDetailQuery(ReportId.of(reportId)));
        response.setContentType(
                StringUtils.defaultIfBlank(result.getContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
        if (result.getContentLength() != null) {
            response.setContentLengthLong(result.getContentLength());
        }
        response.setHeader(
                "Content-Disposition", contentDisposition(resolveFilename(result), Boolean.TRUE.equals(download)));
        try (InputStream inputStream = result.getInputStream()) {
            inputStream.transferTo(response.getOutputStream());
        }
    }

    private static String resolveFilename(OperationsReportDownloadResult result) {
        if (result == null) {
            return "operations-report.bin";
        }
        String filename = StringUtils.defaultIfBlank(result.getArtifactFilename(), result.getStorageOriginalFilename());
        if (StringUtils.isNotBlank(filename)) {
            return filename;
        }
        Long reportId =
                result.getReportId() == null ? null : result.getReportId().value();
        return "operations-report-" + (reportId == null ? "unknown" : reportId) + "." + suffix(result.getFormat());
    }

    private static String suffix(String format) {
        if ("HTML".equalsIgnoreCase(format)) {
            return "html";
        }
        if ("PDF".equalsIgnoreCase(format)) {
            return "pdf";
        }
        return "bin";
    }

    private static String contentDisposition(String originalFilename, boolean download) {
        String disposition = download ? "attachment" : "inline";
        String filename = StringUtils.defaultIfBlank(fileName(originalFilename), "file");
        String asciiFilename = filename.replace("\\", "").replace("\"", "");
        String encodedFilename =
                URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return disposition + "; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + encodedFilename;
    }

    private static String fileName(String originalFilename) {
        if (StringUtils.isBlank(originalFilename)) {
            return null;
        }
        int slashIndex = Math.max(originalFilename.lastIndexOf('/'), originalFilename.lastIndexOf('\\'));
        return slashIndex < 0 ? originalFilename : originalFilename.substring(slashIndex + 1);
    }
}
