import { Typography } from "antd";
import { KuzhambuSpace, KuzhambuTable, type KuzhambuTableProps, KuzhambuTag } from "@/components";
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
    actions: 180,
    status: 130,
    taskSummary: 190
};

interface MaterialTableProps {
    canExtractMaterial?: boolean;
    canOpenMaterial?: boolean;
    canViewTasks?: boolean;
    dataSource: GraphMaterialListRecord[];
    loading?: boolean;
    onOpenMaterial: (material: GraphMaterialRecord) => void;
    onExtract: (contentRef: GraphContentRefRecord) => Promise<unknown>;
    onViewTasks: (url: string) => void;
    pagination?: KuzhambuTableProps<GraphMaterialListRecord>["pagination"];
}

const formatContentRef = (contentRef: GraphContentRefRecord) =>
    `${contentRef.contentType}:${contentRef.contentRefId}`;

const readSourceTypeLabel = (contentType: string) => SOURCE_TYPE_LABELS[contentType] || contentType;

const readRecordKey = (record: GraphMaterialListRecord) =>
    `${record.source.contentRef.contentType}:${record.source.contentRef.contentRefId}`;

const buildGraphMaterialTaskUrl = (record: GraphMaterialListRecord) => {
    const params = new URLSearchParams();
    params.set("contentRefs", JSON.stringify([record.source.contentRef]));
    return `/knowledge/graph-extraction?${params.toString()}`;
};

export const MaterialTable = ({
    canExtractMaterial = true,
    canOpenMaterial = true,
    canViewTasks = true,
    dataSource,
    loading = false,
    onOpenMaterial,
    onExtract,
    onViewTasks,
    pagination
}: MaterialTableProps) => {
    const columns: KuzhambuTableProps<GraphMaterialListRecord>["columns"] = [
        {
            title: "素材标题",
            key: "title",
            render: (_, record) => (
                <KuzhambuSpace orientation="vertical" size={2}>
                    {record.material ? (
                        <a
                            href="#"
                            aria-label={`打开素材 ${record.source.title}`}
                            data-testid={`knowledge-graph-material-open-${record.material.id}-link`}
                            onClick={(event) => {
                                event.preventDefault();
                                onOpenMaterial(record.material as GraphMaterialRecord);
                            }}
                        >
                            {record.source.title}
                        </a>
                    ) : (
                        <Text strong>{record.source.title}</Text>
                    )}
                    {record.source.summary ? (
                        <Text type="secondary">{record.source.summary}</Text>
                    ) : null}
                    <Text type="secondary">
                        {readSourceTypeLabel(record.source.contentType)} ·{" "}
                        {formatContentRef(record.source.contentRef)}
                    </Text>
                </KuzhambuSpace>
            )
        },
        {
            title: "发布状态",
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
            title: "提取状态",
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
                        {task.disposition ? (
                            <Text type="secondary">{DISPOSITION_LABELS[task.disposition]}</Text>
                        ) : null}
                    </KuzhambuSpace>
                );
            }
        },
        {
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            options: (record) => [
                {
                    key: "extract",
                    text: "提取",
                    testId: "knowledge-graph-material-extract-button",
                    ariaLabel: `提取 ${record.source.title}`,
                    disabled: !canExtractMaterial,
                    onClick: () => void onExtract(record.source.contentRef)
                },
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
            ariaLabel="图谱素材列表"
            columns={columns}
            dataSource={dataSource}
            loading={loading}
            pagination={pagination}
            rowKey={readRecordKey}
            scroll={{ x: 720 }}
            locale={{
                emptyText: loading ? "图谱素材加载中..." : "暂无图谱素材"
            }}
        />
    );
};
