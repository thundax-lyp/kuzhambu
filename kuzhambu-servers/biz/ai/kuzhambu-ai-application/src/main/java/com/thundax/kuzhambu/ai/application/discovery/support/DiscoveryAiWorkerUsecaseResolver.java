package com.thundax.kuzhambu.ai.application.discovery.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryAiWorkerUsecaseResolver {

    private static final Map<String, DiscoveryAiWorkerUsecaseSpec> SUPPORTED_USECASES = Map.of(
            "DISCOVERY_QUERY_UNDERSTANDING",
            new DiscoveryAiWorkerUsecaseSpec(
                    "DISCOVERY_QUERY_UNDERSTANDING",
                    "/internal/ai/discovery/query-understanding",
                    "query_understanding",
                    false),
            "DISCOVERY_QUERY_REWRITE",
            new DiscoveryAiWorkerUsecaseSpec(
                    "DISCOVERY_QUERY_REWRITE", "/internal/ai/discovery/query-rewrite", "query_understanding", false),
            "DISCOVERY_ANSWER_GENERATION",
            new DiscoveryAiWorkerUsecaseSpec(
                    "DISCOVERY_ANSWER_GENERATION",
                    "/internal/ai/discovery/answer-generation",
                    "answer_generation",
                    false),
            "DISCOVERY_ANSWER_GENERATION_STREAM",
            new DiscoveryAiWorkerUsecaseSpec(
                    "DISCOVERY_ANSWER_GENERATION_STREAM",
                    "/internal/ai/discovery/answer-generation/stream",
                    "answer_generation",
                    true));

    public DiscoveryAiWorkerUsecaseSpec resolve(String usecase) {
        DiscoveryAiWorkerUsecaseSpec spec = SUPPORTED_USECASES.get(usecase);
        if (spec == null) {
            throw new BizException("unsupported discovery ai worker usecase: usecase=" + usecase);
        }
        return spec;
    }
}
