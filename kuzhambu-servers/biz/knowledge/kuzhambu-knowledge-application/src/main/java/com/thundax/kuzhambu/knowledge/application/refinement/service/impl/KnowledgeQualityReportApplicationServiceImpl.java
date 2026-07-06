package com.thundax.kuzhambu.knowledge.application.refinement.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.page.PageRules;
import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportPageQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.IssueRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.SourceDetailRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeQualityReportApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityAnnotation;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReport;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportIssue;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.QualityReportSourceDetail;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementTask;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityAnnotationRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.QualityReportRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementEntityDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageNodeDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementLineageRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementRelationDraftRepository;
import com.thundax.kuzhambu.knowledge.domain.refinement.repository.RefinementTaskRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@BizExceptionBoundary
public class KnowledgeQualityReportApplicationServiceImpl implements KnowledgeQualityReportApplicationService {

    private static final BigDecimal LOW_RATE_THRESHOLD = new BigDecimal("0.8000");
    private static final String MANUAL_CONFIRMED = "MANUAL_CONFIRMED";
    private static final String PUBLISHED = "PUBLISHED";

    private final GraphVersionRepository graphVersionRepository;
    private final KnowledgeEntityRepository entityRepository;
    private final KnowledgeRelationRepository relationRepository;
    private final KnowledgeLineageNodeRepository lineageNodeRepository;
    private final KnowledgeLineageRelationRepository lineageRelationRepository;
    private final RefinementTaskRepository refinementTaskRepository;
    private final RefinementEntityDraftRepository entityDraftRepository;
    private final RefinementRelationDraftRepository relationDraftRepository;
    private final RefinementLineageNodeDraftRepository lineageNodeDraftRepository;
    private final RefinementLineageRelationDraftRepository lineageRelationDraftRepository;
    private final QualityAnnotationRepository qualityAnnotationRepository;
    private final QualityReportRepository qualityReportRepository;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public KnowledgeQualityReportApplicationServiceImpl(
            GraphVersionRepository graphVersionRepository,
            KnowledgeEntityRepository entityRepository,
            KnowledgeRelationRepository relationRepository,
            KnowledgeLineageNodeRepository lineageNodeRepository,
            KnowledgeLineageRelationRepository lineageRelationRepository,
            RefinementTaskRepository refinementTaskRepository,
            RefinementEntityDraftRepository entityDraftRepository,
            RefinementRelationDraftRepository relationDraftRepository,
            RefinementLineageNodeDraftRepository lineageNodeDraftRepository,
            RefinementLineageRelationDraftRepository lineageRelationDraftRepository,
            QualityAnnotationRepository qualityAnnotationRepository,
            QualityReportRepository qualityReportRepository) {
        this.graphVersionRepository = graphVersionRepository;
        this.entityRepository = entityRepository;
        this.relationRepository = relationRepository;
        this.lineageNodeRepository = lineageNodeRepository;
        this.lineageRelationRepository = lineageRelationRepository;
        this.refinementTaskRepository = refinementTaskRepository;
        this.entityDraftRepository = entityDraftRepository;
        this.relationDraftRepository = relationDraftRepository;
        this.lineageNodeDraftRepository = lineageNodeDraftRepository;
        this.lineageRelationDraftRepository = lineageRelationDraftRepository;
        this.qualityAnnotationRepository = qualityAnnotationRepository;
        this.qualityReportRepository = qualityReportRepository;
    }

