package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphIncidentEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphQualityQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphSearchQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphIncidentEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphQualityResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphRecentEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphSearchResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphWorkbenchOverviewResult;

public interface GraphWorkbenchApplicationService {

    GraphWorkbenchOverviewResult getOverview();

    GraphRecentEdgesResult listRecentEdges();

    GraphIncidentEdgesResult listIncidentEdges(GraphIncidentEdgesQuery query, PageQuery pageQuery);

    PageResult<GraphSearchResult> search(GraphSearchQuery query, PageQuery pageQuery);

    GraphQualityResult getQuality(GraphQualityQuery query);
}
