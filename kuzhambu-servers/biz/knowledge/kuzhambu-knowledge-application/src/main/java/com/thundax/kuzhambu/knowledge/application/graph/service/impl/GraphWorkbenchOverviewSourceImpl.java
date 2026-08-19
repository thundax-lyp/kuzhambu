package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchOverviewSource;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphCoreRelationPolicy;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchMetrics;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewFingerprint;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GraphWorkbenchOverviewSourceImpl implements GraphWorkbenchOverviewSource {

    private final GraphWorkbenchRepository workbenchRepository;
    private final GraphSchemaResolver schemaResolver;
    private Clock clock = Clock.systemUTC();

    public GraphWorkbenchOverviewSourceImpl(
            GraphWorkbenchRepository workbenchRepository, GraphSchemaResolver schemaResolver) {
        this.workbenchRepository = workbenchRepository;
        this.schemaResolver = schemaResolver;
    }

    @Override
    public GraphWorkbenchOverviewSnapshot load() {
        List<GraphCoreRelationPolicy> policies = schemaResolver.coreRelationPolicies();
        GraphWorkbenchOverviewFingerprint fingerprint =
                workbenchRepository.getByOverviewFingerprint(schemaFingerprint(policies));
        GraphWorkbenchMetrics metrics = workbenchRepository.getByOverview(policies);
        return new GraphWorkbenchOverviewSnapshot(
                Instant.now(clock),
                fingerprint.value(),
                metrics.publishedNodeCount(),
                metrics.publishedEdgeCount(),
                metrics.coveredMaterialCount(),
                metrics.isolatedNodeCount(),
                metrics.missingCoreRelationNodeCount(),
                metrics.pendingConflictCount(),
                metrics.recentActivities());
    }

    @Override
    public GraphWorkbenchOverviewFingerprint getFingerprint() {
        return workbenchRepository.getByOverviewFingerprint(schemaFingerprint(schemaResolver.coreRelationPolicies()));
    }

    GraphWorkbenchOverviewSourceImpl useClock(Clock clock) {
        if (clock != null) {
            this.clock = clock;
        }
        return this;
    }

    private String schemaFingerprint(List<GraphCoreRelationPolicy> policies) {
        return policies.stream()
                .sorted(Comparator.comparing(GraphCoreRelationPolicy::nodeType))
                .map(policy -> policy.nodeType() + ":"
                        + policy.relationTypes().stream().sorted().collect(Collectors.joining(",")))
                .collect(Collectors.joining(";"));
    }
}
