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
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeGraphWorkbenchApplicationServiceImpl implements KnowledgeGraphWorkbenchApplicationService {

    private static final String TASK_TYPE_RELATION = "RELATION";
    private static final String TASK_TYPE_GRAPH = "GRAPH";
    private static final String TASK_TYPE_LINEAGE = "LINEAGE";
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
        return treeAssembler.toTree(
                listPublicContents(),
                sourceContentType,
                parentKey,
                keyword,
                graphStatus,
                content -> latestSnapshot(content.getContentType(), parseContentId(content.getContentId())));
    }

    @Override
    @Transactional(readOnly = true)
    public ManuscriptDetailResult getManuscript(String sourceContentType, Long sourceContentId) {
        validateManuscript(sourceContentType, sourceContentId);
        ClassicsPublicContentFacadeDto manuscript = loadPublicContent(sourceContentType, sourceContentId);
        GraphExtractionTaskResult latestTask = latestTask(null, sourceContentType, sourceContentId);
        GraphVersionResult latestVersion = latestVersion(null, sourceContentType, sourceContentId);
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
        GraphExtractionTaskResult task =
                graphExtractionApplicationService.applyTaskCandidate(GraphExtractionTaskId.ofNullable(taskId));
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

    private ManuscriptGraphSnapshot latestSnapshot(String sourceContentType, Long sourceContentId) {
        GraphExtractionTaskResult task = latestTask(null, sourceContentType, sourceContentId);
        GraphVersionResult version = latestVersion(null, sourceContentType, sourceContentId);
        return new ManuscriptGraphSnapshot(
                resolveGraphStatus(task, version), parseTaskId(task), version == null ? null : version.getVersionId());
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
        if (version != null && Boolean.TRUE.equals(version.getRefinementApplied())) {
            return GRAPH_STATUS_REFINED;
        }
        if (version != null) {
            return GRAPH_STATUS_APPLIED;
        }
        if (task == null) {
            return GRAPH_STATUS_NOT_EXTRACTED;
        }
        return switch (normalize(task.getStatus())) {
            case STATUS_REQUESTED -> GRAPH_STATUS_EXTRACTING;
            case STATUS_FAILED -> GRAPH_STATUS_EXTRACTION_FAILED;
            case STATUS_SUCCEEDED -> GRAPH_STATUS_CANDIDATE_READY;
            case STATUS_APPLIED -> GRAPH_STATUS_APPLIED;
            default -> GRAPH_STATUS_NOT_EXTRACTED;
        };
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
