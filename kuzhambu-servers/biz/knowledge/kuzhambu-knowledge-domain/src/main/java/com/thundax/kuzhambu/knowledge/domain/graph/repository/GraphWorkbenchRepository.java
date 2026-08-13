package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphPublishedSearchHit;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphQualitySnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchMetrics;

public interface GraphWorkbenchRepository {
    GraphWorkbenchMetrics getOverview();

    PageResult<GraphPublishedSearchHit> search(
            String keyword, GraphNodeType nodeType, String relationType, int pageNo, int pageSize);

    GraphQualitySnapshot getQuality(String issueType, GraphNodeType nodeType, int sampleLimit);
}
