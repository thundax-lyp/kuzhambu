import { useMutation } from "@tanstack/react-query";
import type { Dayjs } from "dayjs";
import { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { SearchQueryPanel, type SearchFormState } from "./search-query-panel";
import { SearchResultDetail } from "./search-result-detail";
import { SearchResultTable, type SearchResultEntry } from "./search-result-table";
import * as service from "./search-service";
import type { DiscoverySearchGroupRecord, DiscoverySearchItemRecord } from "./search-types";
import type {
    DiscoverySearchClickEventCommand,
    DiscoverySearchPreviewQuery,
    DiscoverySearchQuery
} from "./search-service";
import "./search-page.css";

const DEFAULT_PAGE_NO = "1";
const DEFAULT_PAGE_SIZE = "10";

const KNOWLEDGE_BASE_OPTIONS = [
    { label: "三才图会", value: "SANCAI_ENTRY" },
    { label: "王圻文档", value: "WANGQI_DOCUMENT" },
    { label: "明代习俗", value: "MING_CUSTOMS" }
];

const toKnowledgeBaseLabel = (value?: string | null) => {
    return KNOWLEDGE_BASE_OPTIONS.find((option) => option.value === value)?.label || value || "-";
};

const INITIAL_FORM_STATE: SearchFormState = {
    dateFrom: "",
    dateTo: "",
    knowledgeBases: [],
    pageNo: DEFAULT_PAGE_NO,
    pageSize: DEFAULT_PAGE_SIZE,
    queryText: ""
};

const splitList = (value: string) => {
    const tokens = value
        .split(/[\n,，、\s]+/gu)
        .map((token) => token.trim())
        .filter(Boolean);

    return Array.from(new Set(tokens));
};

const appendParam = (searchParams: URLSearchParams, key: string, value: string) => {
    const normalizedValue = value.trim();
    if (normalizedValue) {
        searchParams.set(key, normalizedValue);
    }
};

const appendListParam = (searchParams: URLSearchParams, key: string, values: string[]) => {
    if (values.length) {
        searchParams.set(key, values.join(","));
    }
};

const toFormState = (searchParams: URLSearchParams): SearchFormState => ({
    dateFrom: searchParams.get("dateFrom") ?? "",
    dateTo: searchParams.get("dateTo") ?? "",
    knowledgeBases: splitList(searchParams.get("knowledgeBases") ?? ""),
    pageNo: searchParams.get("pageNo") ?? DEFAULT_PAGE_NO,
    pageSize: searchParams.get("pageSize") ?? DEFAULT_PAGE_SIZE,
    queryText: searchParams.get("q") ?? ""
});

const toSearchParams = (form: SearchFormState) => {
    const searchParams = new URLSearchParams();
    appendParam(searchParams, "q", form.queryText);
    appendListParam(searchParams, "knowledgeBases", form.knowledgeBases);
    appendParam(searchParams, "dateFrom", form.dateFrom);
    appendParam(searchParams, "dateTo", form.dateTo);
    return searchParams;
};

const toIsoStartOfDay = (value: string) => {
    return value ? new Date(`${value}T00:00:00`).toISOString() : null;
};

const toIsoEndOfDay = (value: string) => {
    return value ? new Date(`${value}T23:59:59`).toISOString() : null;
};

const toRequest = (form: SearchFormState): DiscoverySearchQuery => ({
    categoryCodes: [],
    dateFrom: toIsoStartOfDay(form.dateFrom),
    dateTo: toIsoEndOfDay(form.dateTo),
    knowledgeBases: form.knowledgeBases,
    pageNo: Number.parseInt(form.pageNo, 10) || 1,
    pageSize: Number.parseInt(form.pageSize, 10) || 10,
    queryText: form.queryText.trim(),
    tagNames: []
});

const renderHighlightText = (highlightText?: string | null) => {
    if (!highlightText) {
        return null;
    }

    const nodes: Array<string | JSX.Element> = [];
    const markPattern = /<mark>(.*?)<\/mark>/giu;
    let lastIndex = 0;
    let match = markPattern.exec(highlightText);

    while (match) {
        if (match.index > lastIndex) {
            nodes.push(highlightText.slice(lastIndex, match.index));
        }
        nodes.push(<mark key={`mark-${match.index}`}>{match[1]}</mark>);
        lastIndex = match.index + match[0].length;
        match = markPattern.exec(highlightText);
    }

    if (lastIndex < highlightText.length) {
        nodes.push(highlightText.slice(lastIndex));
    }

    return nodes;
};

const escapeRegExp = (value: string) => {
    return value.replace(/[.*+?^${}()|[\]\\]/gu, "\\$&");
};

const renderQueryHighlight = (text: string, queryText: string) => {
    const terms = splitList(queryText).filter((term) => term.length > 0);
    if (terms.length === 0) {
        return text;
    }

    const pattern = new RegExp(`(${terms.map(escapeRegExp).join("|")})`, "giu");
    const nodes: Array<string | JSX.Element> = [];
    let lastIndex = 0;
    let match = pattern.exec(text);

    while (match) {
        if (match.index > lastIndex) {
            nodes.push(text.slice(lastIndex, match.index));
        }
        nodes.push(<mark key={`query-mark-${match.index}`}>{match[0]}</mark>);
        lastIndex = match.index + match[0].length;
        match = pattern.exec(text);
    }

    if (lastIndex < text.length) {
        nodes.push(text.slice(lastIndex));
    }

    return nodes.length > 0 ? nodes : text;
};

const toPlainHighlightText = (value?: string | null) => {
    return (value ?? "").replace(/<mark>(.*?)<\/mark>/giu, "$1").trim();
};

const createClickCommand = (
    searchEventId: string,
    group: DiscoverySearchGroupRecord,
    item: DiscoverySearchItemRecord
): DiscoverySearchClickEventCommand | null => {
    if (
        !searchEventId ||
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
        searchEventId,
        targetPath: item.targetPath ?? null
    };
};

const createPreviewQuery = (
    item: DiscoverySearchItemRecord
): DiscoverySearchPreviewQuery | null => {
    if (!item.contentType || !item.contentId) {
        return null;
    }

    return {
        contentId: item.contentId,
        contentType: item.contentType
    };
};

export const SearchPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [form, setForm] = useState<SearchFormState>(() => toFormState(searchParams));
    const [previewResult, setPreviewResult] = useState<SearchResultEntry | null>(null);
    const submittedFormRef = useRef<SearchFormState | null>(null);
    const searchMutation = useMutation({
        mutationFn: service.searchDiscovery
    });
    const searchResultPreviewMutation = useMutation({
        mutationFn: service.previewSearchResult
    });
    const { mutate: runSearch } = searchMutation;

    useEffect(() => {
        const submittedForm = submittedFormRef.current;
        submittedFormRef.current = null;

        const nextForm = submittedForm ?? toFormState(searchParams);
        if (!submittedForm) {
            setForm(nextForm);
        }
        const request = toRequest(nextForm);
        runSearch(request);
    }, [runSearch, searchParams]);

    const response = searchMutation.data;
    const results = useMemo<SearchResultEntry[]>(() => {
        return (response?.groups ?? []).flatMap((group, groupIndex) => {
            const groupKey = group.groupKey || `group-${groupIndex}`;
            return (group.items ?? []).map((item, itemIndex) => ({
                group,
                item,
                key: `${groupKey}-${item.resultRank ?? itemIndex}`
            }));
        });
    }, [response?.groups]);
    const hasActiveFilters =
        form.knowledgeBases.length > 0 || form.dateFrom.trim() !== "" || form.dateTo.trim() !== "";

    const updateField = (key: keyof SearchFormState, value: string | string[]) => {
        setForm((current) => ({
            ...current,
            [key]: value
        }));
    };
    const updateDateRange = (dates: null | [Dayjs | null, Dayjs | null]) => {
        setForm((current) => ({
            ...current,
            dateFrom: dates?.[0]?.format("YYYY-MM-DD") ?? "",
            dateTo: dates?.[1]?.format("YYYY-MM-DD") ?? ""
        }));
    };

    const submitForm = (nextForm: SearchFormState) => {
        setForm(nextForm);
        const request = toRequest(nextForm);

        const nextSearchParams = toSearchParams(nextForm);
        if (nextSearchParams.toString() === searchParams.toString()) {
            runSearch(request);
            return;
        }

        submittedFormRef.current = nextForm;
        setSearchParams(nextSearchParams);
    };

    const submitSearch = () => {
        submitForm({
            ...form,
            pageNo: DEFAULT_PAGE_NO
        });
    };

    const clearFilters = () => {
        const nextForm: SearchFormState = {
            ...INITIAL_FORM_STATE,
            queryText: form.queryText
        };
        submitForm(nextForm);
    };

    const changePage = (pageNo?: number, pageSize?: number) => {
        submitForm({
            ...form,
            pageNo: String(pageNo || 1),
            pageSize: String(pageSize || Number.parseInt(form.pageSize, 10) || 10)
        });
    };

    const recordClick = (group: DiscoverySearchGroupRecord, item: DiscoverySearchItemRecord) => {
        const command = response?.id ? createClickCommand(response.id, group, item) : null;
        if (command) {
            void service.clickSearchResult(command);
        }
    };

    const openSearchResultPreview = (result: SearchResultEntry) => {
        recordClick(result.group, result.item);
        setPreviewResult(result);
        const previewQuery = createPreviewQuery(result.item);
        if (previewQuery) {
            searchResultPreviewMutation.mutate(previewQuery);
            return;
        }
        searchResultPreviewMutation.reset();
    };

    const closeSearchResultPreview = () => {
        setPreviewResult(null);
        searchResultPreviewMutation.reset();
    };

    const shouldShowZeroResult =
        !searchMutation.isPending && !searchMutation.isError && response?.totalCount === 0;
    const currentPageNo = Number.parseInt(form.pageNo, 10) || 1;
    const currentPageSize = Number.parseInt(form.pageSize, 10) || 10;
    const totalCount = response?.totalCount ?? results.length;
    const resultContent = (
        <SearchResultTable
            currentPageNo={currentPageNo}
            currentPageSize={currentPageSize}
            isError={searchMutation.isError}
            isPending={searchMutation.isPending}
            queryText={form.queryText}
            results={results}
            shouldShowZeroResult={shouldShowZeroResult}
            totalCount={totalCount}
            onChangePage={changePage}
            onOpenPreview={openSearchResultPreview}
            renderHighlightText={renderHighlightText}
            renderQueryHighlight={renderQueryHighlight}
            toPlainHighlightText={toPlainHighlightText}
        />
    );
    const previewData = searchResultPreviewMutation.data;
    const previewErrorMessage =
        searchResultPreviewMutation.error instanceof Error
            ? searchResultPreviewMutation.error.message
            : null;

    return (
        <div className="search-page-root">
            <SearchQueryPanel
                content={resultContent}
                filterActive={hasActiveFilters}
                form={form}
                knowledgeBaseOptions={KNOWLEDGE_BASE_OPTIONS}
                loading={searchMutation.isPending}
                onClearFilters={clearFilters}
                onSearch={submitSearch}
                onUpdateDateRange={updateDateRange}
                onUpdateField={updateField}
            />
            <SearchResultDetail
                errorMessage={previewErrorMessage}
                loading={searchResultPreviewMutation.isPending}
                open={Boolean(previewResult)}
                previewData={previewData}
                previewResult={previewResult}
                toKnowledgeBaseLabel={toKnowledgeBaseLabel}
                onClose={closeSearchResultPreview}
            />
        </div>
    );
};
