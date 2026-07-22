import type { ReactNode } from "react";
import { Badge, Select } from "antd";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import { MingCustomsTagCloud } from "./ming-customs-keyword-cloud";
import type { MingCustomsQuery } from "../ming-customs-service";
import type { MingCustomsRecord, MingCustomsTagCloudItem } from "../ming-customs-types";

export type MingCustomsVisibilityFilter = "ALL" | "PUBLIC" | "PRIVATE";
export type MingCustomsSortDirectionFilter = "ASC" | "DESC";

export interface MingCustomsFilters {
    category: string;
    sortDirection: MingCustomsSortDirectionFilter;
    visibility: MingCustomsVisibilityFilter;
}

export interface MingCustomsSelectedTagFilter {
    count?: number | null;
    tagId?: number | null;
    tagNameSnapshot: string;
}

export interface MingCustomsToolbarProps {
    categoryOptions: Array<{ label: string; value: string }>;
    content: ReactNode;
    filterActive: boolean;
    filters: MingCustomsFilters;
    onAdd: () => void;
    onClearTagFilter: () => void;
    onFilterApply: () => void;
    onFilterReset: () => void;
    onFiltersChange: (filters: MingCustomsFilters) => void;
    onSearchChange: (value: string) => void;
    onSelectTag: (item: MingCustomsTagCloudItem) => void;
    query: MingCustomsQuery;
    searchValue: string;
    selectedTagFilter: MingCustomsSelectedTagFilter | null;
}

export const MingCustomsToolbar = ({
    categoryOptions,
    content,
    filterActive,
    filters,
    onAdd,
    onClearTagFilter,
    onFilterApply,
    onFilterReset,
    onFiltersChange,
    onSearchChange,
    onSelectTag,
    query,
    searchValue,
    selectedTagFilter
}: MingCustomsToolbarProps) => {
    return (
        <KuzhambuListPage<MingCustomsRecord>
            pageClassName="ming-customs-page"
            title="明代习俗"
            description="明代习俗专题条目治理入口。"
            subjectName="明代习俗"
            enableSearch
            enableFilter
            enableAdd
            addText="新增明代习俗"
            filterActive={filterActive}
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
                                onFiltersChange({
                                    ...filters,
                                    category: value || ""
                                })
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
                                onFiltersChange({
                                    ...filters,
                                    visibility: value
                                })
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
                                onFiltersChange({
                                    ...filters,
                                    sortDirection: value
                                })
                            }
                        />
                    )
                }
            ]}
            onFilterApply={onFilterApply}
            onFilterReset={onFilterReset}
            onAdd={onAdd}
            pageActions={
                <MingCustomsTagCloud
                    category={query.category}
                    keyword={query.keyword}
                    visibility={query.visibility}
                    onSelect={onSelectTag}
                />
            }
            searchValue={searchValue}
            onSearchChange={onSearchChange}
            content={
                <>
                    {selectedTagFilter ? (
                        <KuzhambuAlert
                            showIcon
                            type="info"
                            style={{ marginBottom: 12 }}
                            title={
                                <span>
                                    当前标签筛选：{selectedTagFilter.tagNameSnapshot}
                                    <Badge
                                        count={selectedTagFilter.count ?? 0}
                                        color="var(--ming-customs-accent-color)"
                                        style={{ marginLeft: 8 }}
                                    />
                                </span>
                            }
                            action={
                                <KuzhambuButton
                                    testId="classics-ming-customs-ming-customs-action-button"
                                    size="small"
                                    onClick={onClearTagFilter}
                                >
                                    清除标签筛选
                                </KuzhambuButton>
                            }
                        />
                    ) : null}
                    {content}
                </>
            }
        />
    );
};
