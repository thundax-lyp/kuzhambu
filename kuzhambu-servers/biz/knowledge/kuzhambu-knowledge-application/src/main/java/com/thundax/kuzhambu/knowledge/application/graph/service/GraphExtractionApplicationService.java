package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionApplyCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphExtractionQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;

public interface GraphExtractionApplicationService {

    GraphExtractionResult startExtraction(GraphExtractionCommand command);

    GraphExtractionResult retryExtraction(GraphExtractionRetryCommand command);

    GraphExtractionResult getCurrentExtraction(GraphExtractionQuery query);

    PageResult<GraphExtractionResult> pageExtractionHistory(GraphExtractionQuery query, PageQuery pageQuery);

    GraphMaterialResult applyExtractionResult(GraphExtractionApplyCommand command);
}
