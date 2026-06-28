package com.thundax.kuzhambu.operations.application.report.support;

import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportArtifactResult;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSnapshot;
import com.thundax.kuzhambu.operations.domain.report.client.OperationsWorkerRenderClient;
import com.thundax.kuzhambu.operations.domain.report.client.dto.OperationsWorkerRenderDtos;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import com.thundax.kuzhambu.storage.facade.StorageUploadFacade;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;
import java.io.ByteArrayInputStream;
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
    private final StorageUploadFacade storageUploadFacade;

    public DefaultOperationsReportTaskExecutor(
            ReportRepository reportRepository,
            OperationsWorkerRenderClient operationsWorkerRenderClient,
            OperationsReportSnapshotAssembler snapshotAssembler,
            StorageUploadFacade storageUploadFacade) {
        this.reportRepository = reportRepository;
        this.operationsWorkerRenderClient = operationsWorkerRenderClient;
        this.snapshotAssembler = snapshotAssembler;
        this.storageUploadFacade = storageUploadFacade;
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
            saveArtifact(artifactResult);
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
        record.setStorageObjectId(artifactResult == null ? null : artifactResult.getStorageObjectId());
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
                artifact.getSha256(),
                null);
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

    private void saveArtifact(OperationsReportArtifactResult artifactResult) {
        if (artifactResult == null
                || artifactResult.getContentBytes() == null
                || artifactResult.getContentBytes().length == 0) {
            throw new IllegalStateException("Operations report artifact content is empty.");
        }
        UploadStorageObjectFacadeResponse storedResult =
                storageUploadFacade.uploadStorageObject(UploadStorageObjectFacadeRequest.builder()
                        .inputStream(new ByteArrayInputStream(artifactResult.getContentBytes()))
                        .originalFilename(filenameHint(artifactResult))
                        .contentType(artifactResult.getContentType())
                        .sizeBytes(
                                artifactResult.getSizeBytes() == null
                                        ? (long) artifactResult.getContentBytes().length
                                        : artifactResult.getSizeBytes())
                        .ownerType(StorageOwnerType.USER.value())
                        .ownerId("system")
                        .build());
        if (storedResult == null || storedResult.getStorageObjectId() == null) {
            throw new IllegalStateException("Operations report storage upload returned empty storage object.");
        }
        artifactResult.setStorageObjectId(storedResult.getStorageObjectId());
    }

    private String filenameHint(OperationsReportArtifactResult artifactResult) {
        if (artifactResult != null && StringUtils.isNotBlank(artifactResult.getFilename())) {
            return artifactResult.getFilename();
        }
        String format = artifactResult == null ? null : artifactResult.getFormat();
        return "operations-report-" + System.currentTimeMillis() + "." + safeSuffix(format);
    }

    private String safeSuffix(String format) {
        return StringUtils.isBlank(format) ? "html" : format.toLowerCase();
    }
}
