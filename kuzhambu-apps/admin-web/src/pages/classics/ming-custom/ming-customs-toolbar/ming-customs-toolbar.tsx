import type { ReactNode } from "react";
import { ScheduleOutlined } from "@ant-design/icons";
import { Badge } from "antd";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuSelect,
    KuzhambuSpace
} from "@/components";

import { MingCustomsTagCloud } from "@/pages/classics/ming-custom/ming-customs-keyword-cloud";
import type { MingCustomsQuery } from "@/pages/classics/ming-custom/ming-custom-service";
import type {
    MingCustomsRecord,
    MingCustomsTagCloudItem
} from "@/pages/classics/ming-custom/ming-custom-types";

export type MingCustomsSortDirectionFilter = "ASC" | "DESC";

export interface MingCustomsFilters {
    category: string;
    sortDirection: MingCustomsSortDirectionFilter;
}

export interface MingCustomsSelectedTagFilter {
    count?: number | null;
    tagId?: string | null;
    tagNameSnapshot: string;
}

interface MingCustomsToolbarProps {
    categoryOptions: Array<{ label: string; value: string }>;
    content: ReactNode;
    filterActive: boolean;
    filters: MingCustomsFilters;
    onAdd: () => void;
    onClearTagFilter: () => void;
    onFilterApply: () => void;
    onFilterReset: () => void;
    onFiltersChange: (filters: MingCustomsFilters) => void;
    onOpenExportJobs: () => void;
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
    onOpenExportJobs,
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
                        <KuzhambuSelect
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
                    name: "sortDirection",
                    label: "排序",
                    render: () => (
                        <KuzhambuSelect
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
                <KuzhambuSpace>
                    <MingCustomsTagCloud
                        category={query.category}
                        keyword={query.keyword}
                        onSelect={onSelectTag}
                    />
                    <KuzhambuButton
                        testId="classics-ming-customs-ming-customs-export-jobs-button"
                        ariaLabel="打开明代习俗导出任务"
                        icon={<ScheduleOutlined />}
                        onClick={onOpenExportJobs}
                    >
                        任务
                    </KuzhambuButton>
                </KuzhambuSpace>
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
