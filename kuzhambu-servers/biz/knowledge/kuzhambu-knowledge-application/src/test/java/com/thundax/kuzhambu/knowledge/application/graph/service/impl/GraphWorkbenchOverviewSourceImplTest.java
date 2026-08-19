package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphSchemaResolver;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphCoreRelationPolicy;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchMetrics;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewFingerprint;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GraphWorkbenchOverviewSourceImplTest {

    @Test
    void loadShouldUseStableSchemaFingerprintAndMapFormalMetrics() {
        GraphWorkbenchRepository repository = mock(GraphWorkbenchRepository.class);
        GraphSchemaResolver schemaResolver = mock(GraphSchemaResolver.class);
        List<GraphCoreRelationPolicy> policies = List.of(
                new GraphCoreRelationPolicy("WORK", List.of("AUTHORED_BY", "MENTIONED_IN")),
                new GraphCoreRelationPolicy("PERSON", List.of("RELATED_TO")));
        GraphWorkbenchOverviewFingerprint fingerprint = fingerprint();
        when(schemaResolver.coreRelationPolicies()).thenReturn(policies);
        when(repository.getByOverviewFingerprint(any())).thenReturn(fingerprint);
        when(repository.getByOverview(policies))
                .thenReturn(new GraphWorkbenchMetrics(11L, 12L, 13L, 14L, 15L, List.of(), 16L));

        var snapshot = new GraphWorkbenchOverviewSourceImpl(repository, schemaResolver)
                .useClock(Clock.fixed(Instant.ofEpochMilli(1_000L), ZoneOffset.UTC))
                .load();

        ArgumentCaptor<String> fingerprintCaptor = ArgumentCaptor.forClass(String.class);
        verify(repository).getByOverviewFingerprint(fingerprintCaptor.capture());
        assertThat(fingerprintCaptor.getValue()).isEqualTo("PERSON:RELATED_TO;WORK:AUTHORED_BY,MENTIONED_IN");
        assertThat(snapshot.generatedAt()).isEqualTo(Instant.ofEpochMilli(1_000L));
        assertThat(snapshot.sourceFingerprint()).isEqualTo(fingerprint.value());
        assertThat(snapshot.publishedNodeCount()).isEqualTo(11L);
        assertThat(snapshot.pendingConflictCount()).isEqualTo(16L);
    }

    private static GraphWorkbenchOverviewFingerprint fingerprint() {
        return new GraphWorkbenchOverviewFingerprint(
                1L, 10L, 2L, 20L, 3L, 30L, 4L, 40L, 5L, 50L, 6L, 60L, 7L, 70L, 8L, null, "schema-v1");
    }
}
