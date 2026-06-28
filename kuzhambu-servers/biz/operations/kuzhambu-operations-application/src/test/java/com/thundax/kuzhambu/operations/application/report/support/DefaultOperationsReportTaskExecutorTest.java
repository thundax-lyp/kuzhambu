package com.thundax.kuzhambu.operations.application.report.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportArtifactResult;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSnapshot;
import com.thundax.kuzhambu.operations.domain.report.client.OperationsWorkerRenderClient;
import com.thundax.kuzhambu.operations.domain.report.client.dto.OperationsWorkerRenderDtos;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import com.thundax.kuzhambu.storage.facade.StorageUploadFacade;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageObjectFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageObjectFacadeResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultOperationsReportTaskExecutorTest {

    @Test
    void executeShouldMarkSucceededAndStoreArtifact() {
        TrackingReportRepository repository = new TrackingReportRepository(record());
        StubRenderClient renderClient = new StubRenderClient(successResponse());
        StubSnapshotAssembler snapshotAssembler = new StubSnapshotAssembler();
        RecordingArtifactStorage storage = new RecordingArtifactStorage();
        DefaultOperationsReportTaskExecutor executor =
                new DefaultOperationsReportTaskExecutor(repository, renderClient, snapshotAssembler, storage);

        OperationsReportArtifactResult result = executor.execute(ReportId.of(9001L));

        assertNotNull(result);
        assertEquals(3001L, result.getStorageObjectId());
        assertEquals(2, repository.updatedRecords.size());
        assertEquals(ReportStatus.PROCESSING, repository.updatedRecords.get(0).getReportStatus());
        assertEquals(ReportStatus.SUCCEEDED, repository.updatedRecords.get(1).getReportStatus());
        assertEquals("weekly-report.pdf", repository.updatedRecords.get(1).getArtifactFilename());
        assertEquals(3001L, repository.updatedRecords.get(1).getStorageObjectId());
        assertNotNull(repository.updatedRecords.get(1).getCompletedAt());
        assertEquals("weekly-report.pdf", storage.lastCommand.getOriginalFilename());
        assertEquals("application/pdf", storage.lastCommand.getContentType());
        assertEquals(2L, storage.lastCommand.getSizeBytes());
        assertEquals("USER", storage.lastCommand.getOwnerType());
        assertEquals("system", storage.lastCommand.getOwnerId());
        assertEquals("ok", read(storage.lastCommand.getInputStream()));
    }

    @Test
    void executeShouldMarkFailedWhenWorkerReturnsFailure() {
        TrackingReportRepository repository = new TrackingReportRepository(record());
        StubRenderClient renderClient = new StubRenderClient(failedResponse());
        StubSnapshotAssembler snapshotAssembler = new StubSnapshotAssembler();
        RecordingArtifactStorage storage = new RecordingArtifactStorage();
        DefaultOperationsReportTaskExecutor executor =
                new DefaultOperationsReportTaskExecutor(repository, renderClient, snapshotAssembler, storage);

        OperationsReportArtifactResult result = executor.execute(ReportId.of(9001L));

        assertNull(result);
        assertEquals(2, repository.updatedRecords.size());
        assertEquals(ReportStatus.PROCESSING, repository.updatedRecords.get(0).getReportStatus());
        assertEquals(ReportStatus.FAILED, repository.updatedRecords.get(1).getReportStatus());
        assertEquals(
                "WORKER_RENDER_FAILED: worker boom",
                repository.updatedRecords.get(1).getFailureReason());
        assertNotNull(repository.updatedRecords.get(1).getCompletedAt());
        assertNull(storage.lastCommand);
    }

    private static ReportRecord record() {
        return new ReportRecord(
                ReportId.of(9001L),
                "WEEKLY",
                "PDF",
                new Date(1_718_000_000_000L),
                new Date(1_718_086_400_000L),
                "req-1",
                "trace-1",
                "2026.06.26",
                null,
                null,
                ReportStatus.PENDING,
                null,
                1001L,
                new Date(1_718_086_500_000L),
                null);
    }

    private static OperationsWorkerRenderDtos.WorkerRenderResponse successResponse() {
        OperationsWorkerRenderDtos.Artifact artifact = new OperationsWorkerRenderDtos.Artifact();
        artifact.setFormat("PDF");
        artifact.setFilename("weekly-report.pdf");
        artifact.setContentType("application/pdf");
        artifact.setEncoding("BASE64");
        artifact.setContent(Base64.getEncoder().encodeToString("ok".getBytes(StandardCharsets.UTF_8)));
        artifact.setSizeBytes(2L);
        OperationsWorkerRenderDtos.WorkerRenderResponse response =
                new OperationsWorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("SUCCEEDED");
        response.setArtifact(artifact);
        return response;
    }

    private static OperationsWorkerRenderDtos.WorkerRenderResponse failedResponse() {
        OperationsWorkerRenderDtos.WorkerRenderError error = new OperationsWorkerRenderDtos.WorkerRenderError();
        error.setCode("WORKER_RENDER_FAILED");
        error.setMessage("worker boom");
        OperationsWorkerRenderDtos.WorkerRenderResponse response =
                new OperationsWorkerRenderDtos.WorkerRenderResponse();
        response.setStatus("FAILED");
        response.setError(error);
        return response;
    }

    private static final class StubSnapshotAssembler extends OperationsReportSnapshotAssembler {
        private StubSnapshotAssembler() {
            super(null, null);
        }

        @Override
        public OperationsReportSnapshot assemble(ReportRecord record) {
            return new OperationsReportSnapshot();
        }

        @Override
        public OperationsWorkerRenderDtos.WorkerRenderRequest toWorkerRequest(
                ReportRecord record, OperationsReportSnapshot snapshot) {
            return new OperationsWorkerRenderDtos.WorkerRenderRequest();
        }
    }

    private static final class StubRenderClient implements OperationsWorkerRenderClient {
        private final OperationsWorkerRenderDtos.WorkerRenderResponse response;

        private StubRenderClient(OperationsWorkerRenderDtos.WorkerRenderResponse response) {
            this.response = response;
        }

        @Override
        public OperationsWorkerRenderDtos.WorkerRenderResponse renderOperationsReport(
                OperationsWorkerRenderDtos.WorkerRenderRequest request) {
            return response;
        }
    }

    private static final class RecordingArtifactStorage implements StorageUploadFacade {
        private UploadStorageObjectFacadeRequest lastCommand;

        @Override
        public UploadStorageObjectFacadeResponse uploadStorageObject(UploadStorageObjectFacadeRequest command) {
            this.lastCommand = command;
            return UploadStorageObjectFacadeResponse.builder()
                    .storageObjectId(3001L)
                    .originalFilename(command.getOriginalFilename())
                    .contentType(command.getContentType())
                    .sizeBytes(command.getSizeBytes())
                    .build();
        }
    }

    private static String read(InputStream inputStream) {
        try {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class TrackingReportRepository implements ReportRepository {
        private final ReportRecord record;
        private final List<ReportRecord> updatedRecords = new ArrayList<>();

        private TrackingReportRepository(ReportRecord record) {
            this.record = record;
        }

        @Override
        public ReportRecord getById(ReportId id) {
            return record;
        }

        @Override
        public com.thundax.kuzhambu.common.core.page.PageResult<ReportRecord> page(
                String reportType,
                String format,
                String reportStatus,
                Long requesterUserId,
                Date periodStart,
                Date periodEnd,
                int pageNo,
                int pageSize) {
            return com.thundax.kuzhambu.common.core.page.PageResult.of(1, 10, 0, List.of());
        }

        @Override
        public ReportId insert(ReportRecord record) {
            return record == null ? null : record.getId();
        }

        @Override
        public int update(ReportRecord record) {
            updatedRecords.add(copy(record));
            return 1;
        }

        @Override
        public int deleteById(ReportId id) {
            return 0;
        }

        private ReportRecord copy(ReportRecord record) {
            return new ReportRecord(
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
                    record.getReportStatus(),
                    record.getFailureReason(),
                    record.getRequesterUserId(),
                    record.getRequestedAt(),
                    record.getCompletedAt());
        }
    }
}
