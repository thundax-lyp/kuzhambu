package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphOneHopEdgesQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphOneHopEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPortalOverviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedGraphResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphRecentEdgesResult;

public interface GraphPortalApplicationService {

    GraphPublishedGraphResult getMaterialGraph(GraphMaterialQuery query);

    GraphPortalOverviewResult getOverview();

    GraphRecentEdgesResult listRecentEdges();

    GraphOneHopEdgesResult listOneHopEdges(GraphOneHopEdgesQuery query);
}
