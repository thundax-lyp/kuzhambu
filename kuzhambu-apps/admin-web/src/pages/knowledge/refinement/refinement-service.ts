import { postJson } from "@/api/http";
import type { Page, PageQuery } from "@/types/page";
import type {
    QualityAnnotationLabel,
    QualityAnnotationObjectType,
    QualityAnnotationRecord,
    QualityAnnotationStatus,
    RefinementApplyRecord,
    RefinementDetailRecord,
    RefinementEntityRecord,
    RefinementTaskStatus,
    RefinementTaskType,
    QualitySummaryRecord,
    RefinementRelationRecord,
    RefinementWorkbenchRecord
} from "./refinement-types";

const API_PREFIX = "/knowledge/refinement";

export type RefinementTaskPageQuery = PageQuery<{
    taskType?: RefinementTaskType | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    sourceCategoryCode?: string | null;
    status?: RefinementTaskStatus | null;
}>;

export interface RefinementTaskOpenCommand {
    graphVersionId: number;
    openedBy?: number | null;
}

export interface RefinementTaskIdCommand {
    refinementTaskId: number;
}

export interface RefinementTaskApplyCommand extends RefinementTaskIdCommand {
    appliedBy?: number | null;
}

export interface UpsertRefinementEntityCommand {
    refinementTaskId: number;
    entityId?: number | null;
    entityKey?: string | null;
    name?: string | null;
    entityType?: string | null;
    description?: string | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
    operatorId?: number | null;
}

export interface ConfirmRefinementEntityCommand {
    refinementTaskId: number;
    entityKey: string;
    operatorId?: number | null;
}

export interface DeleteRefinementEntityCommand {
    refinementTaskId: number;
    entityKey: string;
    operatorId?: number | null;
}

export interface UpsertRefinementRelationCommand {
    refinementTaskId: number;
    relationId?: number | null;
    relationKey?: string | null;
    sourceEntityKey?: string | null;
    targetEntityKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
    operatorId?: number | null;
}

export interface ConfirmRefinementRelationCommand {
    refinementTaskId: number;
    relationKey: string;
    operatorId?: number | null;
}

export interface DeleteRefinementRelationCommand {
    refinementTaskId: number;
    relationKey: string;
    operatorId?: number | null;
}

export interface UpsertRefinementLineageNodeCommand {
    refinementTaskId: number;
    nodeId?: number | null;
    nodeKey?: string | null;
    name?: string | null;
    nodeType?: string | null;
    generation?: number | null;
    gender?: string | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
    operatorId?: number | null;
}

export interface UpsertRefinementLineageRelationCommand {
    refinementTaskId: number;
    relationId?: number | null;
    relationKey?: string | null;
    sourceNodeKey?: string | null;
    targetNodeKey?: string | null;
    sourceName?: string | null;
    targetName?: string | null;
    relationType?: string | null;
    evidence?: string | null;
    sourceRefsJson?: string | null;
    sortOrder?: number | null;
    operatorId?: number | null;
}

export interface UpsertQualityAnnotationCommand {
    annotationId?: number | null;
    objectType?: QualityAnnotationObjectType | null;
    objectKey?: string | null;
    sourceContentType?: string | null;
    sourceContentId?: number | null;
    graphVersionId?: number | null;
    annotationStatus?: QualityAnnotationStatus | null;
    annotationLabel?: QualityAnnotationLabel | null;
    comment?: string | null;
    operatorId?: number | null;
}

export interface DeleteQualityAnnotationCommand {
    annotationId: number;
    operatorId?: number | null;
}

export type QualityAnnotationPageQuery = PageQuery<{
    refinementTaskId: number;
    objectType?: string | null;
}>;

export const pageTasks = (request: RefinementTaskPageQuery = {}) => {
    return postJson<Page<RefinementWorkbenchRecord>, RefinementTaskPageQuery>(
        `${API_PREFIX}/task/page`,
        { body: request }
    );
};

export const getTaskDraft = (request: RefinementTaskOpenCommand) => {
    return postJson<RefinementDetailRecord, RefinementTaskOpenCommand>(`${API_PREFIX}/task/open`, {
        body: request
    });
};

