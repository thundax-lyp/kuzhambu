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
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEdgeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEventRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class GraphMaterialEventApplicationServiceImpl implements GraphMaterialEventApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphMaterialEventApplicationServiceImpl.class);

    private final GraphMaterialEventRepository eventRepository;
    private final GraphMaterialEdgeRepository materialEdgeRepository;
    private final GraphMaterialNodeRepository materialNodeRepository;
    private final GraphMaterialVersionRepository materialVersionRepository;
    private final GraphMaterialRepository materialRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final TransactionTemplate requiresNewTransaction;

    public GraphMaterialEventApplicationServiceImpl(
            GraphMaterialEventRepository eventRepository,
            GraphMaterialEdgeRepository materialEdgeRepository,
            GraphMaterialNodeRepository materialNodeRepository,
            GraphMaterialVersionRepository materialVersionRepository,
            GraphMaterialRepository materialRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            PlatformTransactionManager transactionManager) {
        this.eventRepository = eventRepository;
        this.materialEdgeRepository = materialEdgeRepository;
        this.materialNodeRepository = materialNodeRepository;
        this.materialVersionRepository = materialVersionRepository;
        this.materialRepository = materialRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.requiresNewTransaction = new TransactionTemplate(transactionManager);
        this.requiresNewTransaction.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
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
        GraphMaterialEvent claimed = requiresNewTransaction.execute(status -> claim(command));
        if (claimed == null) {
            return requireEvent(command == null ? null : command.eventId());
        }
        try {
            return requiresNewTransaction.execute(status -> cleanup(claimed.getId(), claimed.getLockVersion()));
        } catch (RuntimeException ex) {
            return requiresNewTransaction.execute(
                    status -> recordFailure(claimed.getId(), claimed.getLockVersion(), ex));
        }
    }

    private GraphMaterialEvent claim(GraphMaterialEventProcessCommand command) {
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

    private GraphMaterialEvent cleanup(GraphMaterialEventId eventId, long processingLockVersion) {
        GraphMaterialEvent event = requireProcessingEvent(eventId, processingLockVersion);
        edgeMaterialRepository.deleteByMaterial(event.getMaterialRef());
        nodeMaterialRepository.deleteByMaterial(event.getMaterialRef());
        materialEdgeRepository.deleteByMaterial(event.getMaterialRef());
        materialNodeRepository.deleteByMaterial(event.getMaterialRef());
        materialVersionRepository.deleteByMaterial(event.getMaterialRef());
        materialRepository.deleteByContentRef(event.getMaterialRef());
        event.succeed();
        updateEvent(event, processingLockVersion);
        return eventRepository.getById(eventId);
    }

    private GraphMaterialEvent recordFailure(
            GraphMaterialEventId eventId, long processingLockVersion, Throwable cause) {
        GraphMaterialEvent event = requireProcessingEvent(eventId, processingLockVersion);
        event.fail();
        updateEvent(event, processingLockVersion);
        LOGGER.error("Graph material event cleanup failed, eventId={}", eventId, cause);
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
