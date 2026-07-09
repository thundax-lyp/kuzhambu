import { getJson, postJson } from "@/api/http";
import type { DictItem } from "@/types/dict";
import type { Page, PageQuery } from "@/types/page";
import type {
    MingCustomsContentVersionRecord,
    MingCustomsKeywordCloudItem,
    MingCustomsRecord,
    MingCustomsTagCloudItem
} from "./ming-customs-types";

const CATEGORY_DICT_TYPE = "CLASSICS_MING_CUSTOMS_CATEGORY";

export type MingCustomsQuery = PageQuery<{
    keyword?: string | null;
    category?: string | null;
    visibility?: string | null;
    tagName?: string | null;
    tagId?: number | null;
    tagNameSnapshot?: string | null;
    sortDirection?: "ASC" | "DESC" | null;
}>;

export interface MingCustomsCommand {
    id?: number | null;
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
    id: number;
    versionId?: number | null;
}

export const page = (request: MingCustomsQuery = {}) => {
    return postJson<Page<MingCustomsRecord>, MingCustomsQuery>("/classics/ming-customs/page", {
        body: request
    });
};

export const get = (id: number) => {
    return getJson<MingCustomsRecord>(`/classics/ming-customs/${id}`);
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

export const deleteById = (id: number) => {
    return postJson<void, MingCustomsCommand>("/classics/ming-customs/delete", {
        body: { id }
    });
};

export const listKeywordCloud = (visibility?: string | null) => {
    const searchParams = new URLSearchParams();
    if (visibility) {
        searchParams.set("visibility", visibility);
    }
    const queryString = searchParams.toString();
    const path = queryString
        ? `/classics/ming-customs/keyword-cloud?${queryString}`
        : "/classics/ming-customs/keyword-cloud";
    return getJson<MingCustomsKeywordCloudItem[]>(path);
};

export const listTagCloud = (query: MingCustomsTagCloudQuery = {}) => {
    const searchParams = new URLSearchParams();
    if (query.category) {
        searchParams.set("category", query.category);
    }
    if (query.keyword) {
        searchParams.set("keyword", query.keyword);
    }
    if (query.visibility) {
        searchParams.set("visibility", query.visibility);
    }
    const queryString = searchParams.toString();
    const path = queryString
        ? `/classics/ming-customs/tag-cloud?${queryString}`
        : "/classics/ming-customs/tag-cloud";
    return getJson<MingCustomsTagCloudItem[]>(path);
};

export const listVersions = (entryId: number) => {
    return postJson<MingCustomsContentVersionRecord[], MingCustomsVersionCommand>(
        "/classics/ming-customs/versions/list",
        {
            body: { id: entryId }
        }
    );
};

export const getVersion = (entryId: number, versionId: number) => {
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

export const resetVersion = (entryId: number, versionId: number) => {
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
