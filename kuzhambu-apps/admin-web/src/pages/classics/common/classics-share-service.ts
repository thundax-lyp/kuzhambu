import { postJson } from "@/api/http";
import type {
    ClassicsBatchOperationRecord,
    ClassicsShareLinkStatus,
    ClassicsShareAccessRecord,
    ClassicsShareRecord,
    ClassicsShareTargetRef,
    ClassicsShareVisibility
} from "./classics-share-types";
import type { Page } from "@/types/page";

export interface ClassicsShareCreateCommand {
    expiresAt?: string | null;
    status?: ClassicsShareLinkStatus | null;
    targets: ClassicsShareTargetRef[];
    title?: string | null;
    visibility?: ClassicsShareVisibility | null;
}

export interface ClassicsBatchShareCreateCommand {
    expiresAt?: string | null;
    privateContentConfirmed: boolean;
    status?: ClassicsShareLinkStatus | null;
    targets: ClassicsShareTargetRef[];
    titlePrefix?: string | null;
    visibility: ClassicsShareVisibility;
    visibilityRiskStatus?: string | null;
}

export interface ClassicsShareQuery {
    contentType?: string | null;
    issuedAfter?: string | null;
    issuedBefore?: string | null;
    pageNo?: number;
    pageSize?: number;
    status?: ClassicsShareLinkStatus | string | null;
    title?: string | null;
    visibility?: ClassicsShareVisibility | string | null;
}

export interface ClassicsShareStatusUpdateCommand {
    id: number;
    status: ClassicsShareLinkStatus | string;
}

export interface ClassicsShareAccessRecordQuery {
    shareLinkId: number;
    shareTargetId?: number | null;
    pageNo?: number;
    pageSize?: number;
}

const SHARE_PATH = "/classics/shares";

export const create = (request: ClassicsShareCreateCommand) => {
    return postJson<ClassicsShareRecord, ClassicsShareCreateCommand>(`${SHARE_PATH}/create`, {
        body: request
    });
};

export const createBatch = (request: ClassicsBatchShareCreateCommand) => {
    return postJson<ClassicsBatchOperationRecord, ClassicsBatchShareCreateCommand>(
        `${SHARE_PATH}/batch/create`,
        {
            body: request
        }
    );
};

export const page = (request: ClassicsShareQuery = {}) => {
    return postJson<Page<ClassicsShareRecord>, ClassicsShareQuery>(`${SHARE_PATH}/page`, {
        body: request
    });
};

export const get = (id: number) => {
    return postJson<ClassicsShareRecord, { id: number }>(`${SHARE_PATH}/get`, {
        body: { id }
    });
};

export const updateStatus = (request: ClassicsShareStatusUpdateCommand) => {
    return postJson<void, ClassicsShareStatusUpdateCommand>(`${SHARE_PATH}/status/update`, {
        body: request
    });
};

export const pageAccessRecords = (request: ClassicsShareAccessRecordQuery) => {
    return postJson<Page<ClassicsShareAccessRecord>, ClassicsShareAccessRecordQuery>(
        `${SHARE_PATH}/access-records/page`,
        {
            body: request
        }
    );
};
