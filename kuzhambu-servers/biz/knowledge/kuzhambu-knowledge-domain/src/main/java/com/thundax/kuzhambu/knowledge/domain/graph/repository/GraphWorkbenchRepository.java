package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphPublishedSearchHit;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphQualitySnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchMetrics;

public interface GraphWorkbenchRepository {
    GraphWorkbenchMetrics getByOverview();

    PageResult<GraphPublishedSearchHit> page(
            String keyword, GraphNodeType nodeType, String relationType, int pageNo, int pageSize);

    GraphQualitySnapshot getByQuality(String issueType, GraphNodeType nodeType, int sampleLimit);
}
