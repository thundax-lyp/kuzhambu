package com.thundax.kuzhambu.knowledge.application.graph.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionBatchCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCancelCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCandidateDiscardCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRegenerateCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphExtractionRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphActiveTaskSyncQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskDetailQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionBatchResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionTaskResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;

public interface GraphExtractionApplicationService {

    GraphExtractionTaskResult createExtraction(GraphExtractionCommand command);

    GraphExtractionBatchResult createBatchExtraction(GraphExtractionBatchCommand command);

    PageResult<GraphExtractionTaskResult> pageTasks(GraphTaskQuery query, PageQuery pageQuery);

    GraphExtractionTaskDetailResult getTask(GraphTaskDetailQuery query);

    GraphExtractionTaskResult retryTask(GraphExtractionRetryCommand command);

    int syncActiveTasks();

    int recoverActiveTasksAtStartup();

    int syncActiveTasks(GraphActiveTaskSyncQuery query);

    GraphExtractionTaskResult cancelTask(GraphExtractionCancelCommand command);

    GraphMaterialResult applyCandidate(GraphExtractionCandidateApplyCommand command);

    GraphExtractionTaskResult discardCandidate(GraphExtractionCandidateDiscardCommand command);

    GraphExtractionTaskResult regenerateTask(GraphExtractionRegenerateCommand command);
}
