import { postJson } from "@/api/http";
import type { DictItem } from "@/types/dict";
import type { Page, PageQuery } from "@/types/page";
import type {
    MingCustomsContentVersionRecord,
    MingCustomsKeywordCloudRecord,
    MingCustomsPublicationActionRecord,
    MingCustomsPublicationBatchRecord,
    MingCustomsRecord,
    MingCustomsTagCloudRecord
} from "./ming-customs-types";

const CATEGORY_DICT_TYPE = "CLASSICS_MING_CUSTOMS_CATEGORY";

export type MingCustomsQuery = PageQuery<{
    keyword?: string | null;
    category?: string | null;
    visibility?: string | null;
    tagName?: string | null;
    tagId?: string | null;
    tagNameSnapshot?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}>;

export interface MingCustomsCommand {
    id?: string | null;
    title?: string | null;
    category?: string | null;
    chapter?: string | null;
    section?: string | null;
    summary?: string | null;
    contentFormat?: string | null;
    content?: string | null;
    originalExcerpts?: string | null;
    visibility?: string | null;
}

export interface MingCustomsTagCloudQuery {
    keyword?: string | null;
    category?: string | null;
    visibility?: string | null;
}

interface MingCustomsVersionCommand {
    id: string;
    versionId?: string | null;
}

export interface MingCustomsPublicationActionCommand {
    id: string;
}

export interface MingCustomsPublicationBatchCommand {
    ids: string[];
}

export const page = (request: MingCustomsQuery = {}) => {
    return postJson<Page<MingCustomsRecord>, MingCustomsQuery>("/classics/ming-customs/page", {
        body: request
    });
};

export const get = (id: string) => {
    return postJson<MingCustomsRecord, MingCustomsCommand>("/classics/ming-customs/get", {
        body: { id }
    });
};

export const add = (request: MingCustomsCommand) => {
    return postJson<MingCustomsRecord, MingCustomsCommand>("/classics/ming-customs/add", {
        body: request
    });
};

export const update = (request: MingCustomsCommand) => {
    return postJson<MingCustomsRecord, MingCustomsCommand>("/classics/ming-customs/update", {
        body: request
    });
};

export const deleteById = (id: string) => {
    return postJson<void, MingCustomsCommand>("/classics/ming-customs/delete", {
        body: { id }
    });
};

export const publish = (command: MingCustomsPublicationActionCommand) => {
    return postJson<MingCustomsPublicationActionRecord, MingCustomsPublicationActionCommand>(
        "/classics/ming-customs/publish",
        { body: command }
    );
};

export const submitOffline = (command: MingCustomsPublicationActionCommand) => {
    return postJson<MingCustomsPublicationActionRecord, MingCustomsPublicationActionCommand>(
        "/classics/ming-customs/offline",
        { body: command }
    );
};

export const publishBatch = (command: MingCustomsPublicationBatchCommand) => {
    return postJson<MingCustomsPublicationBatchRecord, MingCustomsPublicationBatchCommand>(
        "/classics/ming-customs/batch/publish",
        { body: command }
    );
};

export const submitOfflineBatch = (command: MingCustomsPublicationBatchCommand) => {
    return postJson<MingCustomsPublicationBatchRecord, MingCustomsPublicationBatchCommand>(
        "/classics/ming-customs/batch/offline",
        { body: command }
    );
};

export const listKeywordCloud = (visibility?: string | null) => {
    return postJson<MingCustomsKeywordCloudRecord[], MingCustomsCommand>(
        "/classics/ming-customs/keyword-cloud/list",
        {
            body: { visibility }
        }
    );
};

export const listTagCloud = (query: MingCustomsTagCloudQuery = {}) => {
    return postJson<MingCustomsTagCloudRecord[], MingCustomsTagCloudQuery>(
        "/classics/ming-customs/tag-cloud/list",
        {
            body: query
        }
    );
};

export const listVersions = (entryId: string) => {
    return postJson<MingCustomsContentVersionRecord[], MingCustomsVersionCommand>(
        "/classics/ming-customs/versions/list",
        {
            body: { id: entryId }
        }
    );
};

export const getVersion = (entryId: string, versionId: string) => {
    return postJson<MingCustomsContentVersionRecord, MingCustomsVersionCommand>(
        "/classics/ming-customs/versions/get",
        {
            body: {
                id: entryId,
                versionId
            }
        }
    );
};

export const resetVersion = (entryId: string, versionId: string) => {
    return postJson<MingCustomsContentVersionRecord, MingCustomsVersionCommand>(
        "/classics/ming-customs/versions/reset",
        {
            body: {
                id: entryId,
                versionId
            }
        }
    );
};

export const listCategoryOptions = async () => {
    const pageResult = await postJson<{ records: DictItem[] }, PageQuery<{ type: string }>>(
        "/sys/dict/page",
        {
            body: {
                pageNo: 1,
                pageSize: 100,
                type: CATEGORY_DICT_TYPE
            }
        }
    );
    return pageResult.records.map((item) => ({
        type: item.type,
        value: item.value,
        label: item.label
    }));
};
