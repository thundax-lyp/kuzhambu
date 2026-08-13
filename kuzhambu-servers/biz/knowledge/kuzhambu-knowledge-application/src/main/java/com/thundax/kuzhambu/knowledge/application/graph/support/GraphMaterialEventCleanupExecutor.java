package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEventRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GraphMaterialEventCleanupExecutor {

    private final GraphMaterialEventRepository eventRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final GraphMaterialEdgeRepository materialEdgeRepository;
    private final GraphMaterialNodeRepository materialNodeRepository;
    private final GraphMaterialVersionRepository materialVersionRepository;
    private final GraphMaterialRepository materialRepository;

    public GraphMaterialEventCleanupExecutor(
            GraphMaterialEventRepository eventRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            GraphMaterialEdgeRepository materialEdgeRepository,
            GraphMaterialNodeRepository materialNodeRepository,
            GraphMaterialVersionRepository materialVersionRepository,
            GraphMaterialRepository materialRepository) {
        this.eventRepository = eventRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.materialEdgeRepository = materialEdgeRepository;
        this.materialNodeRepository = materialNodeRepository;
        this.materialVersionRepository = materialVersionRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public GraphMaterialEvent cleanup(GraphMaterialEventId eventId, long processingLockVersion) {
        GraphMaterialEvent event = requireProcessingEvent(eventId, processingLockVersion);
        edgeMaterialRepository.deleteByMaterial(event.getMaterialRef());
        nodeMaterialRepository.deleteByMaterial(event.getMaterialRef());
        materialEdgeRepository.deleteByMaterial(event.getMaterialRef());
        materialNodeRepository.deleteByMaterial(event.getMaterialRef());
        materialVersionRepository.deleteByMaterial(event.getMaterialRef());
        materialRepository.deleteByContentRef(event.getMaterialRef());
        event.succeed();
        if (eventRepository.updateIfLockVersion(event, processingLockVersion) != 1) {
            throw new BizException("Graph material event lock version mismatch");
        }
        return eventRepository.getById(eventId);
    }

    private GraphMaterialEvent requireProcessingEvent(GraphMaterialEventId eventId, long processingLockVersion) {
        GraphMaterialEvent event = eventRepository.getById(eventId);
        if (event == null) {
            throw new BizException("Graph material event does not exist");
        }
        event.requireLockVersion(processingLockVersion);
        if (event.getStatus() != GraphMaterialEventStatus.PROCESSING) {
            throw new BizException("Graph material event is not processing");
        }
        return event;
    }
}
