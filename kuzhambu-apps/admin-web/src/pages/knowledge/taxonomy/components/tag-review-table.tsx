import { CheckOutlined, CloseOutlined, ReloadOutlined } from "@ant-design/icons";
import { Typography } from "antd";
import type { Key } from "react";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import {
    KuzhambuListPage,
    type KuzhambuTableProps,
    KuzhambuTag,
    KuzhambuButton
} from "@/components";

import type { TagReviewPageQuery } from "../taxonomy-service";
import type { TagRecord } from "../taxonomy-types";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    category: 160,
    source: 140,
    createdAt: 180,
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

interface TagReviewTableProps {
    loading: boolean;
    query: TagReviewPageQuery;
    selectedRowKeys?: Key[];
    tags: TagRecord[];
    totalCount: number;
    onBatchApprove: () => void;
    onBatchReject: () => void;
    onChange: (values: TagReviewPageQuery) => void;
    onOpenReview: (tag: TagRecord) => void;
    onRefresh: () => void;
    onSelectedRowKeysChange?: (keys: Key[]) => void;
}

export const TagReviewTable = ({
    loading,
    query,
    selectedRowKeys = [],
    tags,
    totalCount,
    onBatchApprove,
    onBatchReject,
    onChange,
    onOpenReview,
    onRefresh,
    onSelectedRowKeysChange
}: TagReviewTableProps) => {
    const currentPageNo = query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = query.pageSize || DEFAULT_PAGE_SIZE;
    const hasSearch = Boolean(normalizeSearch(query.name));

    const updateQuery = (nextQuery: TagReviewPageQuery) => {
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
                <KuzhambuButton
                    testId="knowledge-taxonomy-tag-review-action-button"
                    type="link"
                    className="knowledge-taxonomy-tag-review-trigger"
                    onClick={() => onOpenReview(tag)}
                >
                    {name}
                </KuzhambuButton>
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
            title: "来源",
            dataIndex: "source",
            key: "source",
            width: DEFAULT_COLUMN_WIDTHS.source,
            render: (source?: string | null) => readSourceLabel(source)
        },
        {
            title: "创建时间",
            dataIndex: "createdAt",
            key: "createdAt",
            width: DEFAULT_COLUMN_WIDTHS.createdAt,
            render: (createdAt?: number | null) => <Text>{formatTimestamp(createdAt)}</Text>
        },
        {
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            options: (tag) => [
                {
                    key: "review",
                    text: "审核",
                    ariaLabel: `审核标签 ${tag.name || tag.id}`,
                    onClick: onOpenReview
                }
            ]
        }
    ];

    return (
        <KuzhambuListPage<TagRecord>
            pageClassName="knowledge-taxonomy-review-page"
            title="待审核标签"
            description="查看待审核标签详情并执行通过或拒绝。"
            subjectName="待审核标签"
            enableSearch
            searchShortcut="⌘K"
            searchValue={query.name || ""}
            onSearchChange={(value) =>
                updateQuery({
                    name: normalizeSearch(value),
                    pageNo: DEFAULT_PAGE_NO
                })
            }
            pageActions={
                <KuzhambuButton
                    testId="knowledge-taxonomy-tag-review-refresh-button"
                    icon={<ReloadOutlined />}
                    onClick={onRefresh}
                >
                    刷新
                </KuzhambuButton>
            }
            batchActions={
                <>
                    <KuzhambuButton
                        testId="knowledge-taxonomy-tag-review-action-button-2"
                        icon={<CheckOutlined />}
                        disabled={selectedRowKeys.length < 1}
                        onClick={onBatchApprove}
                    >
                        批量通过
                    </KuzhambuButton>
                    <KuzhambuButton
                        testId="knowledge-taxonomy-tag-review-action-button-3"
                        danger
                        icon={<CloseOutlined />}
                        disabled={selectedRowKeys.length < 1}
                        onClick={onBatchReject}
                    >
                        批量拒绝
                    </KuzhambuButton>
                </>
            }
            selectedCount={selectedRowKeys.length}
            filterActive={hasSearch}
            rowKey="id"
            rowSelection={{
                selectedRowKeys,
                onChange: (keys) => onSelectedRowKeysChange?.(keys)
            }}
            columns={columns}
            dataSource={tags}
            loading={loading}
            pagination={{
                current: currentPageNo,
                pageSize: currentPageSize,
                total: totalCount,
                showTotal: (total) => `共 ${total} 个待审核标签`,
                onChange: (pageNo, pageSize) => {
                    updateQuery({
                        pageNo,
                        pageSize
                    });
                }
            }}
            locale={{
                emptyText: loading ? "待审核标签加载中..." : "暂无待审核标签"
            }}
        />
    );
};
