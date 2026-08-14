package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionDecisionCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionPrecheckCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionTaskProcessCommand;
import com.thundax.kuzhambu.knowledge.application.graph.command.GraphMaterialDeletionTaskRetryCommand;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialDeletionChangeQuery;
import com.thundax.kuzhambu.knowledge.application.graph.query.GraphMaterialDeletionTaskQuery;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialDeletionApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionDecision;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionTaskId;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialDeletionChangeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialDeletionTaskRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedEdgeMaterialRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphPublishedNodeMaterialRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraphMaterialDeletionApplicationServiceImpl implements GraphMaterialDeletionApplicationService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final GraphMaterialRepository materialRepository;
    private final GraphMaterialDeletionChangeRepository changeRepository;
    private final GraphMaterialDeletionTaskRepository taskRepository;
    private final GraphPublishedNodeMaterialRepository nodeMaterialRepository;
    private final GraphPublishedEdgeMaterialRepository edgeMaterialRepository;
    private final Clock clock;

    public GraphMaterialDeletionApplicationServiceImpl(
            GraphMaterialRepository materialRepository,
            GraphMaterialDeletionChangeRepository changeRepository,
            GraphMaterialDeletionTaskRepository taskRepository,
            GraphPublishedNodeMaterialRepository nodeMaterialRepository,
            GraphPublishedEdgeMaterialRepository edgeMaterialRepository,
            Clock clock) {
        this.materialRepository = materialRepository;
        this.changeRepository = changeRepository;
        this.taskRepository = taskRepository;
        this.nodeMaterialRepository = nodeMaterialRepository;
        this.edgeMaterialRepository = edgeMaterialRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public GraphMaterialDeletionChange precheck(GraphMaterialDeletionPrecheckCommand command) {
        ContentRef materialRef = requireMaterialRef(command == null ? null : command.materialRef());
        GraphMaterial material = materialRepository.getByContentRef(materialRef);
        if (material == null) {
            throw new BizException("Graph material does not exist");
        }
        List<GraphPublishedNodeMaterial> nodeMaterials = nodeMaterialRepository.listByMaterial(materialRef);
        List<GraphPublishedEdgeMaterial> edgeMaterials = edgeMaterialRepository.listByMaterial(materialRef);
        GraphMaterialDeletionStatus status = nodeMaterials.isEmpty() && edgeMaterials.isEmpty()
                ? GraphMaterialDeletionStatus.PRECHECKED
                : GraphMaterialDeletionStatus.AWAITING_DECISION;
        GraphMaterialDeletionChange change = new GraphMaterialDeletionChange(
                null,
                materialRef.getContentId(),
                materialRef,
                snapshotJson(material, nodeMaterials, edgeMaterials),
                null,
                status,
                0L,
                null,
                Instant.now(clock),
                null);
        change.setId(changeRepository.insert(change));
        return change;
    }

    @Override
    @Transactional
    public GraphMaterialDeletionTask decide(GraphMaterialDeletionDecisionCommand command) {
        if (command == null || command.changeId() == null || command.decision() == null) {
            throw new BizException("Graph material deletion decision command is required");
        }
        GraphMaterialDeletionChange change = changeRepository.getById(command.changeId());
        if (change == null) {
            throw new BizException("Graph material deletion change does not exist");
        }
        if (change.getLockVersion() != command.lockVersion()) {
            throw lockConflict();
        }
        if (command.decision() == GraphMaterialDeletionDecision.PRESERVE_CONTRIBUTION) {
            preserveContribution(change.getMaterialRef());
        } else {
            withdrawAssociations(change.getMaterialRef());
        }
        change.decide(command.decision(), Instant.now(clock));
        GraphMaterialDeletionChange updated = changeRepository.updateIfLockVersion(change, command.lockVersion());
        return enqueueTask(updated);
    }

    @Override
    public PageResult<GraphMaterialDeletionChange> pageChanges(
            GraphMaterialDeletionChangeQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        return changeRepository.page(
                query == null ? null : query.status(), effectivePage.getPageNo(), effectivePage.getPageSize());
    }

    @Override
    public PageResult<GraphMaterialDeletionTask> pageTasks(GraphMaterialDeletionTaskQuery query, PageQuery pageQuery) {
        PageQuery effectivePage = pageQuery == null ? new PageQuery() : pageQuery;
        effectivePage.normalize();
        return taskRepository.page(
                query == null ? null : query.status(), effectivePage.getPageNo(), effectivePage.getPageSize());
    }

    @Override
    public GraphMaterialDeletionTask getTask(GraphMaterialDeletionTaskId taskId) {
        if (taskId == null) {
            throw new BizException("Graph material deletion task id is required");
        }
        return taskRepository.getById(taskId);
    }

    @Override
    @Transactional
    public GraphMaterialDeletionTask retry(GraphMaterialDeletionTaskRetryCommand command) {
        if (command == null || command.taskId() == null) {
            throw new BizException("Graph material deletion retry command is required");
        }
        GraphMaterialDeletionTask task = requireTask(command.taskId());
        if (task.getLockVersion() != command.lockVersion()) {
            throw lockConflict();
        }
        task.retry();
        return taskRepository.updateIfLockVersion(task, command.lockVersion());
    }

    @Override
    @Transactional
    public List<GraphMaterialDeletionTask> processPendingTasks(GraphMaterialDeletionTaskProcessCommand command) {
        int limit = command == null ? 0 : command.limit();
        int effectiveLimit = limit <= 0 ? 20 : limit;
        return taskRepository.listByStatus(GraphMaterialDeletionStatus.PENDING, effectiveLimit).stream()
                .map(GraphMaterialDeletionTask::getId)
                .map(this::processTask)
                .toList();
    }

    private GraphMaterialDeletionTask processTask(GraphMaterialDeletionTaskId taskId) {
        GraphMaterialDeletionTask task = requireTask(taskId);
        if (task.getStatus() != GraphMaterialDeletionStatus.PENDING) {
            return task;
        }
        long pendingLockVersion = task.getLockVersion();
        task.startRunning();
        GraphMaterialDeletionTask running = taskRepository.updateIfLockVersion(task, pendingLockVersion);
        try {
            GraphMaterialDeletionChange change = changeRepository.getById(running.getDeletionChangeId());
            if (change == null || change.getDecision() == null) {
                throw new BizException("Graph material deletion change is not ready");
            }
            if (change.getDecision() == GraphMaterialDeletionDecision.PRESERVE_CONTRIBUTION) {
                preserveContribution(change.getMaterialRef());
            } else {
                withdrawAssociations(change.getMaterialRef());
            }
            running.succeed("{\"deleted\":true}", Instant.now(clock));
            return taskRepository.updateIfLockVersion(running, running.getLockVersion());
        } catch (RuntimeException exception) {
            running.fail(exception.getMessage(), Instant.now(clock));
            return taskRepository.updateIfLockVersion(running, running.getLockVersion());
        }
    }

    private void preserveContribution(ContentRef materialRef) {
        nodeMaterialRepository.listByMaterial(materialRef);
        edgeMaterialRepository.listByMaterial(materialRef);
        nodeMaterialRepository.deleteByMaterial(materialRef);
        edgeMaterialRepository.deleteByMaterial(materialRef);
    }

    private void withdrawAssociations(ContentRef materialRef) {
        nodeMaterialRepository.deleteByMaterial(materialRef);
        edgeMaterialRepository.deleteByMaterial(materialRef);
    }

    private GraphMaterialDeletionTask enqueueTask(GraphMaterialDeletionChange change) {
        GraphMaterialDeletionTask task = new GraphMaterialDeletionTask(
                null,
                change.getId(),
                "graph-material-deletion:" + change.getId().value(),
                GraphMaterialDeletionStatus.PENDING,
                0L,
                0,
                null,
                null,
                Instant.now(clock),
                null);
        task.setId(taskRepository.insert(task));
        return task;
    }

    private ContentRef requireMaterialRef(ContentRef materialRef) {
        if (materialRef == null) {
            throw new BizException("Graph material ref is required");
        }
        return materialRef;
    }

    private GraphMaterialDeletionTask requireTask(GraphMaterialDeletionTaskId taskId) {
        GraphMaterialDeletionTask task = taskRepository.getById(taskId);
        if (task == null) {
            throw new BizException("Graph material deletion task does not exist");
        }
        return task;
    }

    private String snapshotJson(
            GraphMaterial material,
            List<GraphPublishedNodeMaterial> nodeMaterials,
            List<GraphPublishedEdgeMaterial> edgeMaterials) {
        try {
            return OBJECT_MAPPER.writeValueAsString(Map.of(
                    "contentType",
                    material.getContentRef().getContentType(),
                    "contentRefId",
                    String.valueOf(material.getContentRef().getContentId()),
                    "materialStatus",
                    material.getStatus().value(),
                    "nodeSources",
                    nodeMaterials.stream()
                            .map(GraphPublishedNodeMaterial::getSourceSnapshotJson)
                            .toList(),
                    "edgeSources",
                    edgeMaterials.stream()
                            .map(GraphPublishedEdgeMaterial::getSourceSnapshotJson)
                            .toList()));
        } catch (JsonProcessingException exception) {
            throw new BizException("GRAPH-DELETION-00001", "knowledge.graph.deletion-snapshot", "图谱删除快照无效", exception);
        }
    }

    private BizException lockConflict() {
        return new BizException(
                GraphMaterialDeletionChange.LOCK_CONFLICT_CODE, "knowledge.graph.lock-conflict", "图谱对象已被其他操作修改，请刷新后重试");
    }
}
