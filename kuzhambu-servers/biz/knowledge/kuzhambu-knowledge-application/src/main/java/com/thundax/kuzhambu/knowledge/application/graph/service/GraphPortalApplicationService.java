package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedGraphResult;

public interface GraphPortalApplicationService {

    GraphPublishedGraphResult getMaterialGraph(GraphMaterialQuery query);
}
