package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.CleanupKnowledgeGraphCandidateFacadeRequest;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraphExtractionTaskCleanupOperator {

    private final GraphExtractionTaskRepository taskRepository;
    private final AiFacade aiFacade;

    public GraphExtractionTaskCleanupOperator(GraphExtractionTaskRepository taskRepository, AiFacade aiFacade) {
        this.taskRepository = taskRepository;
        this.aiFacade = aiFacade;
    }

    @Transactional
    public void cleanup(GraphExtractionTask task) {
        if (task.getCandidateId() != null) {
            aiFacade.cleanupKnowledgeGraphCandidate(CleanupKnowledgeGraphCandidateFacadeRequest.builder()
                    .candidateId(task.getCandidateId())
                    .build());
        }
        if (taskRepository.deleteByIdAndLockVersion(task.getId(), task.getLockVersion()) != 1) {
            throw new BizException(
                    "GRAPH_TASK_LOCK_CONFLICT",
                    "graph.task.lock-conflict",
                    "Graph extraction task cleanup lost its optimistic lock");
        }
    }
}
