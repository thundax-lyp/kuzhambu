package com.thundax.kuzhambu.operations.infra.report.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import com.thundax.kuzhambu.operations.infra.report.persistence.dataobject.ReportDO;
import com.thundax.kuzhambu.operations.infra.report.persistence.mapper.ReportMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ReportRepositoryImplTest {

    @Test
    void getByIdShouldMapExtendedFieldsFromDataObject() {
        ReportMapper mapper = mock(ReportMapper.class);
        ReportRepositoryImpl repository = new ReportRepositoryImpl(mapper);
        when(mapper.selectOne(any())).thenReturn(dataObject(9001L));

        ReportRecord result = repository.getById(ReportId.of(9001L));

        assertNotNull(result);
        assertEquals(9001L, result.getId().value());
        assertEquals("req-1", result.getRequestId());
        assertEquals("trace-1", result.getTraceId());
        assertEquals("2026.06.26", result.getTemplateVersion());
        assertEquals("weekly-report.pdf", result.getArtifactFilename());
        assertEquals(ReportStatus.SUCCEEDED, result.getReportStatus());
    }

    @Test
    void pageShouldMapRecordsAndTotalCount() {
        ReportMapper mapper = mock(ReportMapper.class);
        ReportRepositoryImpl repository = new ReportRepositoryImpl(mapper);
        Page<ReportDO> dataObjectPage = new Page<>(2, 5);
        dataObjectPage.setTotal(6);
        dataObjectPage.setRecords(List.of(dataObject(9001L)));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);

        PageResult<ReportRecord> result = repository.page(
                "WEEKLY", "PDF", "SUCCEEDED", 1001L, new Date(1_718_000_000_000L), new Date(1_718_086_400_000L), 2, 5);

        assertEquals(2, result.getPageNo());
        assertEquals(5, result.getPageSize());
        assertEquals(6, result.getTotalCount());
        assertEquals(2, result.getTotalPage());
        assertEquals(1, result.getRecords().size());
        assertEquals(9001L, result.getRecords().get(0).getId().value());
        assertEquals("weekly-report.pdf", result.getRecords().get(0).getArtifactFilename());
    }

    @Test
    void listExpiredReportIdsShouldQueryCompletedReportsBeforeThreshold() {
        ReportMapper mapper = mock(ReportMapper.class);
        ReportRepositoryImpl repository = new ReportRepositoryImpl(mapper);
        when(mapper.selectObjs(any())).thenReturn(List.of(9001L, 9002L));

        List<ReportId> result = repository.listExpiredReportIds(new Date(1_718_086_500_000L), 2);

        assertEquals(2, result.size());
        assertEquals(9001L, result.get(0).value());
        ArgumentCaptor<QueryWrapper<ReportDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectObjs(captor.capture());
        String sqlSegment = captor.getValue().getCustomSqlSegment();
        assertTrue(sqlSegment.contains("requested_at"));
        assertTrue(sqlSegment.contains("report_status"));
        assertTrue(sqlSegment.contains("LIMIT 2"));
    }

    private static ReportDO dataObject(long reportId) {
        return new ReportDO(
                null,
                reportId,
                "WEEKLY",
                "PDF",
                new Date(1_718_000_000_000L),
                new Date(1_718_086_400_000L),
                "req-1",
                "trace-1",
                "2026.06.26",
                3001L,
                "weekly-report.pdf",
                "SUCCEEDED",
                null,
                1001L,
                new Date(1_718_086_500_000L),
                new Date(1_718_086_600_000L));
    }
}
