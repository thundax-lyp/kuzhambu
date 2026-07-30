package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReport;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportIssue;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportSourceDetail;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportIssueDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportSourceDetailDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.QualityReportIssueMapper;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.QualityReportMapper;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.QualityReportSourceDetailMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class QualityReportRepositoryTest {

    @Test
    void saveShouldInsertReportIssuesAndSourceDetails() {
        QualityReportMapper reportMapper = mock(QualityReportMapper.class);
        QualityReportIssueMapper issueMapper = mock(QualityReportIssueMapper.class);
        QualityReportSourceDetailMapper sourceDetailMapper = mock(QualityReportSourceDetailMapper.class);
        QualityReportRepositoryImpl repository =
                new QualityReportRepositoryImpl(reportMapper, issueMapper, sourceDetailMapper);
        QualityReport report = new QualityReport(
                1L,
                1001L,
                "KQR-71",
                71L,
                "SANCAI_ENTRY",
                2001L,
                "myth",
                "神话",
                "PUBLISHED",
                2L,
                1L,
                1L,
                1L,
                0L,
                0L,
                new BigDecimal("0.5000"),
                BigDecimal.ONE,
                BigDecimal.ZERO,
                new BigDecimal("0.6667"),
                1L,
                2L,
                1L,
                Instant.now(),
                Instant.now(),
                Instant.now(),
                Instant.now());
        QualityReportIssue issue = new QualityReportIssue(
                2L,
                2001L,
                1001L,
                "ANNOTATION_ISSUE",
                "medium",
                "ENTITY",
                "person:fuxi",
                "实体需复核",
                "说明",
                "建议",
                "/knowledge/refinement",
                10,
                Instant.now());
        QualityReportSourceDetail sourceDetail = new QualityReportSourceDetail(
                3L,
                3001L,
                1001L,
                "SANCAI_ENTRY",
                2001L,
                "myth",
                "神话",
                71L,
                Instant.now(),
                1L,
                2L,
                "APPLIED",
                "/knowledge/atlas",
                Instant.now());

        repository.save(report, List.of(issue), List.of(sourceDetail));

        ArgumentCaptor<QualityReportDO> reportCaptor = ArgumentCaptor.forClass(QualityReportDO.class);
        ArgumentCaptor<QualityReportIssueDO> issueCaptor = ArgumentCaptor.forClass(QualityReportIssueDO.class);
        ArgumentCaptor<QualityReportSourceDetailDO> sourceCaptor =
                ArgumentCaptor.forClass(QualityReportSourceDetailDO.class);
        verify(reportMapper).insert(reportCaptor.capture());
        verify(issueMapper).insert(issueCaptor.capture());
        verify(sourceDetailMapper).insert(sourceCaptor.capture());
        assertEquals(1001L, reportCaptor.getValue().getReportId());
        assertEquals(1001L, issueCaptor.getValue().getReportId());
        assertEquals(1001L, sourceCaptor.getValue().getReportId());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void getLatestPublishedShouldFilterPublishedAndOrderByGeneratedAt() {
        QualityReportMapper reportMapper = mock(QualityReportMapper.class);
        QualityReportRepositoryImpl repository = new QualityReportRepositoryImpl(
                reportMapper, mock(QualityReportIssueMapper.class), mock(QualityReportSourceDetailMapper.class));
        when(reportMapper.selectOne(any())).thenReturn(null);

        repository.getLatestPublished(71L);

        ArgumentCaptor<QueryWrapper<QualityReportDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(reportMapper).selectOne(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("report_status"));
        assertTrue(sqlSegment.contains("graph_version_id"));
        assertTrue(sqlSegment.contains("ORDER BY generated_at DESC,id DESC"));
    }
}
