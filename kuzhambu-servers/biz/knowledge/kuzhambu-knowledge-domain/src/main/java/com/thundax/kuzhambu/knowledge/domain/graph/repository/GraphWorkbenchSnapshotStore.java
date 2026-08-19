package com.thundax.kuzhambu.knowledge.domain.graph.repository;

import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
import java.util.Optional;

/** Port for the externally stored workbench overview snapshot and its rebuild lease. */
public interface GraphWorkbenchSnapshotStore {
    Optional<GraphWorkbenchOverviewSnapshot> get();

    void replace(GraphWorkbenchOverviewSnapshot snapshot);

    Optional<String> getByLock();

    void deleteByLockToken(String token);
}
