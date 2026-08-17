package com.thundax.kuzhambu.knowledge.application.graph.result;

import java.util.List;

public record GraphExtractionTaskDetailResult(
        GraphExtractionTaskResult task,
        List<GraphExtractionStageResult> stages,
        List<GraphExtractionTaskResult> relatedTasks,
        GraphExtractionCandidatePreviewResult candidate) {}
