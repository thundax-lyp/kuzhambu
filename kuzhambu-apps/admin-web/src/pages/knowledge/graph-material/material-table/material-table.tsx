import type { Key } from "react";
import { Typography } from "antd";
import {
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuTable,
    type KuzhambuTableProps,
    KuzhambuTag
} from "@/components";
import type {
    GraphContentRefRecord,
    GraphMaterialListRecord,
    GraphMaterialRecord,
    GraphMaterialStatus,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "@/pages/knowledge/graph-material/graph-material-types";

const { Text } = Typography;

const SOURCE_TYPE_LABELS: Readonly<Record<string, string>> = {
    MING_CUSTOMS: "明代风俗",
    SANCAI_ENTRY: "三才图会",
    WANGQI_DOCUMENT: "王祺文献"
};

const STATUS_LABELS: Readonly<Record<GraphMaterialStatus, string>> = {
    DRAFT: "草稿",
    FAILED: "失败",
    PUBLISHED: "已发布",
    PUBLISHING: "发布中",
    WITHDRAWING: "撤回中"
};

const STATUS_TYPES: Readonly<
    Record<GraphMaterialStatus, "neutral" | "info" | "success" | "warning" | "danger">
> = {
    DRAFT: "neutral",
    FAILED: "danger",
    PUBLISHED: "success",
    PUBLISHING: "info",
    WITHDRAWING: "warning"
};

const TASK_STATUS_LABELS: Readonly<Record<GraphTaskExecutionStatus, string>> = {
    CANCELLED: "已取消",
    FAILED: "已失败",
    PENDING: "待执行",
    RUNNING: "运行中",
    SUCCEEDED: "已成功"
};

const TASK_STATUS_TYPES: Readonly<
    Record<GraphTaskExecutionStatus, "neutral" | "info" | "success" | "warning" | "danger">
> = {
    CANCELLED: "neutral",
    FAILED: "danger",
    PENDING: "neutral",
    RUNNING: "info",
    SUCCEEDED: "success"
};

const DISPOSITION_LABELS: Readonly<Record<GraphTaskDisposition, string>> = {
    ADOPTED_MERGE: "合并采纳",
    ADOPTED_REPLACE: "替换采纳",
    DISCARDED: "已丢弃",
    PENDING: "待处置",
    SUPERSEDED: "已替代"
};

const DEFAULT_COLUMN_WIDTHS = {
    source: 180,
    stats: 180,
    status: 130,
    taskSummary: 210,
    disposition: 130,
    publication: 150,
    risk: 190,
    changedAt: 170,
    actions: 136
};

interface MaterialTableProps {
    canOpenMaterial?: boolean;
    canViewTasks?: boolean;
    dataSource: GraphMaterialListRecord[];
    loading?: boolean;
    onOpenMaterial: (material: GraphMaterialRecord) => void;
    onSelectionChange?: (selectedRowKeys: Key[]) => void;
    onViewTasks: (url: string) => void;
    selectedRowKeys?: Key[];
}

const formatContentRef = (contentRef: GraphContentRefRecord) =>
    `${contentRef.contentType}:${contentRef.contentRefId}`;

const formatTimestamp = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(Number(value));
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleString("zh-CN", { hour12: false });
};

const readSourceTypeLabel = (contentType: string) => SOURCE_TYPE_LABELS[contentType] || contentType;

const readRecordKey = (record: GraphMaterialListRecord) =>
    `${record.source.contentRef.contentType}:${record.source.contentRef.contentRefId}`;

const isStatsRefreshing = (record: GraphMaterialListRecord) =>
    record.latestTask?.executionStatus === "RUNNING" ||
    Number(record.materialStats?.activeTaskCount ?? 0) > 0;

const readRecentChangeAt = (record: GraphMaterialListRecord) =>
    record.latestTask?.completedAt ||
    record.latestTask?.requestedAt ||
    record.material?.publishedAt ||
    record.materialStats?.calculatedAt;

const buildGraphMaterialTaskUrl = (record: GraphMaterialListRecord) => {
    const params = new URLSearchParams();
    params.set("contentRefs", JSON.stringify([record.source.contentRef]));
    return `/knowledge/graph-extraction?${params.toString()}`;
};

