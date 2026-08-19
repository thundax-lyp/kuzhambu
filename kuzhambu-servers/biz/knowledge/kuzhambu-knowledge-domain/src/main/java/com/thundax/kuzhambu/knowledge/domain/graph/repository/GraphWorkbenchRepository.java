package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphCoreRelationPolicy;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphPublishedSearchHit;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphQualitySnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchMetrics;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewFingerprint;
import java.util.List;

public interface GraphWorkbenchRepository {
    GraphWorkbenchMetrics getByOverview(List<GraphCoreRelationPolicy> coreRelationPolicies);

    GraphWorkbenchOverviewFingerprint getByOverviewFingerprint(String schemaFingerprint);

    PageResult<GraphPublishedSearchHit> page(
            String keyword, GraphNodeType nodeType, String relationType, int pageNo, int pageSize);

    GraphQualitySnapshot getByQuality(
            String issueType,
            GraphNodeType nodeType,
            int sampleLimit,
            List<GraphCoreRelationPolicy> coreRelationPolicies);
}
