package com.thundax.kuzhambu.knowledge.application.graph.service;

public interface GraphWorkbenchSnapshotRefresher {

    void refreshIfRequired(GraphWorkbenchRefreshReason reason);
}
