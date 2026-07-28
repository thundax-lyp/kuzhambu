package com.thundax.kuzhambu.ai.application.scenario.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryAiWorkerUsecaseResolver {

    private static final Map<String, DiscoveryAiWorkerUsecaseSpec> SUPPORTED_USECASES = Map.of(
            "DISCOVERY_QUERY_UNDERSTANDING",
            new DiscoveryAiWorkerUsecaseSpec(
                    "DISCOVERY_QUERY_UNDERSTANDING",
                    null,
                    "discovery_query_understanding",
                    "query_understanding",
                    false),
            "DISCOVERY_QUERY_REWRITE",
            new DiscoveryAiWorkerUsecaseSpec(
                    "DISCOVERY_QUERY_REWRITE", null, "discovery_query_understanding", "query_understanding", false),
            "DISCOVERY_ANSWER_GENERATION",
            new DiscoveryAiWorkerUsecaseSpec(
                    "DISCOVERY_ANSWER_GENERATION", null, "discovery_answer_generation", "answer_generation", false),
            "DISCOVERY_ANSWER_GENERATION_STREAM",
            new DiscoveryAiWorkerUsecaseSpec(
                    "DISCOVERY_ANSWER_GENERATION_STREAM",
                    null,
                    "discovery_answer_generation",
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
