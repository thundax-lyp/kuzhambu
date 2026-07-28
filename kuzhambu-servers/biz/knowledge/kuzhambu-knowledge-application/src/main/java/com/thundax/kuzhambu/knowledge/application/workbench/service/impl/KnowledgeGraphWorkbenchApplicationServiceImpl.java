package com.thundax.kuzhambu.knowledge.application.workbench.service.impl;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.request.GetAiCandidateFacadeRequest;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestGraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestLineageExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.RequestRelationExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeQualityReportApplicationService;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateApplyResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.CandidateSummaryResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptDetailResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptTreeNodeResult;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.application.workbench.service.KnowledgeGraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.application.workbench.support.KnowledgeGraphManuscriptPayloadBuilder;
import com.thundax.kuzhambu.knowledge.application.workbench.support.KnowledgeGraphManuscriptPayloadBuilder.ManuscriptExtractionPayload;
import com.thundax.kuzhambu.knowledge.application.workbench.support.KnowledgeGraphManuscriptTreeAssembler;
import com.thundax.kuzhambu.knowledge.application.workbench.support.KnowledgeGraphManuscriptTreeAssembler.ManuscriptGraphSnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionTaskIdCodec;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeGraphWorkbenchApplicationServiceImpl implements KnowledgeGraphWorkbenchApplicationService {

    private static final String TASK_TYPE_RELATION = "RELATION";
    private static final String TASK_TYPE_GRAPH = "GRAPH";
    private static final String TASK_TYPE_LINEAGE = "LINEAGE";
    private static final String SOURCE_TYPE_SANCAI_ENTRY =
            KnowledgeGraphManuscriptTreeAssembler.SOURCE_TYPE_SANCAI_ENTRY;
    private static final String STATUS_REQUESTED = "REQUESTED";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_APPLIED = "APPLIED";
    private static final String GRAPH_STATUS_NOT_EXTRACTED = "NOT_EXTRACTED";
    private static final String GRAPH_STATUS_EXTRACTING = "EXTRACTING";
    private static final String GRAPH_STATUS_EXTRACTION_FAILED = "EXTRACTION_FAILED";
    private static final String GRAPH_STATUS_CANDIDATE_READY = "CANDIDATE_READY";
    private static final String GRAPH_STATUS_APPLIED = "APPLIED";
    private static final String GRAPH_STATUS_REFINED = "REFINED";
    private static final String TRIGGER_SOURCE_MANUAL = "MANUAL";
    private static final int SNAPSHOT_PAGE_SIZE = 100;

    private final ClassicsFacade classicsFacade;
    private final KnowledgeGraphExtractionApplicationService graphExtractionApplicationService;
    private final KnowledgeQualityReportApplicationService qualityReportApplicationService;
    private final AiFacade aiFacade;
    private final KnowledgeGraphManuscriptTreeAssembler treeAssembler;
    private final KnowledgeGraphManuscriptPayloadBuilder payloadBuilder;

    public KnowledgeGraphWorkbenchApplicationServiceImpl(
            ClassicsFacade classicsFacade,
            KnowledgeGraphExtractionApplicationService graphExtractionApplicationService,
            KnowledgeQualityReportApplicationService qualityReportApplicationService,
            AiFacade aiFacade,
            KnowledgeGraphManuscriptTreeAssembler treeAssembler,
            KnowledgeGraphManuscriptPayloadBuilder payloadBuilder) {
        this.classicsFacade = classicsFacade;
        this.graphExtractionApplicationService = graphExtractionApplicationService;
        this.qualityReportApplicationService = qualityReportApplicationService;
        this.aiFacade = aiFacade;
        this.treeAssembler = treeAssembler;
        this.payloadBuilder = payloadBuilder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManuscriptTreeNodeResult> listManuscriptTree(
            String sourceContentType, String parentKey, String keyword, String graphStatus) {
        List<ClassicsPublicContentFacadeDto> contents =
                isBlank(parentKey) && isBlank(keyword) && isBlank(graphStatus) ? List.of() : listPublicContents();
        Map<String, ManuscriptGraphSnapshot> snapshotsBySource = latestSnapshots(contents);
        return treeAssembler.toTree(
                contents,
                sourceContentType,
                parentKey,
                keyword,
                graphStatus,
                content -> snapshotsBySource.getOrDefault(
                        snapshotKey(content.getContentType(), parseContentId(content.getContentId())),
                        ManuscriptGraphSnapshot.empty()));
    }

    @Override
    @Transactional(readOnly = true)
    public ManuscriptDetailResult getManuscript(String sourceContentType, Long sourceContentId) {
        validateManuscript(sourceContentType, sourceContentId);
        ClassicsPublicContentFacadeDto manuscript = loadPublicContent(sourceContentType, sourceContentId);
        GraphExtractionTaskResult latestTask = latestTask(TASK_TYPE_GRAPH, sourceContentType, sourceContentId);
        GraphVersionResult latestVersion = latestVersion(TASK_TYPE_GRAPH, sourceContentType, sourceContentId);
        return ManuscriptDetailResult.builder()
                .sourceContentType(sourceContentType)
                .sourceContentId(sourceContentId)
                .title(manuscript.getTitle())
                .summary(manuscript.getSummary())
                .sourcePath(sourcePath(manuscript))
                .currentVersionNo(manuscript.getCurrentVersionNo())
                .graphStatus(resolveGraphStatus(latestTask, latestVersion))
                .latestExtractionTask(latestTask)
                .latestGraphVersion(latestVersion)
                .qualitySummary(latestQualitySummary(latestVersion))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GraphExtractionTaskResult extractManuscript(
            String sourceContentType, Long sourceContentId, String taskType, Long requestedBy) {
        ensureSancaiSource(sourceContentType);
        String resolvedTaskType = normalizeTaskType(taskType);
        ManuscriptExtractionPayload payload =
                payloadBuilder.build(sourceContentType, sourceContentId, resolvedTaskType);
        return switch (resolvedTaskType) {
            case TASK_TYPE_RELATION ->
                graphExtractionApplicationService.requestRelationExtraction(relationCommand(payload, requestedBy));
            case TASK_TYPE_GRAPH ->
                graphExtractionApplicationService.requestGraphExtraction(graphCommand(payload, requestedBy));
            case TASK_TYPE_LINEAGE ->
                graphExtractionApplicationService.requestLineageExtraction(lineageCommand(payload, requestedBy));
            default -> throw new BizException("Unsupported knowledge graph extraction task type: " + resolvedTaskType);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateSummaryResult getLatestCandidate(String sourceContentType, Long sourceContentId, String taskType) {
        GraphExtractionTaskResult task = latestTask(normalizeTaskType(taskType), sourceContentType, sourceContentId);
        AiCandidateFacadeDto candidate = latestCandidate(task);
        return CandidateSummaryResult.builder()
                .taskId(parseTaskId(task))
                .aiCandidateId(task == null ? null : task.getAiCandidateId())
                .taskType(task == null ? normalizeTaskType(taskType) : task.getTaskType())
                .status(task == null ? GRAPH_STATUS_NOT_EXTRACTED : task.getStatus())
                .sourceContentType(sourceContentType)
                .sourceContentId(sourceContentId)
                .candidatePayloadJson(candidate == null ? null : candidate.getResultPayload())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CandidateApplyResult applyCandidate(Long taskId) {
        if (taskId == null) {
            throw new BizException("Knowledge graph candidate taskId is required");
        }
        GraphExtractionTaskResult detail =
                graphExtractionApplicationService.getTaskDetail(GraphExtractionTaskIdCodec.toDomain(taskId));
        ensureSancaiSource(detail == null ? null : detail.getSourceContentType());
        GraphExtractionTaskResult task =
                graphExtractionApplicationService.applyTaskCandidate(GraphExtractionTaskIdCodec.toDomain(taskId));
        GraphVersionResult version =
                latestVersion(task.getTaskType(), task.getSourceContentType(), task.getSourceContentId());
        return CandidateApplyResult.builder()
                .taskId(parseTaskId(task))
                .graphVersionId(version == null ? null : version.getVersionId())
                .graphStatus(resolveGraphStatus(task, version))
                .build();
    }

    private RequestGraphExtractionCommand graphCommand(ManuscriptExtractionPayload payload, Long requestedBy) {
        return new RequestGraphExtractionCommand(
                payload.scopeType(),
                payload.scopeJson(),
                TRIGGER_SOURCE_MANUAL,
                null,
                Boolean.TRUE,
                null,
                payload.sourceContentType(),
                payload.sourceContentId(),
                requestedBy,
                null,
                null,
                payload.modelId(),
                payload.modelName(),
                null,
                payload.requestId(),
                payload.traceId(),
                payload.promptMessagesJson(),
                null,
                null,
                payload.inputPayloadJson(),
                payload.outputSchemaJson(),
                payload.forceJson(),
                payload.locale());
    }

    private RequestRelationExtractionCommand relationCommand(ManuscriptExtractionPayload payload, Long requestedBy) {
        return new RequestRelationExtractionCommand(
                payload.scopeType(),
                payload.scopeJson(),
                TRIGGER_SOURCE_MANUAL,
                null,
                Boolean.TRUE,
                null,
                payload.sourceContentType(),
                payload.sourceContentId(),
                requestedBy,
                null,
                null,
                payload.modelId(),
                payload.modelName(),
                null,
                payload.requestId(),
                payload.traceId(),
                payload.promptMessagesJson(),
                null,
                null,
                payload.inputPayloadJson(),
                payload.outputSchemaJson(),
                payload.forceJson(),
                payload.locale());
    }

    private RequestLineageExtractionCommand lineageCommand(ManuscriptExtractionPayload payload, Long requestedBy) {
        return new RequestLineageExtractionCommand(
                payload.scopeType(),
                payload.scopeJson(),
                TRIGGER_SOURCE_MANUAL,
                null,
                Boolean.TRUE,
                null,
                payload.sourceContentType(),
                payload.sourceContentId(),
                requestedBy,
                null,
                null,
                payload.modelId(),
                payload.modelName(),
                null,
                payload.requestId(),
                payload.traceId(),
                payload.promptMessagesJson(),
                null,
                null,
                payload.inputPayloadJson(),
                payload.outputSchemaJson(),
                payload.forceJson(),
                payload.locale());
    }

    private List<ClassicsPublicContentFacadeDto> listPublicContents() {
        ClassicsPublicContentsFacadeResponse response = classicsFacade.listPublicContents();
        return response == null || response.getContents() == null ? List.of() : response.getContents();
    }

    private ClassicsPublicContentFacadeDto loadPublicContent(String sourceContentType, Long sourceContentId) {
        ClassicsPublicContentFacadeResponse response =
                classicsFacade.getPublicContent(ClassicsPublicContentFacadeRequest.builder()
                        .contentType(sourceContentType)
                        .contentId(String.valueOf(sourceContentId))
                        .build());
        ClassicsPublicContentFacadeDto content = response == null ? null : response.getContent();
        if (content == null) {
            throw new BizException(
                    "Knowledge graph manuscript not found: " + sourceContentType + "#" + sourceContentId);
        }
        return content;
    }

    private Map<String, ManuscriptGraphSnapshot> latestSnapshots(List<ClassicsPublicContentFacadeDto> contents) {
        Map<String, Set<Long>> contentIdsBySource = contentIdsBySource(contents);
        Map<String, ManuscriptGraphSnapshot> snapshots = new HashMap<>();
        for (Map.Entry<String, Set<Long>> entry : contentIdsBySource.entrySet()) {
            Map<Long, GraphExtractionTaskResult> tasks = latestTasksByContentId(entry.getKey(), entry.getValue());
            Map<Long, GraphVersionResult> versions = latestVersionsByContentId(entry.getKey(), entry.getValue());
            for (Long contentId : entry.getValue()) {
                GraphExtractionTaskResult task = tasks.get(contentId);
                GraphVersionResult version = versions.get(contentId);
                snapshots.put(
                        snapshotKey(entry.getKey(), contentId),
                        new ManuscriptGraphSnapshot(
                                resolveGraphStatus(task, version),
                                parseTaskId(task),
                                version == null ? null : version.getVersionId()));
            }
        }
        return snapshots;
    }

    private Map<String, Set<Long>> contentIdsBySource(List<ClassicsPublicContentFacadeDto> contents) {
        Map<String, Set<Long>> contentIdsBySource = new HashMap<>();
        if (contents == null) {
            return contentIdsBySource;
        }
        for (ClassicsPublicContentFacadeDto content : contents) {
            if (content == null || !SOURCE_TYPE_SANCAI_ENTRY.equals(content.getContentType())) {
                continue;
            }
            Long contentId = parseContentId(content.getContentId());
            if (contentId != null) {
                contentIdsBySource
                        .computeIfAbsent(content.getContentType(), ignored -> new LinkedHashSet<>())
                        .add(contentId);
            }
        }
        return contentIdsBySource;
    }

    private Map<Long, GraphExtractionTaskResult> latestTasksByContentId(
            String sourceContentType, Set<Long> contentIds) {
        Map<Long, GraphExtractionTaskResult> tasks = new HashMap<>();
        if (isBlank(sourceContentType) || contentIds == null || contentIds.isEmpty()) {
            return tasks;
        }
        int pageNo = 1;
        while (tasks.size() < contentIds.size()) {
            PageResult<GraphExtractionTaskResult> page = graphExtractionApplicationService.pageTasks(
                    TASK_TYPE_GRAPH,
                    null,
                    null,
                    null,
                    sourceContentType,
                    null,
                    new PageQuery(pageNo, SNAPSHOT_PAGE_SIZE));
            List<GraphExtractionTaskResult> records = page == null ? List.of() : page.getRecords();
            if (records == null || records.isEmpty()) {
                break;
            }
            for (GraphExtractionTaskResult task : records) {
                if (task == null || !contentIds.contains(task.getSourceContentId())) {
                    continue;
                }
                tasks.merge(task.getSourceContentId(), task, this::newerTask);
            }
            if (page == null || pageNo >= page.getTotalPage()) {
                break;
            }
            pageNo++;
        }
        return tasks;
    }

    private Map<Long, GraphVersionResult> latestVersionsByContentId(String sourceContentType, Set<Long> contentIds) {
        Map<Long, GraphVersionResult> versions = new HashMap<>();
        if (isBlank(sourceContentType) || contentIds == null || contentIds.isEmpty()) {
            return versions;
        }
        int pageNo = 1;
        while (versions.size() < contentIds.size()) {
            PageResult<GraphVersionResult> page = graphExtractionApplicationService.pageVersions(
                    TASK_TYPE_GRAPH, null, sourceContentType, null, new PageQuery(pageNo, SNAPSHOT_PAGE_SIZE));
            List<GraphVersionResult> records = page == null ? List.of() : page.getRecords();
            if (records == null || records.isEmpty()) {
                break;
            }
            for (GraphVersionResult version : records) {
                if (version == null || !contentIds.contains(version.getSourceContentId())) {
                    continue;
                }
                versions.merge(version.getSourceContentId(), version, this::newerVersion);
            }
            if (page == null || pageNo >= page.getTotalPage()) {
                break;
            }
            pageNo++;
        }
        return versions;
    }

    private GraphExtractionTaskResult latestTask(String taskType, String sourceContentType, Long sourceContentId) {
        if (isBlank(sourceContentType) || sourceContentId == null) {
            return null;
        }
        PageResult<GraphExtractionTaskResult> page = graphExtractionApplicationService.pageTasks(
                normalize(taskType), null, null, null, sourceContentType, sourceContentId, new PageQuery(1, 1));
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    private GraphVersionResult latestVersion(String taskType, String sourceContentType, Long sourceContentId) {
        if (isBlank(sourceContentType) || sourceContentId == null) {
            return null;
        }
        PageResult<GraphVersionResult> page = graphExtractionApplicationService.pageVersions(
                normalize(taskType), null, sourceContentType, sourceContentId, new PageQuery(1, 1));
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    private QualitySummaryResult latestQualitySummary(GraphVersionResult version) {
        if (version == null || version.getVersionId() == null) {
            return null;
        }
        QualityReportDetailResult detail = qualityReportApplicationService.latest(version.getVersionId());
        QualityReportDetailResult.ReportRecord report = detail == null ? null : detail.getReport();
        if (report == null) {
            return null;
        }
        return QualitySummaryResult.builder()
                .entityCoverageRate(toDouble(report.getEntityCoverageRate()))
                .relationAccuracyRate(toDouble(report.getRelationAccuracyRate()))
                .completenessRate(toDouble(report.getCompletenessRate()))
                .build();
    }

    private AiCandidateFacadeDto latestCandidate(GraphExtractionTaskResult task) {
        if (task == null || task.getAiCandidateId() == null) {
            return null;
        }
        return aiFacade.getCandidate(GetAiCandidateFacadeRequest.builder()
                .candidateId(task.getAiCandidateId())
                .build());
    }

    private String resolveGraphStatus(GraphExtractionTaskResult task, GraphVersionResult version) {
        if (task == null) {
            if (version != null && Boolean.TRUE.equals(version.getRefinementApplied())) {
                return GRAPH_STATUS_REFINED;
            }
            if (version != null) {
                return GRAPH_STATUS_APPLIED;
            }
            return GRAPH_STATUS_NOT_EXTRACTED;
        }
        if (versionMatchesTask(task, version) && Boolean.TRUE.equals(version.getRefinementApplied())) {
            return GRAPH_STATUS_REFINED;
        }
        if (versionMatchesTask(task, version)) {
            return GRAPH_STATUS_APPLIED;
        }
        String taskStatus = normalize(task.getStatus());
        if (STATUS_APPLIED.equals(taskStatus)
                && version != null
                && Boolean.TRUE.equals(version.getRefinementApplied())) {
            return GRAPH_STATUS_REFINED;
        }
        if (STATUS_APPLIED.equals(taskStatus) && version != null) {
            return GRAPH_STATUS_APPLIED;
        }
        if (taskStatus == null) {
            return GRAPH_STATUS_NOT_EXTRACTED;
        }
        return switch (taskStatus) {
            case STATUS_REQUESTED -> GRAPH_STATUS_EXTRACTING;
            case STATUS_FAILED -> GRAPH_STATUS_EXTRACTION_FAILED;
            case STATUS_SUCCEEDED -> GRAPH_STATUS_CANDIDATE_READY;
            case STATUS_APPLIED -> GRAPH_STATUS_APPLIED;
            default -> GRAPH_STATUS_NOT_EXTRACTED;
        };
    }

    private boolean versionMatchesTask(GraphExtractionTaskResult task, GraphVersionResult version) {
        if (task == null || version == null) {
            return false;
        }
        return normalize(task.getTaskId()) != null
                && normalize(task.getTaskId()).equals(normalize(version.getTaskId()));
    }

    private GraphExtractionTaskResult newerTask(GraphExtractionTaskResult left, GraphExtractionTaskResult right) {
        Long leftActivityAt = firstPresent(left.getAppliedAt(), left.getCompletedAt(), left.getRequestedAt());
        Long rightActivityAt = firstPresent(right.getAppliedAt(), right.getCompletedAt(), right.getRequestedAt());
        if (leftActivityAt == null) {
            return right;
        }
        if (rightActivityAt == null) {
            return left;
        }
        return leftActivityAt >= rightActivityAt ? left : right;
    }

    private GraphVersionResult newerVersion(GraphVersionResult left, GraphVersionResult right) {
        Long leftAppliedAt = left.getAppliedAt();
        Long rightAppliedAt = right.getAppliedAt();
        if (leftAppliedAt == null && rightAppliedAt == null) {
            return Long.compare(
                                    left.getVersionId() == null ? 0L : left.getVersionId(),
                                    right.getVersionId() == null ? 0L : right.getVersionId())
                            >= 0
                    ? left
                    : right;
        }
        if (leftAppliedAt == null) {
            return right;
        }
        if (rightAppliedAt == null) {
            return left;
        }
        return leftAppliedAt >= rightAppliedAt ? left : right;
    }

    private Long firstPresent(Long... values) {
        for (Long value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String sourcePath(ClassicsPublicContentFacadeDto manuscript) {
        String category = normalize(manuscript.getCategoryName());
        String title = normalize(manuscript.getTitle());
        if (category == null) {
            return title;
        }
        return title == null ? category : category + " / " + title;
    }

    private Long parseTaskId(GraphExtractionTaskResult task) {
        if (task == null || isBlank(task.getTaskId())) {
            return null;
        }
        return Long.valueOf(task.getTaskId());
    }

    private Long parseContentId(String value) {
        return isBlank(value) ? null : Long.valueOf(value);
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private void validateManuscript(String sourceContentType, Long sourceContentId) {
        if (isBlank(sourceContentType) || sourceContentId == null) {
            throw new BizException("Knowledge graph manuscript is required");
        }
    }

    private void ensureSancaiSource(String sourceContentType) {
        if (!SOURCE_TYPE_SANCAI_ENTRY.equals(normalize(sourceContentType))) {
            throw new BizException("Knowledge graph workbench only supports Sancai manuscripts");
        }
    }

    private String snapshotKey(String sourceContentType, Long sourceContentId) {
        return normalize(sourceContentType) + "#" + sourceContentId;
    }

    private String normalizeTaskType(String taskType) {
        String normalizedTaskType = normalize(taskType);
        return normalizedTaskType == null ? TASK_TYPE_GRAPH : normalizedTaskType;
    }

    private String normalize(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
