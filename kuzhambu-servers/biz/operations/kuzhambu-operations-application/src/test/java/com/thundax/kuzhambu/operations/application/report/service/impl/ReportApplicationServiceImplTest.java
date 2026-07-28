package com.thundax.kuzhambu.operations.application.report.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.report.command.OperationsReportGenerateCommand;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportDetailQuery;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportPageQuery;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDetailResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportGenerateResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportPageResult;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportArtifactResult;
import com.thundax.kuzhambu.operations.application.report.support.OperationsReportTaskExecutor;
import com.thundax.kuzhambu.operations.domain.report.codec.ReportIdCodec;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.domain.report.repository.ReportRepository;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportApplicationServiceImplTest {

    @Test
    void generateShouldInsertPendingRecordAndDispatchTask() {
        CapturingReportRepository repository = new CapturingReportRepository();
        RecordingTaskExecutor taskExecutor = new RecordingTaskExecutor();
        ReportApplicationServiceImpl service = new ReportApplicationServiceImpl(repository, taskExecutor);
        OperationsReportGenerateCommand command = new OperationsReportGenerateCommand();
        command.setReportType("WEEKLY");
        command.setFormat("PDF");
        command.setPeriodStart(new Date(1_718_000_000_000L));
        command.setPeriodEnd(new Date(1_718_086_400_000L));
        command.setRequesterUserId(1001L);

        OperationsReportGenerateResult result = service.generate(command);

        assertEquals(9001L, result.getReportId().value());
        assertEquals("PENDING", result.getReportStatus());
        assertNotNull(repository.insertedRecord);
        assertEquals("WEEKLY", repository.insertedRecord.getReportType());
        assertEquals("PDF", repository.insertedRecord.getFormat());
        assertEquals(1001L, repository.insertedRecord.getRequesterUserId());
        assertEquals(ReportStatus.PENDING, repository.insertedRecord.getReportStatus());
        assertEquals("2026.06.26", repository.insertedRecord.getTemplateVersion());
        assertTrue(repository.insertedRecord.getRequestId().startsWith("operations-report-"));
        assertTrue(repository.insertedRecord.getTraceId().startsWith("operations-trace-"));
        assertEquals(9001L, taskExecutor.asyncReportId.value());
    }

    @Test
    void pageShouldMapRepositoryRecordsToApplicationResult() {
        CapturingReportRepository repository = new CapturingReportRepository();
        RecordingTaskExecutor taskExecutor = new RecordingTaskExecutor();
        ReportApplicationServiceImpl service = new ReportApplicationServiceImpl(repository, taskExecutor);
        repository.pageResult = PageResult.of(
                1,
                20,
                1,
                List.of(new ReportRecord(
                        ReportIdCodec.toDomain(9001L),
                        "MONTHLY",
                        "HTML",
                        new Date(1_718_000_000_000L),
                        new Date(1_720_419_200_000L),
                        "req-1",
                        "trace-1",
                        "2026.06.26",
                        3001L,
                        "monthly-report.html",
                        ReportStatus.SUCCEEDED,
                        null,
                        1001L,
                        new Date(1_720_420_000_000L),
                        new Date(1_720_420_300_000L))));
        OperationsReportPageQuery query = new OperationsReportPageQuery();
        query.setReportType("MONTHLY");
        query.setFormat("HTML");
        query.setReportStatus("SUCCEEDED");
        query.setRequesterUserId(1001L);

        PageResult<OperationsReportPageResult> result = service.page(query, new PageQuery(0, 0));

        assertEquals(1, result.getPageNo());
        assertEquals(20, result.getPageSize());
        assertEquals(10, repository.lastPageSize);
        assertEquals("MONTHLY", repository.lastReportType);
        assertEquals("HTML", repository.lastFormat);
        assertEquals("SUCCEEDED", repository.lastReportStatus);
        assertEquals(1, result.getRecords().size());
        assertEquals(9001L, result.getRecords().get(0).getReportId().value());
        assertEquals("monthly-report.html", result.getRecords().get(0).getArtifactFilename());
    }

    @Test
    void detailShouldReturnNullWhenRepositoryHasNoRecord() {
        ReportApplicationServiceImpl service =
                new ReportApplicationServiceImpl(new CapturingReportRepository(), new RecordingTaskExecutor());
        OperationsReportDetailQuery query = new OperationsReportDetailQuery();
        query.setReportId(ReportIdCodec.toDomain(9001L));

        OperationsReportDetailResult result = service.detail(query);

        assertNull(result);
    }

    private static final class RecordingTaskExecutor implements OperationsReportTaskExecutor {
        private ReportId asyncReportId;

        @Override
        public void executeAsync(ReportId reportId) {
            this.asyncReportId = reportId;
        }

        @Override
        public OperationsReportArtifactResult execute(ReportId reportId) {
            return null;
        }
    }

    private static final class CapturingReportRepository implements ReportRepository {
        private ReportRecord insertedRecord;
        private PageResult<ReportRecord> pageResult = PageResult.of(1, 10, 0, List.of());
        private String lastReportType;
        private String lastFormat;
        private String lastReportStatus;
        private Long lastRequesterUserId;
        private Date lastPeriodStart;
        private Date lastPeriodEnd;
        private int lastPageNo;
        private int lastPageSize;

        @Override
        public ReportRecord getById(ReportId id) {
            return null;
        }

        @Override
        public PageResult<ReportRecord> page(
                String reportType,
                String format,
                String reportStatus,
                Long requesterUserId,
                Date periodStart,
                Date periodEnd,
                int pageNo,
                int pageSize) {
            this.lastReportType = reportType;
            this.lastFormat = format;
            this.lastReportStatus = reportStatus;
            this.lastRequesterUserId = requesterUserId;
            this.lastPeriodStart = periodStart;
            this.lastPeriodEnd = periodEnd;
            this.lastPageNo = pageNo;
            this.lastPageSize = pageSize;
            return pageResult;
        }

        @Override
        public ReportId insert(ReportRecord record) {
            this.insertedRecord = record;
            return ReportIdCodec.toDomain(9001L);
        }

        @Override
        public int update(ReportRecord record) {
            return 0;
        }

        @Override
        public int deleteById(ReportId id) {
            return 0;
        }
    }
}
