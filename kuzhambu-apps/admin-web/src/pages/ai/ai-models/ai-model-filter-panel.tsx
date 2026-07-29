import { DeleteOutlined, ReloadOutlined } from "@ant-design/icons";
import type { ReactNode } from "react";
import {
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuSelect,
    KuzhambuSpace,
    type KuzhambuTableBatchActionBarProps
} from "@/components";

import { API_SOURCE_OPTIONS, readApiSourceMeta } from "./ai-models-metadata";
import type { AiModelRecord } from "./ai-models-types";

export interface ModelFilters {
    apiSource?: string | null;
    enabled: "ALL" | "ENABLED" | "DISABLED";
}

interface AiModelFilterPanelProps {
    batchDeleting: boolean;
    batchUpdating: boolean;
    canEditConfig: boolean;
    filterActive: boolean;
    filters: ModelFilters;
    hasSelectedModels: boolean;
    loading: boolean;
    onAdd: () => void;
    onBatchDelete: () => void;
    onBatchUpdateEnabled: (enabled: boolean) => void;
    onFilterApply: () => void;
    onFilterReset: () => void;
    onFiltersChange: (filters: ModelFilters) => void;
    onRefresh: () => void;
    onSearchChange: (value: string) => void;
    searchText: string;
    selectedCount: number;
    table: (batchActionBar: KuzhambuTableBatchActionBarProps) => ReactNode;
}

export const AiModelFilterPanel = ({
    batchDeleting,
    batchUpdating,
    canEditConfig,
    filterActive,
    filters,
    hasSelectedModels,
    loading,
    onAdd,
    onBatchDelete,
    onBatchUpdateEnabled,
    onFilterApply,
    onFilterReset,
    onFiltersChange,
    onRefresh,
    onSearchChange,
    searchText,
    selectedCount,
    table
}: AiModelFilterPanelProps) => {
    const batchActionBar: KuzhambuTableBatchActionBarProps = {
        className: "ai-models-table-toolbar",
        selectedCount,
        actions: (
            <KuzhambuSpace wrap>
                <KuzhambuButton
                    testId="ai-models-enable-button"
                    disabled={!canEditConfig || !hasSelectedModels}
                    loading={batchUpdating}
                    onClick={() => onBatchUpdateEnabled(true)}
                >
                    启用
                </KuzhambuButton>
                <KuzhambuButton
                    testId="ai-models-disable-button"
                    disabled={!canEditConfig || !hasSelectedModels}
                    loading={batchUpdating}
                    onClick={() => onBatchUpdateEnabled(false)}
                >
                    禁用
                </KuzhambuButton>
                <KuzhambuButton
                    testId="ai-models-batch-delete-button"
                    danger
                    icon={<DeleteOutlined />}
                    disabled={!canEditConfig || !hasSelectedModels}
                    loading={batchDeleting}
                    onClick={onBatchDelete}
                >
                    批量删除
                </KuzhambuButton>
            </KuzhambuSpace>
        )
    };

    return (
        <KuzhambuListPage<AiModelRecord>
            pageClassName="ai-models-page"
            title="模型管理"
            description="维护 AI 模型、供应商、能力和调用参数。"
            subjectName="模型"
            enableAdd={canEditConfig}
            enableFilter
            enableSearch
            searchShortcut="⌘K"
            searchValue={searchText}
            searchPlaceholder="搜索模型..."
            onSearchChange={onSearchChange}
            onAdd={onAdd}
            filterActive={filterActive}
            filterFields={[
                {
                    name: "apiSource",
                    label: "供应商",
                    render: () => (
                        <KuzhambuSelect
                            allowClear
                            value={filters.apiSource ?? undefined}
                            options={API_SOURCE_OPTIONS.map((value) => ({
                                label: readApiSourceMeta(value).label,
                                value
                            }))}
                            onChange={(apiSource) =>
                                onFiltersChange({
                                    ...filters,
                                    apiSource: apiSource ?? null
                                })
                            }
                        />
                    )
                },
                {
                    name: "enabled",
                    label: "状态",
                    render: () => (
                        <KuzhambuSelect
                            value={filters.enabled}
                            options={[
                                { label: "全部", value: "ALL" },
                                { label: "启用", value: "ENABLED" },
                                { label: "禁用", value: "DISABLED" }
                            ]}
                            onChange={(enabled) =>
                                onFiltersChange({
                                    ...filters,
                                    enabled
                                })
                            }
                        />
                    )
                }
            ]}
            onFilterApply={onFilterApply}
            onFilterReset={onFilterReset}
            pageActions={
                <KuzhambuButton
                    testId="ai-models-refresh-button"
                    icon={<ReloadOutlined />}
                    loading={loading}
                    onClick={onRefresh}
                >
                    刷新
                </KuzhambuButton>
            }
            content={table(batchActionBar)}
        />
    );
};
