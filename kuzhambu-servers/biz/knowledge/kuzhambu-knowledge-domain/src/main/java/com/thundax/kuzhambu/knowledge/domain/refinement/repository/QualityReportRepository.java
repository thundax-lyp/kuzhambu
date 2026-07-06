package com.thundax.kuzhambu.knowledge.domain.refinement.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReport;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportIssue;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportSourceDetail;
import java.util.List;

public interface QualityReportRepository {

    void save(QualityReport report, List<QualityReportIssue> issues, List<QualityReportSourceDetail> sourceDetails);

    QualityReport getByReportId(Long reportId);

    QualityReport getLatestPublished(Long graphVersionId);

    PageResult<QualityReport> page(
            Long graphVersionId,
            String sourceContentType,
            Long sourceContentId,
            String reportStatus,
            int pageNo,
            int pageSize);

    List<QualityReportIssue> listIssuesByReportId(Long reportId);

    List<QualityReportSourceDetail> listSourceDetailsByReportId(Long reportId);
}