    @Override
    public QualityReportDetailResult generateReport(GenerateQualityReportCommand command) {
        Long graphVersionId = command == null ? null : command.getGraphVersionId();
        GraphVersion version = graphVersionRepository.getByVersionId(graphVersionId);
        List<KnowledgeEntity> entities = entityRepository.listByVersionId(graphVersionId);
        List<KnowledgeRelation> relations = relationRepository.listByVersionId(graphVersionId);
        List<KnowledgeLineageNode> lineageNodes = lineageNodeRepository.listByVersionId(graphVersionId);
        List<KnowledgeLineageRelation> lineageRelations = lineageRelationRepository.listByVersionId(graphVersionId);
        List<QualityAnnotation> annotations = qualityAnnotationRepository.listByGraphVersionId(graphVersionId);
        RefinementTask task = refinementTaskRepository.findLatestDraft(
                version.getTaskType(),
                version.getSourceContentType(),
                version.getSourceContentId(),
                version.getVersionId());
        RefinementCounts refinementCounts = loadRefinementCounts(task);
        Date now = new Date();
        Long reportId = idGenerator.nextId().value();
        QualityReport report = buildReport(
                reportId,
                command == null ? null : command.getGeneratedBy(),
                now,
                version,
                entities,
                relations,
                lineageNodes,
                lineageRelations,
                refinementCounts,
                annotations);
        List<QualityReportIssue> issues = buildIssues(reportId, report, annotations, now);
        report.setIssueCount((long) issues.size());
        List<QualityReportSourceDetail> sourceDetails =
                List.of(buildSourceDetail(reportId, version, annotations, issues, now));
        qualityReportRepository.save(report, issues, sourceDetails);
        return toDetail(report, issues, sourceDetails, annotations);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ReportRecord> pageReports(QualityReportPageQuery query) {
        QualityReportPageQuery effective = query == null
                ? new QualityReportPageQuery(
                        null, null, null, null, PageRules.firstPageIndex(), PageRules.defaultPageSize())
                : query;
        PageResult<QualityReport> page = qualityReportRepository.page(
                effective.getGraphVersionId(),
                effective.getSourceContentType(),
                effective.getSourceContentId(),
                effective.getReportStatus(),
                effective.getPageNo(),
                effective.getPageSize());
        return PageResult.of(
                page.getPageNo(),
                page.getPageSize(),
                page.getTotalCount(),
                page.getRecords().stream().map(this::toReportRecord).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QualityReportDetailResult detail(Long reportId) {
        QualityReport report = qualityReportRepository.getByReportId(reportId);
        if (report == null) {
            return emptyDetail();
        }
        return toDetail(
                report,
                qualityReportRepository.listIssuesByReportId(reportId),
                qualityReportRepository.listSourceDetailsByReportId(reportId),
                qualityAnnotationRepository.listByGraphVersionId(report.getGraphVersionId()));
    }

    @Override
    @Transactional(readOnly = true)
    public QualityReportDetailResult latest(Long graphVersionId) {
        QualityReport report = qualityReportRepository.getLatestPublished(graphVersionId);
        return report == null ? emptyDetail() : detail(report.getReportId());
    }

    private QualityReport buildReport(
            Long reportId,
            Long generatedBy,
            Date now,
            GraphVersion version,
            List<KnowledgeEntity> entities,
            List<KnowledgeRelation> relations,
            List<KnowledgeLineageNode> lineageNodes,
            List<KnowledgeLineageRelation> lineageRelations,
            RefinementCounts refinementCounts,
            List<QualityAnnotation> annotations) {
        long entityTotal = size(entities);
        long relationTotal = size(relations);
        long lineageTotal = size(lineageNodes) + size(lineageRelations);
        long entityConfirmed = effectiveConfirmed(refinementCounts.entityConfirmed, confirmedEntities(entities));
        long relationConfirmed = effectiveConfirmed(refinementCounts.relationConfirmed, confirmedRelations(relations));
        long lineageConfirmed = effectiveConfirmed(
                refinementCounts.lineageConfirmed,
                confirmedLineageNodes(lineageNodes) + confirmedLineageRelations(lineageRelations));
        BigDecimal entityCoverageRate = ratio(entityConfirmed, entityTotal);
        BigDecimal relationAccuracyRate = ratio(relationConfirmed, relationTotal);
        BigDecimal lineageCoverageRate = ratio(lineageConfirmed, lineageTotal);
        BigDecimal completenessRate = ratio(
                entityConfirmed + relationConfirmed + lineageConfirmed, entityTotal + relationTotal + lineageTotal);
        return new QualityReport(
                null,
                reportId,
                reportNo(now, version.getVersionId()),
                version.getVersionId(),
                version.getSourceContentType(),
                version.getSourceContentId(),
                version.getSourceCategoryCode(),
                version.getSourceCategoryName(),
                PUBLISHED,
                entityTotal,
                entityConfirmed,
                relationTotal,
                relationConfirmed,
                lineageTotal,
                lineageConfirmed,
                entityCoverageRate,
                relationAccuracyRate,
                lineageCoverageRate,
                completenessRate,
                (long) size(annotations),
                0L,
                generatedBy,
                now,
                now,
                now,
                now);
    }

    private List<QualityReportIssue> buildIssues(
            Long reportId, QualityReport report, List<QualityAnnotation> annotations, Date now) {
        List<QualityReportIssue> issues = new ArrayList<>();
        int priority = 10;
        if (report.getEntityTotalCount() + report.getRelationTotalCount() + report.getLineageTotalCount() == 0) {
            issues.add(issue(
                    reportId,
                    "EMPTY_GOVERNABLE_OBJECT",
                    "high",
                    null,
                    null,
                    "无可治理知识对象",
                    "当前图谱版本没有正式实体、关系或世系对象。",
                    "先完成图谱抽取或精修应用后再生成质量报告。",
                    "/knowledge/refinement",
                    priority,
                    now));
            priority += 10;
        }
        priority = addRateIssue(
                issues, reportId, "LOW_ENTITY_COVERAGE", "实体覆盖率偏低", report.getEntityCoverageRate(), priority, now);
        priority = addRateIssue(
                issues, reportId, "LOW_RELATION_ACCURACY", "关系准确率偏低", report.getRelationAccuracyRate(), priority, now);
        priority = addRateIssue(
                issues, reportId, "LOW_LINEAGE_COVERAGE", "世系覆盖率偏低", report.getLineageCoverageRate(), priority, now);
        for (QualityAnnotation annotation : annotations == null ? List.<QualityAnnotation>of() : annotations) {
            if (!"ISSUE".equals(annotation.getAnnotationStatus())) {
                continue;
            }
            issues.add(issue(
                    reportId,
                    "ANNOTATION_ISSUE",
                    "medium",
                    annotation.getObjectType(),
                    annotation.getObjectKey(),
                    annotationTitle(annotation),
                    annotation.getComment(),
                    "进入知识图谱精修工作台处理该人工标注。",
                    "/knowledge/refinement",
                    priority,
                    now));
            priority += 10;
        }
        return issues;
    }

    private int addRateIssue(
            List<QualityReportIssue> issues,
            Long reportId,
            String issueType,
            String title,
            BigDecimal rate,
            int priority,
            Date now) {
        if (rate.compareTo(LOW_RATE_THRESHOLD) >= 0) {
            return priority;
        }
        issues.add(issue(
                reportId,
                issueType,
                "medium",
                null,
                null,
                title,
                "当前指标为 " + rate + "，低于质量报告阈值 " + LOW_RATE_THRESHOLD + "。",
                "优先确认待精修对象，并补齐人工质量标注。",
                "/knowledge/refinement",
                priority,
                now));
        return priority + 10;
    }

    private QualityReportIssue issue(
            Long reportId,
            String issueType,
            String severity,
            String objectType,
            String objectKey,
            String title,
            String description,
            String suggestion,
            String href,
            int priority,
            Date now) {
        return new QualityReportIssue(
                null,
                null,
                reportId,
                issueType,
                severity,
                objectType,
                objectKey,
                title,
                description,
                suggestion,
                href,
                priority,
                now);
    }

    private QualityReportSourceDetail buildSourceDetail(
            Long reportId,
            GraphVersion version,
            List<QualityAnnotation> annotations,
            List<QualityReportIssue> issues,
            Date now) {
        return new QualityReportSourceDetail(
                null,
                null,
                reportId,
                version.getSourceContentType(),
                version.getSourceContentId(),
                version.getSourceCategoryCode(),
                version.getSourceCategoryName(),
                version.getVersionId(),
                version.getAppliedAt(),
                (long) size(annotations),
                (long) size(issues),
                "APPLIED",
                "/knowledge/atlas",
                now);
    }

    private RefinementCounts loadRefinementCounts(RefinementTask task) {
        if (task == null || task.getRefinementTaskId() == null) {
            return new RefinementCounts(0L, 0L, 0L);
        }
        Long taskId = task.getRefinementTaskId().value();
        long entityConfirmed = entityDraftRepository.listByTaskId(taskId).stream()
                .filter(item -> MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
                .count();
        long relationConfirmed = relationDraftRepository.listByTaskId(taskId).stream()
                .filter(item -> MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
                .count();
        long lineageConfirmed = lineageNodeDraftRepository.listByTaskId(taskId).stream()
                        .filter(item -> MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
                        .count()
                + lineageRelationDraftRepository.listByTaskId(taskId).stream()
                        .filter(item -> MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
                        .count();
        return new RefinementCounts(entityConfirmed, relationConfirmed, lineageConfirmed);
    }

    private QualityReportDetailResult toDetail(
            QualityReport report,
            List<QualityReportIssue> issues,
            List<QualityReportSourceDetail> sourceDetails,
            List<QualityAnnotation> annotations) {
        return new QualityReportDetailResult(
                toReportRecord(report),
                issues == null
                        ? List.of()
                        : issues.stream().map(this::toIssueRecord).toList(),
                sourceDetails == null
                        ? List.of()
                        : sourceDetails.stream().map(this::toSourceDetailRecord).toList(),
                annotations == null
                        ? List.of()
                        : annotations.stream().map(this::toAnnotationResult).toList());
    }

    private QualityReportDetailResult emptyDetail() {
        return new QualityReportDetailResult(null, List.of(), List.of(), List.of());
    }

    private ReportRecord toReportRecord(QualityReport report) {
        return report == null
                ? null
                : new ReportRecord(
                        report.getReportId(),
                        report.getReportNo(),
                        report.getGraphVersionId(),
                        report.getSourceContentType(),
                        report.getSourceContentId(),
                        report.getSourceCategoryCode(),
                        report.getSourceCategoryName(),
                        report.getReportStatus(),
                        report.getEntityTotalCount(),
                        report.getEntityConfirmedCount(),
                        report.getRelationTotalCount(),
                        report.getRelationConfirmedCount(),
                        report.getLineageTotalCount(),
                        report.getLineageConfirmedCount(),
                        report.getEntityCoverageRate(),
                        report.getRelationAccuracyRate(),
                        report.getLineageCoverageRate(),
                        report.getCompletenessRate(),
                        report.getAnnotationCount(),
                        report.getIssueCount(),
                        report.getGeneratedBy(),
                        report.getGeneratedAt(),
                        report.getPublishedAt());
    }

    private IssueRecord toIssueRecord(QualityReportIssue issue) {
        return new IssueRecord(
                issue.getIssueId(),
                issue.getIssueType(),
                issue.getSeverity(),
                issue.getObjectType(),
                issue.getObjectKey(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getSuggestion(),
                issue.getHref(),
                issue.getPriority());
    }

    private SourceDetailRecord toSourceDetailRecord(QualityReportSourceDetail sourceDetail) {
        return new SourceDetailRecord(
                sourceDetail.getDetailId(),
                sourceDetail.getSourceContentType(),
                sourceDetail.getSourceContentId(),
                sourceDetail.getSourceCategoryCode(),
                sourceDetail.getSourceCategoryName(),
                sourceDetail.getGraphVersionId(),
                sourceDetail.getAppliedAt(),
                sourceDetail.getAnnotationCount(),
                sourceDetail.getIssueCount(),
                sourceDetail.getStatus(),
                sourceDetail.getHref());
    }

    private QualityAnnotationResult toAnnotationResult(QualityAnnotation annotation) {
        return new QualityAnnotationResult(
                annotation.getAnnotationId(),
                annotation.getObjectType(),
                annotation.getObjectKey(),
                annotation.getGraphVersionId(),
                annotation.getAnnotationStatus(),
                annotation.getAnnotationLabel(),
                annotation.getComment());
    }

    private long confirmedEntities(List<KnowledgeEntity> entities) {
        return entities == null
                ? 0L
                : entities.stream()
                        .filter(item -> MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
                        .count();
    }

    private long confirmedRelations(List<KnowledgeRelation> relations) {
        return relations == null
                ? 0L
                : relations.stream()
                        .filter(item -> MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
                        .count();
    }

    private long confirmedLineageNodes(List<KnowledgeLineageNode> lineageNodes) {
        return lineageNodes == null
                ? 0L
                : lineageNodes.stream()
                        .filter(item -> MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
                        .count();
    }

    private long confirmedLineageRelations(List<KnowledgeLineageRelation> lineageRelations) {
        return lineageRelations == null
                ? 0L
                : lineageRelations.stream()
                        .filter(item -> MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
                        .count();
    }

    private long effectiveConfirmed(long refinementConfirmed, long formalConfirmed) {
        return refinementConfirmed > 0 ? refinementConfirmed : formalConfirmed;
    }

    private BigDecimal ratio(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private String reportNo(Date now, Long graphVersionId) {
        return "KQR-" + new SimpleDateFormat("yyyyMMddHHmmss").format(now) + "-" + graphVersionId;
    }

    private String annotationTitle(QualityAnnotation annotation) {
        return "人工标注问题：" + annotation.getAnnotationLabel();
    }

    private int size(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private record RefinementCounts(long entityConfirmed, long relationConfirmed, long lineageConfirmed) {}
}
