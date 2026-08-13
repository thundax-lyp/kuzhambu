package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEventCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEventProcessCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEventRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialEventQuery;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialEventApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraphMaterialEventApplicationServiceImpl implements GraphMaterialEventApplicationService {

    private final GraphMaterialEventRepository eventRepository;

    public GraphMaterialEventApplicationServiceImpl(GraphMaterialEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public GraphMaterialEventId recordEvent(GraphMaterialEventCommand command) {
        if (command == null || command.materialRef() == null) {
            throw new BizException("Graph material event command is required");
        }
        if (command.eventType() != GraphMaterialEventType.DELETED) {
            throw new BizException("Only deleted graph material events are supported");
        }
        GraphMaterialEvent existing =
                eventRepository.getByMaterialRefAndType(command.materialRef(), command.eventType());
        if (existing != null) {
            return existing.getId();
        }
        GraphMaterialEvent event = new GraphMaterialEvent(
                null,
                command.materialRef(),
                command.eventType(),
                GraphMaterialEventStatus.SCHEDULED,
                command.changedAt(),
                0L);
        return eventRepository.insert(event);
    }

    @Override
    public PageResult<GraphMaterialEvent> pageEvents(GraphMaterialEventQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        return eventRepository.page(
                query == null ? null : query.materialRef(),
                query == null ? null : query.eventType(),
                query == null ? null : query.status(),
                effectivePage.getPageNo(),
                effectivePage.getPageSize());
    }

    @Override
    @Transactional
    public GraphMaterialEvent retryEvent(GraphMaterialEventRetryCommand command) {
        GraphMaterialEvent event = requireEvent(command == null ? null : command.eventId());
        event.requireLockVersion(command.lockVersion());
        event.scheduleRetry();
        updateEvent(event, command.lockVersion());
        return eventRepository.getById(event.getId());
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public GraphMaterialEvent processEvent(GraphMaterialEventProcessCommand command) {
        GraphMaterialEvent event = requireEvent(command == null ? null : command.eventId());
        event.requireLockVersion(command.lockVersion());
        event.startProcessing();
        updateEvent(event, command.lockVersion());
        GraphMaterialEvent processing = eventRepository.getById(event.getId());
        processing.succeed();
        updateEvent(processing, processing.getLockVersion());
        return eventRepository.getById(event.getId());
    }

    private GraphMaterialEvent requireEvent(GraphMaterialEventId eventId) {
        GraphMaterialEvent event = eventRepository.getById(eventId);
        if (event == null) {
            throw new BizException("Graph material event does not exist");
        }
        return event;
    }

    private void updateEvent(GraphMaterialEvent event, long expectedLockVersion) {
        if (eventRepository.updateIfLockVersion(event, expectedLockVersion) != 1) {
            throw new BizException("Graph material event lock version mismatch");
        }
    }
}
