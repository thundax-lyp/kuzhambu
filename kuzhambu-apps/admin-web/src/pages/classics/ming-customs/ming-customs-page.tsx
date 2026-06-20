import { useQuery } from "@tanstack/react-query";
import { Select } from "antd";
import { useMemo, useState } from "react";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { MingCustomsKeywordCloud } from "./components/ming-customs-keyword-cloud";
import { MingCustomsList } from "./components/ming-customs-list";
import * as service from "./ming-customs-service";
import type { MingCustomsQuery } from "./ming-customs-service";
import type { MingCustomsRecord } from "./ming-customs-types";
import "./ming-customs-page.css";

type MingCustomsVisibilityFilter = "ALL" | "PUBLIC" | "PRIVATE";
type MingCustomsSortDirectionFilter = "ASC" | "DESC";

interface MingCustomsFilters {
    category: string;
    sortDirection: MingCustomsSortDirectionFilter;
    visibility: MingCustomsVisibilityFilter;
}

const DEFAULT_MING_CUSTOMS_FILTERS: MingCustomsFilters = {
    category: "",
    sortDirection: "DESC",
    visibility: "ALL"
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readVisibilityValue = (visibility: MingCustomsVisibilityFilter) => {
    return visibility === "ALL" ? undefined : visibility;
};

export const MingCustomsPage = () => {
    const [query, setQuery] = useState<MingCustomsQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE,
        sortDirection: DEFAULT_MING_CUSTOMS_FILTERS.sortDirection
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<MingCustomsFilters>(DEFAULT_MING_CUSTOMS_FILTERS);
    const hasActiveFilters = Boolean(
        filters.category ||
        filters.visibility !== "ALL" ||
        filters.sortDirection !== DEFAULT_MING_CUSTOMS_FILTERS.sortDirection
    );

    const mingCustomsQuery = useQuery({
        queryKey: ["ming-customs", "page", query],
        queryFn: () => service.page(query),
        retry: false
    });
    const categoryOptionsQuery = useQuery({
        queryKey: ["ming-customs", "category-options"],
        queryFn: service.listCategoryOptions,
        retry: false
    });
    const keywordCloudQuery = useQuery({
        queryKey: ["ming-customs", "keyword-cloud", query.visibility],
        queryFn: () => service.listKeywordCloud(query.visibility),
        retry: false
    });
    const pageResult = mingCustomsQuery.data;
    const records = useMemo(() => pageResult?.records || [], [pageResult?.records]);
    const categoryOptions = useMemo(
        () => categoryOptionsQuery.data || [],
        [categoryOptionsQuery.data]
    );
    const categoryLabels = useMemo(() => {
        return Object.fromEntries(categoryOptions.map((option) => [option.value, option.label]));
    }, [categoryOptions]);
    const totalCount = pageResult?.count ?? pageResult?.totalCount ?? 0;
    const currentPageNo = pageResult?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = pageResult?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const searchMingCustoms = (value: string) => {
        setSearchText(value);
        setQuery((currentQuery) => ({
            ...currentQuery,
            keyword: normalizeSearch(value),
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const applyFilters = () => {
        setQuery((currentQuery) => ({
            ...currentQuery,
            category: filters.category || undefined,
            visibility: readVisibilityValue(filters.visibility),
            sortDirection: filters.sortDirection,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const resetFilters = () => {
        setFilters(DEFAULT_MING_CUSTOMS_FILTERS);
        setQuery((currentQuery) => ({
            ...currentQuery,
            category: undefined,
            visibility: undefined,
            sortDirection: DEFAULT_MING_CUSTOMS_FILTERS.sortDirection,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    const selectKeyword = (keyword: string) => {
        setSearchText(keyword);
        setQuery((currentQuery) => ({
            ...currentQuery,
            keyword,
            pageNo: DEFAULT_PAGE_NO
        }));
    };

    return (
        <KuzhambuListPage<MingCustomsRecord>
            pageClassName="ming-customs-page"
            title="明代习俗"
            description="明代习俗专题条目治理入口。"
            subjectName="明代习俗"
            enableSearch
            enableFilter
            filterActive={hasActiveFilters}
            filterFields={[
                {
                    name: "category",
                    label: "分类",
                    render: () => (
                        <Select
                            allowClear
                            aria-label="明代习俗分类"
                            placeholder="全部分类"
                            value={filters.category || undefined}
                            options={categoryOptions.map((option) => ({
                                value: option.value,
                                label: option.label
                            }))}
                            onChange={(value) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    category: value || ""
                                }))
                            }
                        />
                    )
                },
                {
                    name: "visibility",
                    label: "可见性",
                    render: () => (
                        <Select
                            aria-label="明代习俗可见性"
                            value={filters.visibility}
                            options={[
                                { value: "ALL", label: "全部" },
                                { value: "PUBLIC", label: "公开" },
                                { value: "PRIVATE", label: "私有" }
                            ]}
                            onChange={(value) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    visibility: value
                                }))
                            }
                        />
                    )
                },
                {
                    name: "sortDirection",
                    label: "排序",
                    render: () => (
                        <Select
                            aria-label="明代习俗排序方向"
                            value={filters.sortDirection}
                            options={[
                                { value: "DESC", label: "最新优先" },
                                { value: "ASC", label: "最早优先" }
                            ]}
                            onChange={(value) =>
                                setFilters((currentFilters) => ({
                                    ...currentFilters,
                                    sortDirection: value
                                }))
                            }
                        />
                    )
                }
            ]}
            onFilterApply={applyFilters}
            onFilterReset={resetFilters}
            searchValue={searchText}
            onSearchChange={searchMingCustoms}
            content={
                <MingCustomsList
                    categoryLabels={categoryLabels}
                    loading={mingCustomsQuery.isLoading}
                    dataSource={records}
                    pagination={{
                        current: currentPageNo,
                        pageSize: currentPageSize,
                        total: totalCount,
                        onChange: (pageNo, pageSize) =>
                            setQuery((currentQuery) => ({
                                ...currentQuery,
                                pageNo,
                                pageSize
                            }))
                    }}
                />
            }
            tableAside={
                <MingCustomsKeywordCloud
                    loading={keywordCloudQuery.isLoading}
                    items={keywordCloudQuery.data || []}
                    onSelect={selectKeyword}
                />
            }
            tableAsidePlacement="right"
        />
    );
};