export const MaterialTable = ({
    canOpenMaterial = true,
    canViewTasks = true,
    dataSource,
    loading = false,
    onOpenMaterial,
    onSelectionChange,
    onViewTasks,
    selectedRowKeys
}: MaterialTableProps) => {
    const columns: KuzhambuTableProps<GraphMaterialListRecord>["columns"] = [
        {
            title: "素材标题",
            key: "title",
            render: (_, record) => (
                <KuzhambuSpace orientation="vertical" size={2}>
                    {record.material ? (
                        <KuzhambuButton
                            testId={`knowledge-graph-material-open-${record.material.id}-link`}
                            type="link"
                            onClick={() => onOpenMaterial(record.material as GraphMaterialRecord)}
                        >
                            {record.source.title}
                        </KuzhambuButton>
                    ) : (
                        <Text strong>{record.source.title}</Text>
                    )}
                    {record.source.summary ? (
                        <Text type="secondary">{record.source.summary}</Text>
                    ) : null}
                </KuzhambuSpace>
            )
        },
        {
            title: "来源",
            key: "source",
            width: DEFAULT_COLUMN_WIDTHS.source,
            render: (_, record) => (
                <KuzhambuSpace orientation="vertical" size={2}>
                    <Text>{readSourceTypeLabel(record.source.contentType)}</Text>
                    <Text type="secondary">{formatContentRef(record.source.contentRef)}</Text>
                    <Text type="secondary">
                        {[record.source.category, record.source.volume]
                            .filter(Boolean)
                            .join(" / ") || "-"}
                    </Text>
                </KuzhambuSpace>
            )
        },
        {
            title: "统计",
            key: "stats",
            width: DEFAULT_COLUMN_WIDTHS.stats,
            render: (_, record) => {
                const stats = record.materialStats;
                if (!stats) {
                    return record.material ? "统计待生成" : "未初始化";
                }
                return (
                    <KuzhambuSpace orientation="vertical" size={2}>
                        <Text>
                            草稿 {stats.draftNodeCount} 点 / {stats.draftEdgeCount} 边
                        </Text>
                        <Text type="secondary">
                            已发布 {stats.publishedNodeCount} 点 / {stats.publishedEdgeCount} 边
                        </Text>
                        {isStatsRefreshing(record) ? (
                            <KuzhambuTag type="info">统计更新中</KuzhambuTag>
                        ) : null}
                    </KuzhambuSpace>
                );
            }
        },
        {
            title: "状态",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (_, record) =>
                record.material ? (
                    <KuzhambuTag type={STATUS_TYPES[record.material.status]}>
                        {STATUS_LABELS[record.material.status]}
                    </KuzhambuTag>
                ) : (
                    <KuzhambuTag type="neutral">未初始化/未抽取</KuzhambuTag>
                )
        },
        {
            title: "任务摘要",
            key: "taskSummary",
            width: DEFAULT_COLUMN_WIDTHS.taskSummary,
            render: (_, record) => {
                const task = record.latestTask;
                if (!task) {
                    return "暂无任务";
                }
                return (
                    <KuzhambuSpace orientation="vertical" size={2}>
                        <KuzhambuTag type={TASK_STATUS_TYPES[task.executionStatus]}>
                            {TASK_STATUS_LABELS[task.executionStatus]}
                        </KuzhambuTag>
                        <Text>
                            #{task.id} · {task.currentStage} · {task.progress}%
                        </Text>
                        <Text type="secondary">第 {task.attemptNo} 次</Text>
                    </KuzhambuSpace>
                );
            }
        },
        {
            title: "候选处置",
            key: "disposition",
            width: DEFAULT_COLUMN_WIDTHS.disposition,
            render: (_, record) => {
                const disposition = record.latestTask?.disposition;
                return disposition ? DISPOSITION_LABELS[disposition] : "无候选";
            }
        },
        {
            title: "发布贡献",
            key: "publication",
            width: DEFAULT_COLUMN_WIDTHS.publication,
            render: (_, record) => {
                const stats = record.materialStats;
                if (!stats) {
                    return "-";
                }
                return (
                    <KuzhambuSpace orientation="vertical" size={2}>
                        <Text>{stats.publicationContributionCount} 项贡献</Text>
                        <Text type="secondary">发布版本 {stats.statsRevision}</Text>
                    </KuzhambuSpace>
                );
            }
        },
        {
            title: "风险",
            key: "risk",
            width: DEFAULT_COLUMN_WIDTHS.risk,
            render: (_, record) => {
                if (record.material?.failureReason) {
                    return <Text type="danger">{record.material.failureReason}</Text>;
                }
                if (record.latestTask?.failureReason) {
                    return <Text type="danger">{record.latestTask.failureReason}</Text>;
                }
                if (Number(record.materialStats?.failedTaskCount ?? 0) > 0) {
                    return <KuzhambuTag type="danger">存在失败任务</KuzhambuTag>;
                }
                if (Number(record.materialStats?.pendingReviewTaskCount ?? 0) > 0) {
                    return <KuzhambuTag type="warning">候选待处置</KuzhambuTag>;
                }
                return "无";
            }
        },
        {
            title: "最近变更",
            key: "changedAt",
            width: DEFAULT_COLUMN_WIDTHS.changedAt,
            render: (_, record) => formatTimestamp(readRecentChangeAt(record))
        },
        {
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            options: (record) => [
                {
                    key: "viewTasks",
                    text: "查看任务",
                    testId: "knowledge-graph-material-view-tasks-button",
                    ariaLabel: `查看任务 ${record.source.title}`,
                    disabled: !canViewTasks,
                    onClick: () => onViewTasks(buildGraphMaterialTaskUrl(record))
                },
                {
                    key: "open",
                    text: "打开素材",
                    testId: "knowledge-graph-material-open-material-button",
                    ariaLabel: `打开素材 ${record.source.title}`,
                    disabled: !canOpenMaterial || !record.material,
                    onClick: () => {
                        if (record.material) {
                            onOpenMaterial(record.material);
                        }
                    }
                }
            ]
        }
    ];

    return (
        <KuzhambuTable<GraphMaterialListRecord>
            ariaLabel="图谱素材复合表格"
            columns={columns}
            dataSource={dataSource}
            loading={loading}
            pagination={false}
            rowKey={readRecordKey}
            rowSelection={
                selectedRowKeys && onSelectionChange
                    ? {
                          selectedRowKeys,
                          onChange: onSelectionChange
                      }
                    : undefined
            }
            scroll={{ x: 1380 }}
            locale={{
                emptyText: loading ? "图谱素材加载中..." : "暂无图谱素材"
            }}
        />
    );
};
