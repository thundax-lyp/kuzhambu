package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEventCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEventProcessCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialEventRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialEventQuery;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;

public interface GraphMaterialEventApplicationService {

    GraphMaterialEventId recordEvent(GraphMaterialEventCommand command);

    PageResult<GraphMaterialEvent> pageEvents(GraphMaterialEventQuery query, PageQuery pageQuery);

    GraphMaterialEvent retryEvent(GraphMaterialEventRetryCommand command);

    GraphMaterialEvent processEvent(GraphMaterialEventProcessCommand command);

    GraphMaterialEvent processScheduledEvent(GraphMaterialEventId eventId, long lockVersion);

    GraphMaterialEvent reclaimStaleProcessingEvent(GraphMaterialEventId eventId, long lockVersion);
}
