package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GraphMaterialEventFailureRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphMaterialEventFailureRecorder.class);

    private final GraphMaterialEventRepository eventRepository;

    public GraphMaterialEventFailureRecorder(GraphMaterialEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public GraphMaterialEvent recordFailure(GraphMaterialEventId eventId, long processingLockVersion, Throwable cause) {
        GraphMaterialEvent event = eventRepository.getById(eventId);
        if (event == null) {
            throw new BizException("Graph material event does not exist");
        }
        event.requireLockVersion(processingLockVersion);
        if (event.getStatus() != GraphMaterialEventStatus.PROCESSING) {
            throw new BizException("Graph material event is not processing");
        }
        event.fail();
        if (eventRepository.updateIfLockVersion(event, processingLockVersion) != 1) {
            throw new BizException("Graph material event lock version mismatch");
        }
        LOGGER.error("Graph material event cleanup failed, eventId={}", eventId, cause);
        return eventRepository.getById(eventId);
    }
}
