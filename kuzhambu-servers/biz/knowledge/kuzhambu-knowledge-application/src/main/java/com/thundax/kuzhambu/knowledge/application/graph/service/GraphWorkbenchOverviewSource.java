package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewFingerprint;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;

/** Loads formal graph data for the asynchronous workbench overview read model. */
public interface GraphWorkbenchOverviewSource {

    GraphWorkbenchOverviewSnapshot load();

    GraphWorkbenchOverviewFingerprint getFingerprint();
}
