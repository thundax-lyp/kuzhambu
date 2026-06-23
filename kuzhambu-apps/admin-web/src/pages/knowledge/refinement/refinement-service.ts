import { postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type {
    ConfirmRefinementEntityCommand,
    ConfirmRefinementRelationCommand,
    DeleteRefinementEntityCommand,
    DeleteRefinementRelationCommand,
    QualityAnnotationPageQuery,
    QualityAnnotationRecord,
    QualitySummaryRecord,
    RefinementDetailRecord,
    RefinementEntityRecord,
    RefinementRelationRecord,
    RefinementTaskApplyCommand,
    RefinementTaskIdCommand,
    RefinementTaskOpenCommand,
    RefinementTaskPageQuery,
    RefinementWorkbenchItem,
    UpsertQualityAnnotationCommand,
    UpsertRefinementEntityCommand,
    UpsertRefinementLineageNodeCommand,
    UpsertRefinementLineageRelationCommand,
    UpsertRefinementRelationCommand
} from "./refinement-types";

const API_PREFIX = "/knowledge/refinement";

export const pageTasks = (request: RefinementTaskPageQuery = {}) => {
    return postJson<Page<RefinementWorkbenchItem>, RefinementTaskPageQuery>(
        `${API_PREFIX}/task/page`,
        { body: request }
    );
};

export const openTask = (request: RefinementTaskOpenCommand) => {
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
    return postJson<RefinementDetailRecord, RefinementTaskApplyCommand>(
        `${API_PREFIX}/task/apply`,
        { body: request }
    );
};

export const qualitySummary = (request: RefinementTaskIdCommand) => {
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

export const upsertAnnotation = (request: UpsertQualityAnnotationCommand) => {
    return postJson<QualityAnnotationRecord, UpsertQualityAnnotationCommand>(
        `${API_PREFIX}/annotation/update`,
        { body: request }
    );
};

export const pageAnnotations = (request: QualityAnnotationPageQuery) => {
    return postJson<Page<QualityAnnotationRecord>, QualityAnnotationPageQuery>(
        `${API_PREFIX}/annotation/page`,
        { body: request }
    );
};
