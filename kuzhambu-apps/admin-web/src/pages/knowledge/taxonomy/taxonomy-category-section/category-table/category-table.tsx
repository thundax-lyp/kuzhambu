import { ReloadOutlined } from "@ant-design/icons";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import {
    KuzhambuListPage,
    type KuzhambuTableProps,
    KuzhambuSwitch,
    KuzhambuButton
} from "@/components";

import type {
    TagCategoryPageQuery,
    TagCategoryStatusCommand
} from "@/pages/knowledge/taxonomy/taxonomy-service";
import type { TagCategoryRecord } from "@/pages/knowledge/taxonomy/taxonomy-types";

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    description: 300,
    priority: 120,
    status: 140,
    actions: 140
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readStatusValue = (category: TagCategoryRecord): "ENABLED" | "DISABLED" => {
    return category.status === "DISABLED" ? "DISABLED" : "ENABLED";
};

const readStatusLabel = (status: "ENABLED" | "DISABLED") => {
    return status === "ENABLED" ? "启用" : "禁用";
};

interface CategoryTableProps {
    canEditCategory: boolean;
    categories: TagCategoryRecord[];
    loading: boolean;
    totalCount: number;
    query: TagCategoryPageQuery;
    onAdd: () => void;
    onChange: (values: TagCategoryPageQuery) => void;
    onEdit: (category: TagCategoryRecord) => void;
    onRefresh: () => void;
    onStatusChange: (request: TagCategoryStatusCommand) => void;
}

export const CategoryTable = ({
    canEditCategory,
    categories,
    loading,
    totalCount,
    query,
    onAdd,
    onChange,
    onEdit,
    onRefresh,
    onStatusChange
}: CategoryTableProps) => {
    const currentPageNo = query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = query.pageSize || DEFAULT_PAGE_SIZE;
    const hasSearch = Boolean(normalizeSearch(query.name));

    const updateQuery = (nextQuery: TagCategoryPageQuery) => {
        onChange({
            ...query,
            ...nextQuery,
            pageNo: nextQuery.pageNo || DEFAULT_PAGE_NO,
            pageSize: nextQuery.pageSize || currentPageSize
        });
    };

    const columns: KuzhambuTableProps<TagCategoryRecord>["columns"] = [
        {
            title: "分类名",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            ellipsis: true,
            render: (name: string, category) => (
                <span aria-label={`标签分类名称 ${name || category.id}`}>{name}</span>
            )
        },
        {
            title: "描述",
            dataIndex: "description",
            key: "description",
            width: DEFAULT_COLUMN_WIDTHS.description,
            ellipsis: true,
            render: (description?: string | null) => description || "-"
        },
        {
            title: "优先级",
            dataIndex: "priority",
            key: "priority",
            width: DEFAULT_COLUMN_WIDTHS.priority,
            render: (priority?: number | null) => priority ?? "-"
        },
        {
            title: "状态",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (_, category) => {
                const statusValue = readStatusValue(category);
                return (
                    <KuzhambuSwitch
                        checked={statusValue === "ENABLED"}
                        checkedChildren={readStatusLabel("ENABLED")}
                        unCheckedChildren={readStatusLabel("DISABLED")}
                        disabled={!canEditCategory}
                        aria-label={`切换 ${category.name || category.id} 状态`}
                        onChange={(checked) => {
                            onStatusChange({
                                id: category.id,
                                status: checked ? "ENABLED" : "DISABLED"
                            });
                        }}
                    />
                );
            }
        },
        {
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            options: (category) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑标签分类 ${category.name || category.id}`,
                    disabled: !canEditCategory,
                    onClick: onEdit
                }
            ]
        }
    ];

    return (
        <KuzhambuListPage<TagCategoryRecord>
            pageClassName="knowledge-taxonomy-category-page"
            title="标签分类管理"
            description="维护标签分类，支持分页、创建、更新、启用与禁用。"
            subjectName="标签分类"
            enableAdd={canEditCategory}
            enableSearch
            searchShortcut="⌘K"
            searchValue={query.name || ""}
            onSearchChange={(value) =>
                updateQuery({
                    name: normalizeSearch(value),
                    pageNo: DEFAULT_PAGE_NO
                })
            }
            onAdd={onAdd}
            pageActions={
                <KuzhambuButton
                    testId="knowledge-taxonomy-category-refresh-button"
                    icon={<ReloadOutlined />}
                    onClick={onRefresh}
                >
                    刷新
                </KuzhambuButton>
            }
            filterActive={hasSearch}
            rowKey="id"
            columns={columns}
            dataSource={categories}
            loading={loading}
            pagination={{
                current: currentPageNo,
                pageSize: currentPageSize,
                total: totalCount,
                showTotal: (total) => `共 ${total} 个分类`,
                onChange: (pageNo, pageSize) => {
                    updateQuery({
                        pageNo,
                        pageSize
                    });
                }
            }}
            locale={{
                emptyText: loading ? "标签分类加载中..." : "暂无标签分类"
            }}
        />
    );
};
