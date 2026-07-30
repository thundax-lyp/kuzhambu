import type {
    DiscoverySearchClickEventRequest,
    DiscoverySearchGroupResponse,
    DiscoverySearchItemResponse,
    DiscoverySearchRequest
} from "./search-types";

export const KNOWLEDGE_BASE_OPTIONS = [
    {
        label: "三才图会",
        value: "SANCAI_ENTRY"
    },
    {
        label: "王圻文档",
        value: "WANGQI_DOCUMENT"
    },
    {
        label: "明代习俗",
        value: "MING_CUSTOMS"
    }
] as const;

export const SAMPLE_QUERIES = [
    {
        knowledgeBases: "SANCAI_ENTRY",
        label: "三才图会",
        queryText: "图谱里的礼器"
    },
    {
        knowledgeBases: "WANGQI_DOCUMENT",
        label: "王圻文档",
        queryText: "明代官制"
    },
    {
        knowledgeBases: "MING_CUSTOMS",
        label: "明代习俗",
        queryText: "节令"
    }
] as const;

export const DEFAULT_PAGE_SIZE = "10";

export interface SearchFormState {
    categoryCodes: string;
    contentStatuses: string;
    dateFrom: string;
    dateTo: string;
    knowledgeBases: string;
    pageNo: string;
    pageSize: string;
    queryText: string;
    tagNames: string;
    visibilityScopes: string;
}

export const INITIAL_FORM_STATE: SearchFormState = {
    categoryCodes: "",
    contentStatuses: "",
    dateFrom: "",
    dateTo: "",
    knowledgeBases: "",
    pageNo: "1",
    pageSize: DEFAULT_PAGE_SIZE,
    queryText: "",
    tagNames: "",
    visibilityScopes: ""
};

export const hasAdvancedFilters = (form: SearchFormState) => {
    return Boolean(form.dateFrom || form.dateTo || form.knowledgeBases);
};

export const toFormState = (searchParams: URLSearchParams): SearchFormState => {
    return {
        categoryCodes: "",
        contentStatuses: "",
        dateFrom: searchParams.get("dateFrom") ?? "",
        dateTo: searchParams.get("dateTo") ?? "",
        knowledgeBases: searchParams.get("knowledgeBases") ?? "",
        pageNo: searchParams.get("pageNo") ?? INITIAL_FORM_STATE.pageNo,
        pageSize: searchParams.get("pageSize") ?? INITIAL_FORM_STATE.pageSize,
        queryText: searchParams.get("q") ?? "",
        tagNames: "",
        visibilityScopes: ""
    };
};

export const splitList = (value: string) => {
    const tokens = value
        .split(/[\n,，、\s]+/gu)
        .map((token) => token.trim())
        .filter(Boolean);

    return Array.from(new Set(tokens));
};

export const joinList = (values: string[]) => values.join(", ");

export const hasListValue = (value: string, token: string) => splitList(value).includes(token);

export const toSearchParams = (form: SearchFormState) => {
    const searchParams = new URLSearchParams();
    appendParam(searchParams, "q", form.queryText);
    appendParam(searchParams, "knowledgeBases", form.knowledgeBases);
    appendParam(searchParams, "dateFrom", form.dateFrom);
    appendParam(searchParams, "dateTo", form.dateTo);
    if (form.pageNo !== INITIAL_FORM_STATE.pageNo) {
        appendParam(searchParams, "pageNo", form.pageNo);
    }
    if (form.pageSize !== INITIAL_FORM_STATE.pageSize) {
        appendParam(searchParams, "pageSize", form.pageSize);
    }

    return searchParams;
};

export const flattenGroups = (groups: DiscoverySearchGroupResponse[]) => {
    return groups.flatMap((group, groupIndex) =>
        (group.items ?? []).map((item, itemIndex) => ({
            group,
            groupIndex,
            item,
            itemIndex
        }))
    );
};

export const toRequest = (form: SearchFormState): DiscoverySearchRequest => {
    return {
        categoryCodes: splitList(form.categoryCodes),
        contentStatuses: splitList(form.contentStatuses),
        dateFrom: toIsoStartOfDay(form.dateFrom),
        dateTo: toIsoEndOfDay(form.dateTo),
        knowledgeBases: splitList(form.knowledgeBases),
        pageNo: Number.parseInt(form.pageNo, 10) || 1,
        pageSize: Number.parseInt(form.pageSize, 10) || 10,
        queryText: form.queryText.trim(),
        tagNames: splitList(form.tagNames),
        visibilityScopes: splitList(form.visibilityScopes)
    };
};

export const createClickCommand = (
    searchEventId: string | number | null | undefined,
    group: DiscoverySearchGroupResponse,
    item: DiscoverySearchItemResponse
): DiscoverySearchClickEventRequest | null => {
    const normalizedSearchEventId = normalizeNumericId(searchEventId);
    if (
        !normalizedSearchEventId ||
        !group.groupKey ||
        !item.contentDomain ||
        !item.contentType ||
        !item.contentId ||
        item.resultRank == null ||
        item.groupRank == null
    ) {
        return null;
    }

    return {
        contentDomain: item.contentDomain,
        contentId: item.contentId,
        contentTitle: item.title ?? null,
        contentType: item.contentType,
        groupRank: item.groupRank,
        resultGroupKey: group.groupKey,
        resultRank: item.resultRank,
        searchEventId: normalizedSearchEventId,
        targetPath: item.targetPath ?? null
    };
};

export const formatCount = (value?: number | null) => {
    return value ?? 0;
};

const appendParam = (searchParams: URLSearchParams, key: string, value: string) => {
    const normalizedValue = value.trim();
    if (normalizedValue) {
        searchParams.set(key, normalizedValue);
    }
};

const toIsoStartOfDay = (value: string) => {
    return value ? new Date(`${value}T00:00:00`).toISOString() : null;
};

const toIsoEndOfDay = (value: string) => {
    return value ? new Date(`${value}T23:59:59`).toISOString() : null;
};

const normalizeNumericId = (value: string | number | null | undefined) => {
    if (typeof value === "number") {
        return Number.isSafeInteger(value) && value > 0 ? String(value) : null;
    }

    if (typeof value !== "string") {
        return null;
    }

    const trimmedValue = value.trim();

    return /^\d+$/u.test(trimmedValue) ? trimmedValue : null;
};
