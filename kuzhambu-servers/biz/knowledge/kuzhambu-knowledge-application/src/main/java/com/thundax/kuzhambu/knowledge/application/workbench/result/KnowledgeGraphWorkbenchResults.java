package com.thundax.kuzhambu.knowledge.application.workbench.result;

import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphVersionResult;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public final class KnowledgeGraphWorkbenchResults {

    private KnowledgeGraphWorkbenchResults() {}

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ManuscriptTreeNodeResult {
        private final String nodeKey;
        private final String parentKey;
        private final String nodeType;
        private final String title;
        private final String sourceContentType;
        private final Long sourceContentId;
        private final String sourcePath;
        private final String graphStatus;
        private final Long latestTaskId;
        private final Long latestGraphVersionId;
        private final List<ManuscriptTreeNodeResult> children;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ManuscriptDetailResult {
        private final String sourceContentType;
        private final Long sourceContentId;
        private final String title;
        private final String summary;
        private final String sourcePath;
        private final Integer currentVersionNo;
        private final String graphStatus;
        private final GraphExtractionTaskResult latestExtractionTask;
        private final GraphVersionResult latestGraphVersion;
        private final QualitySummaryResult qualitySummary;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CandidateSummaryResult {
        private final Long taskId;
        private final Long aiCandidateId;
        private final String taskType;
        private final String status;
        private final String sourceContentType;
        private final Long sourceContentId;
        private final String candidatePayloadJson;
        private final List<CandidateEntityResult> entities;
        private final List<CandidateRelationResult> relations;
        private final List<String> warnings;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CandidateEntityResult {
        private final String name;
        private final String entityType;
        private final String description;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CandidateRelationResult {
        private final String sourceName;
        private final String sourceType;
        private final String relationType;
        private final String targetName;
        private final String targetType;
        private final String evidence;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CandidateApplyResult {
        private final Long taskId;
        private final Long graphVersionId;
        private final String graphStatus;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class QualitySummaryResult {
        private final Double entityCoverageRate;
        private final Double relationAccuracyRate;
        private final Double completenessRate;
    }
}
