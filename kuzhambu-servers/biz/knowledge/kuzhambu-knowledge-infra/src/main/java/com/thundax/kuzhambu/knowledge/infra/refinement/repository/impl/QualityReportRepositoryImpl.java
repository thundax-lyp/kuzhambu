package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReport;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportIssue;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportSourceDetail;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityReportRepository;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.QualityReportIssuePersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.QualityReportPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.assembler.QualityReportSourceDetailPersistenceAssembler;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportIssueDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.QualityReportSourceDetailDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.QualityReportIssueMapper;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.QualityReportMapper;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.QualityReportSourceDetailMapper;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class QualityReportRepositoryImpl implements QualityReportRepository {

    private final QualityReportMapper reportMapper;
    private final QualityReportIssueMapper issueMapper;
    private final QualityReportSourceDetailMapper sourceDetailMapper;

    public QualityReportRepositoryImpl(
            QualityReportMapper reportMapper,
            QualityReportIssueMapper issueMapper,
            QualityReportSourceDetailMapper sourceDetailMapper) {
        this.reportMapper = reportMapper;
        this.issueMapper = issueMapper;
        this.sourceDetailMapper = sourceDetailMapper;
    }

    @Override
    public void save(
            QualityReport report, List<QualityReportIssue> issues, List<QualityReportSourceDetail> sourceDetails) {
        QualityReportDO reportDataObject = QualityReportPersistenceAssembler.toObject(report);
        ensureReportIds(reportDataObject);
        reportMapper.insert(reportDataObject);
        insertIssues(reportDataObject.getReportId(), issues);
        insertSourceDetails(reportDataObject.getReportId(), sourceDetails);
    }

    @Override
    public QualityReport getByReportId(Long reportId) {
        QueryWrapper<QualityReportDO> wrapper = new QueryWrapper<>();
        wrapper.eq("report_id", reportId);
        return QualityReportPersistenceAssembler.toDomain(reportMapper.selectOne(wrapper));
    }

    @Override
    public QualityReport getByLatestPublished(Long graphVersionId) {
        QueryWrapper<QualityReportDO> wrapper = new QueryWrapper<>();
        wrapper.eq("report_status", "PUBLISHED")
                .eq(graphVersionId != null, "graph_version_id", graphVersionId)
                .orderByDesc("generated_at")
                .orderByDesc("id")
                .last("limit 1");
        return QualityReportPersistenceAssembler.toDomain(reportMapper.selectOne(wrapper));
    }

    @Override
    public PageResult<QualityReport> page(
            Long graphVersionId,
            String sourceContentType,
            Long sourceContentId,
            String reportStatus,
            int pageNo,
            int pageSize) {
        QueryWrapper<QualityReportDO> wrapper = new QueryWrapper<>();
        wrapper.eq(graphVersionId != null, "graph_version_id", graphVersionId)
                .eq(StringUtils.isNotBlank(sourceContentType), "source_content_type", sourceContentType)
                .eq(sourceContentId != null, "source_content_id", sourceContentId)
                .eq(StringUtils.isNotBlank(reportStatus), "report_status", reportStatus)
                .orderByDesc("generated_at")
                .orderByDesc("id");
        IPage<QualityReportDO> pageResult = reportMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(
                (int) pageResult.getCurrent(),
                (int) pageResult.getSize(),
                pageResult.getTotal(),
                QualityReportPersistenceAssembler.toDomainList(pageResult.getRecords()));
    }

    @Override
    public List<QualityReportIssue> listIssuesByReportId(Long reportId) {
        QueryWrapper<QualityReportIssueDO> wrapper = new QueryWrapper<>();
        wrapper.eq("report_id", reportId).orderByAsc("priority").orderByAsc("id");
        return QualityReportIssuePersistenceAssembler.toDomainList(issueMapper.selectList(wrapper));
    }

    @Override
    public List<QualityReportSourceDetail> listSourceDetailsByReportId(Long reportId) {
        QueryWrapper<QualityReportSourceDetailDO> wrapper = new QueryWrapper<>();
        wrapper.eq("report_id", reportId).orderByDesc("applied_at").orderByDesc("id");
        return QualityReportSourceDetailPersistenceAssembler.toDomainList(sourceDetailMapper.selectList(wrapper));
    }

    private void ensureReportIds(QualityReportDO reportDataObject) {
        if (reportDataObject.getReportId() == null) {
            reportDataObject.setReportId(reportDataObject.getId());
        }
    }

    private void insertIssues(Long reportId, List<QualityReportIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return;
        }
        for (QualityReportIssue issue : issues) {
            QualityReportIssueDO dataObject = QualityReportIssuePersistenceAssembler.toObject(issue);
            if (dataObject.getIssueId() == null) {
                dataObject.setIssueId(dataObject.getId());
            }
            dataObject.setReportId(reportId);
            issueMapper.insert(dataObject);
        }
    }

    private void insertSourceDetails(Long reportId, List<QualityReportSourceDetail> sourceDetails) {
        if (sourceDetails == null || sourceDetails.isEmpty()) {
            return;
        }
        for (QualityReportSourceDetail sourceDetail : sourceDetails) {
            QualityReportSourceDetailDO dataObject =
                    QualityReportSourceDetailPersistenceAssembler.toObject(sourceDetail);
            if (dataObject.getDetailId() == null) {
                dataObject.setDetailId(dataObject.getId());
            }
            dataObject.setReportId(reportId);
            sourceDetailMapper.insert(dataObject);
        }
    }
}
