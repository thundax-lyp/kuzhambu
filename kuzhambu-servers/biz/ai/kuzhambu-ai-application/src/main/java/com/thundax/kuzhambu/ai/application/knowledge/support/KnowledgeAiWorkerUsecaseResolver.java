package com.thundax.kuzhambu.ai.application.knowledge.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeAiWorkerUsecaseResolver {

    private static final Map<String, KnowledgeAiWorkerUsecaseSpec> SUPPORTED_USECASES = Map.of(
            "RELATION",
            new KnowledgeAiWorkerUsecaseSpec(
                    "KNOWLEDGE_RELATION_EXTRACTION",
                    "/internal/ai/knowledge/relation-extraction",
                    "relation_extraction"),
            "GRAPH",
            new KnowledgeAiWorkerUsecaseSpec(
                    "KNOWLEDGE_GRAPH_EXTRACTION", "/internal/ai/knowledge/graph-extraction", "knowledge_graph"),
            "LINEAGE",
            new KnowledgeAiWorkerUsecaseSpec(
                    "KNOWLEDGE_LINEAGE_EXTRACTION", "/internal/ai/knowledge/lineage-extraction", "lineage_extraction"),
            "TAG",
            new KnowledgeAiWorkerUsecaseSpec(
                    "KNOWLEDGE_TAG_EXTRACTION", "/internal/ai/knowledge/tag-extraction", "tags"));

    public KnowledgeAiWorkerUsecaseSpec resolve(String taskType) {
        KnowledgeAiWorkerUsecaseSpec spec = SUPPORTED_USECASES.get(taskType);
        if (spec == null) {
            throw new BizException("unsupported knowledge ai worker usecase: taskType=" + taskType);
        }
        return spec;
    }
}
