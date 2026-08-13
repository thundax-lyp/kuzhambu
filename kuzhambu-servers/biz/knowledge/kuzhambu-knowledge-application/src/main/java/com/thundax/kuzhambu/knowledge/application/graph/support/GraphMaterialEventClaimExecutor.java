package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEventProcessCommand;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GraphMaterialEventClaimExecutor {

    private final GraphMaterialEventRepository eventRepository;

    public GraphMaterialEventClaimExecutor(GraphMaterialEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public GraphMaterialEvent claim(GraphMaterialEventProcessCommand command) {
        if (command == null || command.eventId() == null) {
            throw new BizException("Graph material event process command is required");
        }
        GraphMaterialEvent event = eventRepository.getById(command.eventId());
        if (event == null) {
            throw new BizException("Graph material event does not exist");
        }
        event.requireLockVersion(command.lockVersion());
        event.startProcessing();
        if (eventRepository.updateIfLockVersion(event, command.lockVersion()) != 1) {
            return null;
        }
        return eventRepository.getById(command.eventId());
    }
}
