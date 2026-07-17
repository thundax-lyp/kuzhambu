import { useMutation } from "@tanstack/react-query";
import { SearchOutlined } from "@ant-design/icons";
import { DatePicker, Input, Select } from "antd";
import dayjs from "dayjs";
import type { Dayjs } from "dayjs";
import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import type { KuzhambuTableColumn } from "@/components/kuzhambu-table";
import * as service from "./search-service";
import type { DiscoverySearchGroupRecord, DiscoverySearchItemRecord } from "./search-types";
import type { DiscoverySearchClickCommand, DiscoverySearchQuery } from "./search-service";
import "./search-page.css";

const DEFAULT_PAGE_NO = "1";
const DEFAULT_PAGE_SIZE = "10";
const PUBLIC_VISIBILITY_SCOPE = "PUBLIC";
const PUBLISHED_CONTENT_STATUS = "PUBLISHED";

const KNOWLEDGE_BASE_OPTIONS = [
    { label: "三才图会", value: "SANCAI_ENTRY" },
    { label: "王圻文档", value: "WANGQI_DOCUMENT" },
    { label: "明代习俗", value: "MING_CUSTOMS" }
];

interface SearchResultRow {
    group: DiscoverySearchGroupRecord;
    item: DiscoverySearchItemRecord;
    key: string;
}

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
    contentStatuses: [PUBLISHED_CONTENT_STATUS],
    dateFrom: toIsoStartOfDay(form.dateFrom),
    dateTo: toIsoEndOfDay(form.dateTo),
    knowledgeBases: form.knowledgeBases,
    pageNo: Number.parseInt(form.pageNo, 10) || 1,
    pageSize: Number.parseInt(form.pageSize, 10) || 10,
    queryText: form.queryText.trim(),
    tagNames: [],
    visibilityScopes: [PUBLIC_VISIBILITY_SCOPE]
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

const createClickCommand = (
    searchLogId: string,
    group: DiscoverySearchGroupRecord,
    item: DiscoverySearchItemRecord
): DiscoverySearchClickCommand | null => {
    if (
        !searchLogId ||
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
        searchLogId,
        targetPath: item.targetPath ?? null
    };
};

export const SearchPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [form, setForm] = useState<SearchFormState>(() => toFormState(searchParams));
    const searchMutation = useMutation({
        mutationFn: service.searchDiscovery
    });
    const { mutate: runSearch } = searchMutation;

    useEffect(() => {
        const nextForm = toFormState(searchParams);
        const request = toRequest(nextForm);
        runSearch(request);
    }, [runSearch, searchParams]);

    const response = searchMutation.data;
    const rows = useMemo(() => {
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
        const command = response?.searchLogId
            ? createClickCommand(response.searchLogId, group, item)
            : null;
        if (command) {
            void service.clickSearchResult(command);
        }
    };

    const shouldShowZeroResult =
        !searchMutation.isPending && !searchMutation.isError && response?.totalCount === 0;
    const columns: KuzhambuTableColumn<SearchResultRow>[] = [
        {
            title: "分组",
            dataIndex: ["group", "groupTitle"],
            key: "group",
            width: 140,
            render: (_value, row) => row.group.groupTitle || row.group.groupKey || "-"
        },
        {
            title: "标题",
            dataIndex: ["item", "title"],
            key: "title",
            width: 220,
            render: (_value, row) => {
                const title = row.item.title || "未命名结果";
                if (!row.item.targetPath) {
                    return title;
                }

                return (
                    <Link to={row.item.targetPath} onClick={() => recordClick(row.group, row.item)}>
                        {title}
                    </Link>
                );
            }
        },
        {
            title: "摘要",
            dataIndex: ["item", "highlightText"],
            key: "summary",
            render: (_value, row) => (
                <span className="search-page-highlight">
                    {renderHighlightText(row.item.highlightText) || row.item.summary || "-"}
                </span>
            )
        },
        {
            title: "类型",
            dataIndex: ["item", "contentType"],
            key: "contentType",
            width: 140,
            render: (_value, row) => row.item.contentType || "-"
        },
        {
            title: "内容 ID",
            dataIndex: ["item", "contentId"],
            key: "contentId",
            width: 120,
            render: (_value, row) => row.item.contentId || "-"
        },
        {
            title: "排序",
            key: "rank",
            width: 120,
            render: (_value, row) => `${row.item.resultRank ?? "-"} / ${row.item.groupRank ?? "-"}`
        }
    ];

    return (
        <KuzhambuListPage<SearchResultRow>
            pageClassName="search-page"
            title="跨库搜索"
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
            filterFields={[
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
                        <Select
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
            ]}
            onFilterApply={submitSearch}
            onFilterReset={clearFilters}
            pageActions={
                <KuzhambuButton
                    ariaLabel="搜索"
                    icon={<SearchOutlined />}
                    loading={searchMutation.isPending}
                    testId="discovery-search-submit-button"
                    type="primary"
                    onClick={submitSearch}
                >
                    搜索
                </KuzhambuButton>
            }
            columns={columns}
            dataSource={rows}
            loading={searchMutation.isPending}
            pagination={{
                current: Number.parseInt(form.pageNo, 10) || 1,
                pageSize: Number.parseInt(form.pageSize, 10) || 10,
                showSizeChanger: true,
                total: response?.totalCount ?? rows.length
            }}
            rowKey="key"
            locale={{
                emptyText: shouldShowZeroResult ? "没有找到匹配内容" : "暂无搜索结果"
            }}
            onChange={(pagination) => changePage(pagination.current, pagination.pageSize)}
        />
    );
};
