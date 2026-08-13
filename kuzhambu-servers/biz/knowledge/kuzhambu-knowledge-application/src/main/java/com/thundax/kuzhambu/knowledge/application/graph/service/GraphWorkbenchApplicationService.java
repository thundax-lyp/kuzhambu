package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphIncidentEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphQualityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphSearchQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphQualityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphSearchResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWorkbenchOverviewResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import java.util.List;

public interface GraphWorkbenchApplicationService {

    GraphWorkbenchOverviewResult getOverview();

    List<GraphPublishedNode> listRecentSeedNodes();

    GraphPublishedEdgeSlice listIncidentEdges(GraphIncidentEdgesQuery query, PageQuery pageQuery);

    PageResult<GraphSearchResult> search(GraphSearchQuery query, PageQuery pageQuery);

    GraphQualityResult getQuality(GraphQualityQuery query);
}