export const getTaskDetail = (request: RefinementTaskIdCommand) => {
    return postJson<RefinementDetailRecord, RefinementTaskIdCommand>(`${API_PREFIX}/task/detail`, {
        body: request
    });
};

export const addEntity = (request: UpsertRefinementEntityCommand) => {
    return postJson<RefinementEntityRecord, UpsertRefinementEntityCommand>(
        `${API_PREFIX}/entity/add`,
        { body: request }
    );
};

export const updateEntity = (request: UpsertRefinementEntityCommand) => {
    return postJson<RefinementEntityRecord, UpsertRefinementEntityCommand>(
        `${API_PREFIX}/entity/update`,
        { body: request }
    );
};

export const confirmEntity = (request: ConfirmRefinementEntityCommand) => {
    return postJson<RefinementEntityRecord, ConfirmRefinementEntityCommand>(
        `${API_PREFIX}/entity/confirm`,
        { body: request }
    );
};

export const deleteEntity = (request: DeleteRefinementEntityCommand) => {
    return postJson<void, DeleteRefinementEntityCommand>(`${API_PREFIX}/entity/delete`, {
        body: request
    });
};

export const addRelation = (request: UpsertRefinementRelationCommand) => {
    return postJson<RefinementRelationRecord, UpsertRefinementRelationCommand>(
        `${API_PREFIX}/relation/add`,
        { body: request }
    );
};

export const updateRelation = (request: UpsertRefinementRelationCommand) => {
    return postJson<RefinementRelationRecord, UpsertRefinementRelationCommand>(
        `${API_PREFIX}/relation/update`,
        { body: request }
    );
};

export const confirmRelation = (request: ConfirmRefinementRelationCommand) => {
    return postJson<RefinementRelationRecord, ConfirmRefinementRelationCommand>(
        `${API_PREFIX}/relation/confirm`,
        { body: request }
    );
};

export const deleteRelation = (request: DeleteRefinementRelationCommand) => {
    return postJson<void, DeleteRefinementRelationCommand>(`${API_PREFIX}/relation/delete`, {
        body: request
    });
};

export const applyTask = (request: RefinementTaskApplyCommand) => {
    return postJson<RefinementApplyRecord, RefinementTaskApplyCommand>(`${API_PREFIX}/task/apply`, {
        body: request
    });
};

export const getQualitySummary = (request: RefinementTaskIdCommand) => {
    return postJson<QualitySummaryRecord, RefinementTaskIdCommand>(
        `${API_PREFIX}/quality/summary`,
        { body: request }
    );
};

export const addLineageNode = (request: UpsertRefinementLineageNodeCommand) => {
    return postJson(`${API_PREFIX}/lineage-node/add`, { body: request });
};

export const updateLineageNode = (request: UpsertRefinementLineageNodeCommand) => {
    return postJson(`${API_PREFIX}/lineage-node/update`, { body: request });
};

export const addLineageRelation = (request: UpsertRefinementLineageRelationCommand) => {
    return postJson(`${API_PREFIX}/lineage-relation/add`, { body: request });
};

export const updateLineageRelation = (request: UpsertRefinementLineageRelationCommand) => {
    return postJson(`${API_PREFIX}/lineage-relation/update`, { body: request });
};

export const updateAnnotation = (request: UpsertQualityAnnotationCommand) => {
    return postJson<QualityAnnotationRecord, UpsertQualityAnnotationCommand>(
        `${API_PREFIX}/annotation/update`,
        { body: request }
    );
};

export const deleteAnnotation = (request: DeleteQualityAnnotationCommand) => {
    return postJson<void, DeleteQualityAnnotationCommand>(`${API_PREFIX}/annotation/delete`, {
        body: request
    });
};

export const pageAnnotations = (request: QualityAnnotationPageQuery) => {
    return postJson<Page<QualityAnnotationRecord>, QualityAnnotationPageQuery>(
        `${API_PREFIX}/annotation/page`,
        { body: request }
    );
};
