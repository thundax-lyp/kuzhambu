import { SearchOutlined } from "@ant-design/icons";
import { useMutation, useQuery } from "@tanstack/react-query";
import { DatePicker, Input } from "antd";
import dayjs from "dayjs";
import type { Dayjs } from "dayjs";
import { useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuSelect,
    type KuzhambuListPageFilterField
} from "@/components";
import { SearchResultDetail } from "./search-result-detail";
import { SearchResultTable, type SearchResultEntry } from "./search-result-table";
import * as service from "./search-service";
import type { DiscoverySearchGroupRecord, DiscoverySearchItemRecord } from "./search-types";
import type { DiscoverySearchClickEventCommand, DiscoverySearchQuery } from "./search-service";
import "./search-page.css";

const DEFAULT_PAGE_NO = "1";
const DEFAULT_PAGE_SIZE = "10";

const KNOWLEDGE_BASE_OPTIONS = [
    { label: "三才图会", value: "SANCAI_ENTRY" },
    { label: "王圻文档", value: "WANGQI_DOCUMENT" },
    { label: "明代习俗", value: "MING_CUSTOMS" }
];

interface SearchFormState {
    dateFrom: string;
    dateTo: string;
    knowledgeBases: string[];
    pageNo: string;
    pageSize: string;
    queryText: string;
}

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
    appendParam(searchParams, "pageNo", form.pageNo);
    appendParam(searchParams, "pageSize", form.pageSize);
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

interface SearchWorkspaceProps {
    initialForm: SearchFormState;
    searchParamsValue: string;
    onCommitSearchParams: (searchParams: URLSearchParams) => void;
}

const SearchWorkspace = ({
    initialForm,
    searchParamsValue,
    onCommitSearchParams
}: SearchWorkspaceProps) => {
    const [form, setForm] = useState<SearchFormState>(initialForm);
    const [previewResult, setPreviewResult] = useState<SearchResultEntry | null>(null);
    const request = useMemo(() => toRequest(initialForm), [initialForm]);
    const searchQuery = useQuery({
        queryFn: () => service.searchDiscovery(request),
        queryKey: ["discovery-search", "results", request]
    });
    const clickMutation = useMutation({
        mutationFn: service.clickSearchResult
    });
    const response = searchQuery.data;
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
        setForm((current) => ({ ...current, [key]: value }));
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
        const nextSearchParams = toSearchParams(nextForm);
        if (nextSearchParams.toString() === searchParamsValue) {
            void searchQuery.refetch();
            return;
        }
        onCommitSearchParams(nextSearchParams);
    };
    const submitSearch = () => submitForm({ ...form, pageNo: DEFAULT_PAGE_NO });
    const clearFilters = () => submitForm({ ...INITIAL_FORM_STATE, queryText: form.queryText });
    const changePage = (pageNo?: number, pageSize?: number) => {
        submitForm({
            ...form,
            pageNo: String(pageNo || 1),
            pageSize: String(pageSize || Number.parseInt(form.pageSize, 10) || 10)
        });
    };
    const openSearchResultPreview = (result: SearchResultEntry) => {
        const command = response?.id
            ? createClickCommand(response.id, result.group, result.item)
            : null;
        if (command) {
            clickMutation.mutate(command);
        }
        setPreviewResult(result);
    };

    const filterFields: KuzhambuListPageFilterField[] = [
        {
            name: "queryText",
            label: "搜索词",
            render: () => (
                <Input
                    placeholder="输入古籍、实体或正文关键词"
                    value={form.queryText}
                    onChange={(event) => updateField("queryText", event.target.value)}
                />
            )
        },
        {
            name: "knowledgeBases",
            label: "知识库",
            render: () => (
                <KuzhambuSelect
                    mode="multiple"
                    allowClear
                    options={KNOWLEDGE_BASE_OPTIONS}
                    placeholder="全部知识库"
                    value={form.knowledgeBases}
                    onChange={(value) => updateField("knowledgeBases", value)}
                />
            )
        },
        {
            name: "dateRange",
            label: "时间范围",
            render: () => (
                <DatePicker.RangePicker
                    value={[
                        form.dateFrom ? dayjs(form.dateFrom) : null,
                        form.dateTo ? dayjs(form.dateTo) : null
                    ]}
                    onChange={updateDateRange}
                />
            )
        }
    ];

    return (
        <div className="search-page-root">
            <KuzhambuListPage
                pageClassName="search-page"
                title="检索"
                description="公开已发布内容。"
                subjectName="内容"
                enableFilter
                filterText="高级"
                enableSearch
                searchShortcut="⌘K"
                searchValue={form.queryText}
                searchPlaceholder="搜索公开已发布内容..."
                onSearchChange={(queryText) => updateField("queryText", queryText)}
                filterActive={hasActiveFilters}
                filterFields={filterFields}
                onFilterApply={submitSearch}
                onFilterReset={clearFilters}
                pageActions={
                    <KuzhambuButton
                        ariaLabel="搜索"
                        icon={<SearchOutlined />}
                        loading={searchQuery.isFetching}
                        testId="discovery-search-submit-button"
                        type="primary"
                        onClick={submitSearch}
                    >
                        搜索
                    </KuzhambuButton>
                }
                content={
                    <SearchResultTable
                        currentPageNo={Number.parseInt(initialForm.pageNo, 10) || 1}
                        currentPageSize={Number.parseInt(initialForm.pageSize, 10) || 10}
                        isError={searchQuery.isError}
                        isPending={searchQuery.isFetching}
                        queryText={initialForm.queryText}
                        results={results}
                        shouldShowZeroResult={
                            !searchQuery.isFetching &&
                            !searchQuery.isError &&
                            response?.totalCount === 0
                        }
                        totalCount={response?.totalCount ?? results.length}
                        onChangePage={changePage}
                        onOpenPreview={openSearchResultPreview}
                    />
                }
            />
            <SearchResultDetail
                previewResult={previewResult}
                onClose={() => setPreviewResult(null)}
            />
        </div>
    );
};

export const SearchPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const searchParamsValue = searchParams.toString();
    const initialForm = useMemo(() => toFormState(searchParams), [searchParams]);

    return (
        <SearchWorkspace
            key={searchParamsValue}
            initialForm={initialForm}
            searchParamsValue={searchParamsValue}
            onCommitSearchParams={setSearchParams}
        />
    );
};
