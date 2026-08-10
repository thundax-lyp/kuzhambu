package com.thundax.kuzhambu.knowledge.application.refinement.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ReextractLowQualityCategoryCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityAnnotationResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.IssueRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.SourceDetailRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.ReextractLowQualityCategoryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeQualityReportApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionSourceContentIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@BizExceptionBoundary
public class KnowledgeQualityReportApplicationServiceImpl implements KnowledgeQualityReportApplicationService {

    private static final BigDecimal LOW_RATE_THRESHOLD = new BigDecimal("0.8000");
    private static final String MANUAL_CONFIRMED = "MANUAL_CONFIRMED";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String TASK_TYPE_RELATION = "RELATION";
    private static final String TASK_TYPE_GRAPH = "GRAPH";
    private static final String TASK_TYPE_LINEAGE = "LINEAGE";
    private static final DateTimeFormatter REPORT_NO_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);
    private static final String TRIGGER_SOURCE_QUALITY_REPORT = "QUALITY_REPORT";
    private static final String STALE_REASON_REFINEMENT_APPLIED_AFTER_REPORT = "REFINEMENT_APPLIED_AFTER_REPORT";
    private static final String DEFAULT_LOCALE = "zh-CN";

    private final GraphVersionRepository graphVersionRepository;
    private final KnowledgeGraphExtractionApplicationService graphExtractionApplicationService;
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
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public KnowledgeQualityReportApplicationServiceImpl(
            GraphVersionRepository graphVersionRepository,
            KnowledgeGraphExtractionApplicationService graphExtractionApplicationService,
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
        this.graphExtractionApplicationService = graphExtractionApplicationService;
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
        Long graphVersionId = command == null ? null : command.graphVersionId();
        GraphVersion version = graphVersionRepository.getByVersionId(GraphVersionIdCodec.toDomain(graphVersionId));
        List<KnowledgeEntity> entities = entityRepository.listByVersionId(GraphVersionIdCodec.toDomain(graphVersionId));
        List<KnowledgeRelation> relations = relationRepository.listByVersionId(graphVersionId);
        List<KnowledgeLineageNode> lineageNodes = lineageNodeRepository.listByVersionId(graphVersionId);
        List<KnowledgeLineageRelation> lineageRelations = lineageRelationRepository.listByVersionId(graphVersionId);
        List<QualityAnnotation> annotations = qualityAnnotationRepository.listByGraphVersionId(graphVersionId);
        RefinementTask task = refinementTaskRepository.findLatestDraft(
                graphVersionTaskTypeValue(version),
                version.getSourceContentType(),
                GraphExtractionSourceContentIdCodec.toValue(version.getSourceContentId()),
                GraphVersionIdCodec.toValue(version.getId()));
        RefinementCounts refinementCounts = loadRefinementCounts(task);
        Instant now = Instant.now();
        Long reportId = idGenerator.nextId().value();
        QualityReport report = buildReport(
                reportId,
                command == null ? null : command.generatedBy(),
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
    public PageResult<ReportRecord> pageReports(QualityReportQuery query, PageQuery pageQuery) {
        QualityReportQuery effective = query == null ? new QualityReportQuery(null, null, null, null) : query;
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        PageResult<QualityReport> page = qualityReportRepository.page(
                effective.graphVersionId(),
                effective.sourceContentType(),
                effective.sourceContentId(),
                effective.reportStatus(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
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

    @Override
    public ReextractLowQualityCategoryResult reextractLowQualityCategory(ReextractLowQualityCategoryCommand command) {
        if (command == null || command.reportId() == null) {
            throw new BizException("Knowledge quality report id is required");
        }
        if (StringUtils.isBlank(command.sourceCategoryCode())) {
            throw new BizException("Knowledge quality report source category code is required");
        }
        QualityReportDetailResult detail = detail(command.reportId());
        ReportRecord report = detail.getReport();
        if (report == null) {
            throw new BizException("Knowledge quality report not found: " + command.reportId());
        }
        List<SourceDetailRecord> targets = lowQualitySourceDetails(detail, command.sourceCategoryCode());
        if (targets.isEmpty()) {
            throw new BizException("Knowledge quality report source category has no quality issues");
        }
        String sourceContentType = singleSourceContentType(targets);
        List<Long> sourceContentIds = sourceContentIds(targets);
        if (sourceContentIds.isEmpty()) {
            throw new BizException("Knowledge quality report source category has no source content ids");
        }
        Long sourceContentId = sourceContentIds.get(0);
        String taskType = StringUtils.defaultIfBlank(command.taskType(), TASK_TYPE_GRAPH);
        Boolean replaceUnconfirmedOnly =
                command.replaceUnconfirmedOnly() == null ? Boolean.TRUE : command.replaceUnconfirmedOnly();
        String sourceCategoryName = sourceCategoryName(targets);
        String selectionScopeJson = selectionScopeJson(
                command.reportId(),
                report.getGraphVersionId(),
                command.sourceCategoryCode(),
                sourceCategoryName,
                sourceContentType,
                sourceContentIds);
        GraphExtractionTaskResult task = requestReextractTask(
                taskType, selectionScopeJson, replaceUnconfirmedOnly, sourceContentType, sourceContentId, command);
        return new ReextractLowQualityCategoryResult(
                command.reportId(),
                command.sourceCategoryCode(),
                sourceCategoryName,
                sourceContentType,
                sourceContentId,
                parseTaskId(task == null ? null : task.getTaskId()),
                task == null ? null : task.getBatchJobId(),
                taskType,
                TRIGGER_SOURCE_QUALITY_REPORT,
                selectionScopeJson,
                replaceUnconfirmedOnly);
    }

    private QualityReport buildReport(
            Long reportId,
            Long generatedBy,
            Instant now,
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
                reportNo(now, GraphVersionIdCodec.toValue(version.getId())),
                GraphVersionIdCodec.toValue(version.getId()),
                version.getSourceContentType(),
                GraphExtractionSourceContentIdCodec.toValue(version.getSourceContentId()),
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
            Long reportId, QualityReport report, List<QualityAnnotation> annotations, Instant now) {
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
                    "进入知识图谱工作台处理该人工标注。",
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
            Instant now) {
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
            Instant now) {
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
            Instant now) {
        return new QualityReportSourceDetail(
                null,
                null,
                reportId,
                version.getSourceContentType(),
                GraphExtractionSourceContentIdCodec.toValue(version.getSourceContentId()),
                version.getSourceCategoryCode(),
                version.getSourceCategoryName(),
                GraphVersionIdCodec.toValue(version.getId()),
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
        ReportStaleInfo staleInfo = resolveReportStaleInfo(report);
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
                        : annotations.stream().map(this::toAnnotationResult).toList(),
                staleInfo.stale(),
                staleInfo.staleReason(),
                staleInfo.lastRefinementAppliedAt());
    }

    private QualityReportDetailResult emptyDetail() {
        return new QualityReportDetailResult(null, List.of(), List.of(), List.of(), Boolean.FALSE, null, null);
    }

    private ReportStaleInfo resolveReportStaleInfo(QualityReport report) {
        if (report == null || report.getGraphVersionId() == null) {
            return new ReportStaleInfo(Boolean.FALSE, null, null);
        }
        RefinementTask lastAppliedRefinement =
                refinementTaskRepository.findLatestAppliedByGraphVersionId(report.getGraphVersionId());
        if (lastAppliedRefinement == null || lastAppliedRefinement.getAppliedAt() == null) {
            return new ReportStaleInfo(Boolean.FALSE, null, null);
        }
        Long lastRefinementAppliedAt = lastAppliedRefinement.getAppliedAt().toEpochMilli();
        boolean stale = report.getGeneratedAt() != null
                && report.getGeneratedAt().isBefore(lastAppliedRefinement.getAppliedAt());
        return new ReportStaleInfo(
                stale, stale ? STALE_REASON_REFINEMENT_APPLIED_AFTER_REPORT : null, lastRefinementAppliedAt);
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
                        .filter(item ->
                                KnowledgeConfirmationStatus.MANUAL_CONFIRMED.equals(item.getConfirmationStatus()))
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

    private String reportNo(Instant now, Long graphVersionId) {
        return "KQR-" + REPORT_NO_TIME_FORMATTER.format(now) + "-" + graphVersionId;
    }

    private String graphVersionTaskTypeValue(GraphVersion version) {
        return version == null || version.getTaskType() == null
                ? null
                : version.getTaskType().value();
    }

    private String annotationTitle(QualityAnnotation annotation) {
        return "人工标注问题：" + annotation.getAnnotationLabel();
    }

    private int size(List<?> values) {
        return values == null ? 0 : values.size();
    }

    private List<SourceDetailRecord> lowQualitySourceDetails(
            QualityReportDetailResult detail, String sourceCategoryCode) {
        List<SourceDetailRecord> sourceDetails = detail == null ? List.of() : detail.getSourceDetails();
        return sourceDetails == null
                ? List.of()
                : sourceDetails.stream()
                        .filter(item -> sourceCategoryCode.equals(item.getSourceCategoryCode()))
                        .filter(item -> item.getIssueCount() != null && item.getIssueCount() > 0)
                        .toList();
    }

    private String singleSourceContentType(List<SourceDetailRecord> targets) {
        List<String> sourceContentTypes = targets.stream()
                .map(SourceDetailRecord::getSourceContentType)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        if (sourceContentTypes.size() != 1) {
            throw new BizException("低质量门类包含多个来源类型，请按来源类型拆分重提取");
        }
        return sourceContentTypes.get(0);
    }

    private List<Long> sourceContentIds(List<SourceDetailRecord> targets) {
        return targets.stream()
                .map(SourceDetailRecord::getSourceContentId)
                .filter(item -> item != null)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private String sourceCategoryName(List<SourceDetailRecord> targets) {
        return targets.stream()
                .map(SourceDetailRecord::getSourceCategoryName)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    private String selectionScopeJson(
            Long reportId,
            Long graphVersionId,
            String sourceCategoryCode,
            String sourceCategoryName,
            String sourceContentType,
            List<Long> sourceContentIds) {
        Map<String, Object> scope = new LinkedHashMap<>();
        scope.put("triggerSource", TRIGGER_SOURCE_QUALITY_REPORT);
        scope.put("qualityReportId", reportId);
        scope.put("graphVersionId", graphVersionId);
        scope.put("sourceCategoryCode", sourceCategoryCode);
        scope.put("sourceCategoryName", sourceCategoryName);
        scope.put("sourceContentType", sourceContentType);
        scope.put("sourceContentIds", sourceContentIds);
        try {
            return objectMapper.writeValueAsString(scope);
        } catch (JsonProcessingException ex) {
            throw new BizException("Knowledge quality report reextract scope is invalid");
        }
    }

    private GraphExtractionTaskResult requestReextractTask(
            String taskType,
            String selectionScopeJson,
            Boolean replaceUnconfirmedOnly,
            String sourceContentType,
            Long sourceContentId,
            ReextractLowQualityCategoryCommand command) {
        String requestId = "quality-reextract-" + UUID.randomUUID();
        String traceId = "quality-reextract-trace-" + UUID.randomUUID();
        return switch (taskType) {
            case TASK_TYPE_RELATION ->
                graphExtractionApplicationService.requestRelationExtraction(new RequestRelationExtractionCommand(
                        sourceContentType,
                        null,
                        TRIGGER_SOURCE_QUALITY_REPORT,
                        selectionScopeJson,
                        replaceUnconfirmedOnly,
                        null,
                        sourceContentType,
                        sourceContentId,
                        command.requestedBy(),
                        null,
                        null,
                        command.modelId(),
                        command.modelName(),
                        null,
                        requestId,
                        traceId,
                        command.promptMessagesJson(),
                        null,
                        null,
                        command.inputPayloadJson(),
                        null,
                        false,
                        DEFAULT_LOCALE));
            case TASK_TYPE_GRAPH ->
                graphExtractionApplicationService.requestGraphExtraction(new RequestGraphExtractionCommand(
                        sourceContentType,
                        null,
                        TRIGGER_SOURCE_QUALITY_REPORT,
                        selectionScopeJson,
                        replaceUnconfirmedOnly,
                        null,
                        sourceContentType,
                        sourceContentId,
                        command.requestedBy(),
                        null,
                        null,
                        command.modelId(),
                        command.modelName(),
                        null,
                        requestId,
                        traceId,
                        command.promptMessagesJson(),
                        null,
                        null,
                        command.inputPayloadJson(),
                        null,
                        false,
                        DEFAULT_LOCALE));
            case TASK_TYPE_LINEAGE ->
                graphExtractionApplicationService.requestLineageExtraction(new RequestLineageExtractionCommand(
                        sourceContentType,
                        null,
                        TRIGGER_SOURCE_QUALITY_REPORT,
                        selectionScopeJson,
                        replaceUnconfirmedOnly,
                        null,
                        sourceContentType,
                        sourceContentId,
                        command.requestedBy(),
                        null,
                        null,
                        command.modelId(),
                        command.modelName(),
                        null,
                        requestId,
                        traceId,
                        command.promptMessagesJson(),
                        null,
                        null,
                        command.inputPayloadJson(),
                        null,
                        false,
                        DEFAULT_LOCALE));
            default -> throw new BizException("Unsupported knowledge quality reextract task type: " + taskType);
        };
    }

    private Long parseTaskId(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return null;
        }
        try {
            return Long.valueOf(taskId);
        } catch (NumberFormatException ex) {
            throw new BizException("Knowledge graph extraction task id is invalid: " + taskId);
        }
    }

    private record RefinementCounts(long entityConfirmed, long relationConfirmed, long lineageConfirmed) {}

    private record ReportStaleInfo(Boolean stale, String staleReason, Long lastRefinementAppliedAt) {}
}
