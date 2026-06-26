package com.thundax.kuzhambu.operations.application.report.support;

import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportArtifactResult;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSnapshot;
import com.thundax.kuzhambu.operations.domain.report.client.OperationsWorkerRenderClient;
import com.thundax.kuzhambu.operations.domain.report.client.dto.OperationsWorkerRenderDtos;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DefaultOperationsReportTaskExecutor implements OperationsReportTaskExecutor {

    private final ReportRepository reportRepository;
    private final OperationsWorkerRenderClient operationsWorkerRenderClient;
    private final OperationsReportSnapshotAssembler snapshotAssembler;

    public DefaultOperationsReportTaskExecutor(
            ReportRepository reportRepository,
            OperationsWorkerRenderClient operationsWorkerRenderClient,
            OperationsReportSnapshotAssembler snapshotAssembler) {
        this.reportRepository = reportRepository;
        this.operationsWorkerRenderClient = operationsWorkerRenderClient;
        this.snapshotAssembler = snapshotAssembler;
    }

    @Override
    public void executeAsync(ReportId reportId) {
        CompletableFuture.runAsync(() -> execute(reportId));
    }

    @Override
    public OperationsReportArtifactResult execute(ReportId reportId) {
        ReportRecord record = reportRepository.getById(reportId);
        if (record == null) {
            return null;
        }
        try {
            markProcessing(record);
            OperationsReportSnapshot snapshot = snapshotAssembler.assemble(record);
            OperationsWorkerRenderDtos.WorkerRenderRequest workerRequest =
                    snapshotAssembler.toWorkerRequest(record, snapshot);
            OperationsWorkerRenderDtos.WorkerRenderResponse workerResponse =
                    operationsWorkerRenderClient.renderOperationsReport(workerRequest);
            OperationsReportArtifactResult artifactResult = toArtifactResult(workerResponse);
            markSucceeded(record, artifactResult);
            return artifactResult;
        } catch (Exception exception) {
            markFailed(record, exception.getMessage());
            return null;
        }
    }

    private void markProcessing(ReportRecord record) {
        record.setReportStatus(ReportStatus.PROCESSING);
        record.setFailureReason(null);
        record.setCompletedAt(null);
        reportRepository.update(record);
    }

    private void markSucceeded(ReportRecord record, OperationsReportArtifactResult artifactResult) {
        record.setArtifactFilename(artifactResult == null ? null : artifactResult.getFilename());
        record.setReportStatus(ReportStatus.SUCCEEDED);
        record.setFailureReason(null);
        record.setCompletedAt(new Date());
        reportRepository.update(record);
    }

    private void markFailed(ReportRecord record, String failureReason) {
        record.setReportStatus(ReportStatus.FAILED);
        record.setFailureReason(StringUtils.defaultIfBlank(failureReason, "Operations report task failed."));
        record.setCompletedAt(new Date());
        reportRepository.update(record);
    }

    private OperationsReportArtifactResult toArtifactResult(OperationsWorkerRenderDtos.WorkerRenderResponse response) {
        if (response == null) {
            throw new IllegalStateException("Operations report worker returned empty response.");
        }
        if (!StringUtils.equalsIgnoreCase("SUCCEEDED", response.getStatus())) {
            throw new IllegalStateException(resolveFailureReason(response));
        }
        OperationsWorkerRenderDtos.Artifact artifact = response.getArtifact();
        if (artifact == null || StringUtils.isBlank(artifact.getContent())) {
            throw new IllegalStateException("Operations report worker returned empty artifact.");
        }
        byte[] contentBytes = decodeArtifactBytes(artifact);
        return new OperationsReportArtifactResult(
                artifact.getFormat(),
                artifact.getFilename(),
                artifact.getContentType(),
                contentBytes,
                artifact.getSizeBytes() == null ? (long) contentBytes.length : artifact.getSizeBytes(),
                artifact.getSha256());
    }

    private byte[] decodeArtifactBytes(OperationsWorkerRenderDtos.Artifact artifact) {
        if (StringUtils.equalsIgnoreCase("BASE64", artifact.getEncoding())) {
            return java.util.Base64.getDecoder().decode(artifact.getContent());
        }
        return artifact.getContent().getBytes(StandardCharsets.UTF_8);
    }

    private String resolveFailureReason(OperationsWorkerRenderDtos.WorkerRenderResponse response) {
        OperationsWorkerRenderDtos.WorkerRenderError error = response.getError();
        if (error == null) {
            return "Operations report worker execution failed with status " + response.getStatus() + ".";
        }
        String errorCode = StringUtils.defaultIfBlank(error.getCode(), error.getType());
        return StringUtils.defaultIfBlank(errorCode, "WORKER_RENDER_FAILED") + ": "
                + StringUtils.defaultIfBlank(error.getMessage(), "Operations report worker execution failed.");
    }
}
