import { ReloadOutlined } from "@ant-design/icons";
import { Button, Typography } from "antd";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { KuzhambuListPage } from "@/components/kuzhambu-list-page";
import type { KuzhambuTableProps } from "@/components/kuzhambu-table";
import { KuzhambuSwitch } from "@/components/kuzhambu-switch";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { TagStatusCommand } from "../taxonomy-service";
import type { TagPageQuery, TagRecord } from "../taxonomy-types";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    category: 160,
    reviewStatus: 120,
    contentRefCount: 120,
    createdAt: 180,
    status: 120,
    actions: 140
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime())
        ? "-"
        : date.toLocaleString("zh-CN", {
              hour12: false
          });
};

const readStatusValue = (tag: TagRecord): "ENABLED" | "DISABLED" => {
    return tag.status === "DISABLED" ? "DISABLED" : "ENABLED";
};

const readStatusLabel = (status: "ENABLED" | "DISABLED") => {
    return status === "ENABLED" ? "启用" : "禁用";
};

const readReviewStatusLabel = (status?: string | null) => {
    switch (status) {
        case "APPROVED":
            return "已通过";
        case "REJECTED":
            return "已拒绝";
        case "PENDING":
            return "待审核";
        default:
            return status || "-";
    }
};

const readSourceLabel = (source?: string | null) => {
    switch (source) {
        case "MANUAL":
            return "人工";
        case "AI_EXTRACTED":
            return "AI 提取";
        default:
            return source || "-";
    }
};

interface TagTableProps {
    canEditTag: boolean;
    loading: boolean;
    query: TagPageQuery;
    tags: TagRecord[];
    totalCount: number;
    onAdd: () => void;
    onChange: (values: TagPageQuery) => void;
    onEdit: (tag: TagRecord) => void;
    onRefresh: () => void;
    onStatusChange: (request: TagStatusCommand) => void;
}

export const TagTable = ({
    canEditTag,
    loading,
    query,
    tags,
    totalCount,
    onAdd,
    onChange,
    onEdit,
    onRefresh,
    onStatusChange
}: TagTableProps) => {
    const currentPageNo = query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = query.pageSize || DEFAULT_PAGE_SIZE;
    const hasSearch = Boolean(normalizeSearch(query.name));

    const updateQuery = (nextQuery: TagPageQuery) => {
        onChange({
            ...query,
            ...nextQuery,
            pageNo: nextQuery.pageNo || DEFAULT_PAGE_NO,
            pageSize: nextQuery.pageSize || currentPageSize
        });
    };

    const columns: KuzhambuTableProps<TagRecord>["columns"] = [
        {
            title: "标签名",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            ellipsis: true,
            render: (name: string, tag) => (
                <span aria-label={`统一标签名称 ${name || tag.id}`}>{name}</span>
            )
        },
        {
            title: "分类",
            dataIndex: "categoryName",
            key: "categoryName",
            width: DEFAULT_COLUMN_WIDTHS.category,
            render: (categoryName?: string | null) =>
                categoryName ? <KuzhambuTag type="info">{categoryName}</KuzhambuTag> : "-"
        },
        {
            title: "审核状态",
            dataIndex: "reviewStatus",
            key: "reviewStatus",
            width: DEFAULT_COLUMN_WIDTHS.reviewStatus,
            render: (reviewStatus?: string | null) => readReviewStatusLabel(reviewStatus)
        },
        {
            title: "来源",
            dataIndex: "source",
            key: "source",
            width: DEFAULT_COLUMN_WIDTHS.reviewStatus,
            render: (source?: string | null) => readSourceLabel(source)
        },
        {
            title: "内容引用",
            dataIndex: "contentRefCount",
            key: "contentRefCount",
            width: DEFAULT_COLUMN_WIDTHS.contentRefCount,
            render: (contentRefCount?: number | null) => contentRefCount ?? 0
        },
        {
            title: "创建时间",
            dataIndex: "createdAt",
            key: "createdAt",
            width: DEFAULT_COLUMN_WIDTHS.createdAt,
            render: (createdAt?: number | null) => <Text>{formatTimestamp(createdAt)}</Text>
        },
        {
            title: "状态",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (_, tag) => {
                const statusValue = readStatusValue(tag);
                return (
                    <KuzhambuSwitch
                        checked={statusValue === "ENABLED"}
                        checkedChildren={readStatusLabel("ENABLED")}
                        unCheckedChildren={readStatusLabel("DISABLED")}
                        disabled={!canEditTag}
                        aria-label={`切换 ${tag.name || tag.id} 状态`}
                        onChange={(checked) => {
                            onStatusChange({
                                id: tag.id,
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
            options: (tag) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑统一标签 ${tag.name || tag.id}`,
                    disabled: !canEditTag,
                    onClick: onEdit
                }
            ]
        }
    ];

    return (
        <KuzhambuListPage<TagRecord>
            pageClassName="knowledge-taxonomy-tag-page"
            title="统一标签管理"
            description="维护标签列表，支持分页、创建、更新、启用与禁用。"
            subjectName="统一标签"
            enableAdd={canEditTag}
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
                <Button icon={<ReloadOutlined />} onClick={onRefresh}>
                    刷新
                </Button>
            }
            filterActive={hasSearch}
            rowKey="id"
            columns={columns}
            dataSource={tags}
            loading={loading}
            pagination={{
                current: currentPageNo,
                pageSize: currentPageSize,
                total: totalCount,
                showTotal: (total) => `共 ${total} 个标签`,
                onChange: (pageNo, pageSize) => {
                    updateQuery({
                        pageNo,
                        pageSize
                    });
                }
            }}
            locale={{
                emptyText: loading ? "统一标签加载中..." : "暂无统一标签"
            }}
        />
    );
};
