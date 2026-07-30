package com.thundax.kuzhambu.operations.application.report.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.report.command.OperationsReportGenerateCommand;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportDetailQuery;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportPageQuery;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDetailResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDownloadResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportGenerateResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportPageResult;
import com.thundax.kuzhambu.operations.application.report.service.ReportApplicationService;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportTaskExecutor;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import com.thundax.kuzhambu.storage.facade.request.OpenStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.OpenStorageFacadeResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class ReportApplicationServiceImpl implements ReportApplicationService {

    private static final String REPORT_STORAGE_OWNER_TYPE = "USER";
    private static final String REPORT_STORAGE_OWNER_ID = "system";

    private final ReportRepository reportRepository;
    private final OperationsReportTaskExecutor taskExecutor;
    private final StorageFacade storageFacade;

    public ReportApplicationServiceImpl(ReportRepository reportRepository, OperationsReportTaskExecutor taskExecutor) {
        this(reportRepository, taskExecutor, null);
    }

    @Autowired
    public ReportApplicationServiceImpl(
            ReportRepository reportRepository, OperationsReportTaskExecutor taskExecutor, StorageFacade storageFacade) {
        this.reportRepository = reportRepository;
        this.taskExecutor = taskExecutor;
        this.storageFacade = storageFacade;
    }

    @Override
    public OperationsReportGenerateResult generate(OperationsReportGenerateCommand command) {
        validateGenerateCommand(command);
        Instant now = Instant.now();
        ReportRecord record = new ReportRecord(
                null,
                command.getReportType(),
                command.getFormat(),
                command.getPeriodStart(),
                command.getPeriodEnd(),
                requestId(),
                traceId(),
                "2026.06.26",
                null,
                null,
                ReportStatus.PENDING,
                null,
                command.getRequesterUserId(),
                now,
                null);
        ReportId reportId = reportRepository.insert(record);
        taskExecutor.executeAsync(reportId);
        return new OperationsReportGenerateResult(reportId, ReportStatus.PENDING.value());
    }

    @Override
    public PageResult<OperationsReportPageResult> page(OperationsReportPageQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        PageResult<ReportRecord> recordPage = reportRepository.page(
                query == null ? null : query.getReportType(),
                query == null ? null : query.getFormat(),
                query == null ? null : query.getReportStatus(),
                query == null ? null : query.getRequesterUserId(),
                query == null ? null : query.getPeriodStart(),
                query == null ? null : query.getPeriodEnd(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
        List<OperationsReportPageResult> results =
                recordPage.getRecords().stream().map(this::toPageResult).collect(Collectors.toList());
        return PageResult.of(recordPage.getPageNo(), recordPage.getPageSize(), recordPage.getTotalCount(), results);
    }

    @Override
    public OperationsReportDetailResult detail(OperationsReportDetailQuery query) {
        ReportRecord record = reportRepository.getById(query == null ? null : query.getReportId());
        return toDetailResult(record);
    }

    @Override
    public OperationsReportDownloadResult download(OperationsReportDetailQuery query) {
        ReportRecord record = reportRepository.getById(query == null ? null : query.getReportId());
        validateDownloadRecord(record);
        OpenStorageFacadeResponse content = storageFacade.open(OpenStorageFacadeRequest.builder()
                .storageObjectId(record.getStorageObjectId())
                .ownerType(REPORT_STORAGE_OWNER_TYPE)
                .ownerId(REPORT_STORAGE_OWNER_ID)
                .build());
        if (content == null || content.getInputStream() == null) {
            throw new IllegalStateException("Operations report artifact content is not readable.");
        }
        StorageObjectFacadeDto storedObject = content.getStoredObject();
        return new OperationsReportDownloadResult(
                record.getId(),
                record.getFormat(),
                record.getArtifactFilename(),
                storedObject == null ? null : storedObject.getContentType(),
                storedObject == null ? null : storedObject.getSize(),
                storedObject == null ? null : storedObject.getOriginalFilename(),
                content.getInputStream());
    }

    private OperationsReportPageResult toPageResult(ReportRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsReportPageResult(
                record.getId(),
                record.getReportType(),
                record.getFormat(),
                record.getPeriodStart(),
                record.getPeriodEnd(),
                record.getStorageObjectId(),
                record.getArtifactFilename(),
                record.getReportStatus() == null
                        ? null
                        : record.getReportStatus().value(),
                record.getFailureReason(),
                record.getRequesterUserId(),
                record.getRequestedAt(),
                record.getCompletedAt());
    }

    private OperationsReportDetailResult toDetailResult(ReportRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsReportDetailResult(
                record.getId(),
                record.getReportType(),
                record.getFormat(),
                record.getPeriodStart(),
                record.getPeriodEnd(),
                record.getRequestId(),
                record.getTraceId(),
                record.getTemplateVersion(),
                record.getStorageObjectId(),
                record.getArtifactFilename(),
                record.getReportStatus() == null
                        ? null
                        : record.getReportStatus().value(),
                record.getFailureReason(),
                record.getRequesterUserId(),
                record.getRequestedAt(),
                record.getCompletedAt());
    }

    private void validateGenerateCommand(OperationsReportGenerateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Operations report generate command must not be null.");
        }
        if (StringUtils.isBlank(command.getReportType())) {
            throw new IllegalArgumentException("Operations report type must not be blank.");
        }
        if (StringUtils.isBlank(command.getFormat())) {
            throw new IllegalArgumentException("Operations report format must not be blank.");
        }
        if (command.getPeriodStart() == null || command.getPeriodEnd() == null) {
            throw new IllegalArgumentException("Operations report period must not be null.");
        }
        if (command.getPeriodStart().isAfter(command.getPeriodEnd())) {
            throw new IllegalArgumentException("Operations report periodStart must not be after periodEnd.");
        }
    }

    private void validateDownloadRecord(ReportRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Operations report does not exist.");
        }
        if (record.getReportStatus() != ReportStatus.SUCCEEDED) {
            throw new IllegalStateException("Operations report artifact is not ready.");
        }
        if (record.getStorageObjectId() == null) {
            throw new IllegalStateException("Operations report artifact storage object is missing.");
        }
        if (storageFacade == null) {
            throw new IllegalStateException("Operations report storage facade is not available.");
        }
    }

    private String requestId() {
        return "operations-report-" + UUID.randomUUID();
    }

    private String traceId() {
        return "operations-trace-" + UUID.randomUUID();
    }
}
