package com.thundax.kuzhambu.ai.application.scenario.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeAiWorkerUsecaseResolver {

    private static final Map<String, KnowledgeAiWorkerUsecaseSpec> SUPPORTED_USECASES = Map.of(
            "RELATION",
            new KnowledgeAiWorkerUsecaseSpec(
                    "KNOWLEDGE_RELATION_EXTRACTION", null, "knowledge_relation_extract", "relation_extraction"),
            "GRAPH",
            new KnowledgeAiWorkerUsecaseSpec(
                    "KNOWLEDGE_GRAPH_EXTRACTION", null, "knowledge_graph_extract", "knowledge_graph"),
            "LINEAGE",
            new KnowledgeAiWorkerUsecaseSpec(
                    "KNOWLEDGE_LINEAGE_EXTRACTION", null, "knowledge_lineage_extract", "lineage_extraction"),
            "TAG",
            new KnowledgeAiWorkerUsecaseSpec("KNOWLEDGE_TAG_EXTRACTION", null, "knowledge_tags", "tags"));

    public KnowledgeAiWorkerUsecaseSpec resolve(String taskType) {
        KnowledgeAiWorkerUsecaseSpec spec = SUPPORTED_USECASES.get(taskType);
        if (spec == null) {
            throw new BizException("unsupported knowledge ai worker usecase: taskType=" + taskType);
        }
        return spec;
    }
}
