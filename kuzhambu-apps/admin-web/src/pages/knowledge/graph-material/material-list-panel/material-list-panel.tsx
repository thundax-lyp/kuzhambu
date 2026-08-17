import { useMutation } from "@tanstack/react-query";
import type { Key } from "react";
import { useMemo, useState } from "react";
import { RobotOutlined } from "@ant-design/icons";
import { Typography } from "antd";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import { MaterialDetailDrawer } from "./material-detail-drawer";
import type {
    GraphContentRefRecord,
    GraphMaterialListRecord,
    GraphMaterialStatus,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "@/pages/knowledge/graph-material/graph-material-types";
import {
    KuzhambuButton,
    KuzhambuSpace,
    KuzhambuTable,
    KuzhambuTag,
    type KuzhambuTableProps
} from "@/components";
import "./material-list-panel.css";

const { Text } = Typography;

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

const readRecordKey = (record: GraphMaterialListRecord) =>
    `${record.source.contentRef.contentType}:${record.source.contentRef.contentRefId}`;

interface MaterialListPanelProps {
    canExtractMaterial?: boolean;
    dataSource: GraphMaterialListRecord[];
    loading?: boolean;
    onRefreshMaterials: () => Promise<unknown>;
    pagination?: KuzhambuTableProps<GraphMaterialListRecord>["pagination"];
    showPlaceholder?: boolean;
}

export const MaterialListPanel = ({
    canExtractMaterial = true,
    dataSource,
    loading = false,
    onRefreshMaterials,
    pagination,
    showPlaceholder = false
}: MaterialListPanelProps) => {
    const [activeRecord, setActiveRecord] = useState<GraphMaterialListRecord | null>(null);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const batchExtractionMutation = useMutation({
        mutationFn: service.createBatchExtraction,
        onSuccess: () => {
            setSelectedRowKeys([]);
            void onRefreshMaterials();
        }
    });
    const recordByKey = useMemo(
        () =>
            new Map(
                dataSource.map((record) => [
                    `${record.source.contentRef.contentType}:${record.source.contentRef.contentRefId}`,
                    record
                ])
            ),
        [dataSource]
    );
    const selectedRecords = selectedRowKeys
        .map((key) => recordByKey.get(String(key)))
        .filter((record): record is GraphMaterialListRecord => record !== undefined);
    const closeMaterialDetailDrawer = () => {
        setActiveRecord(null);
    };
    const extractMaterial = (contentRef: GraphContentRefRecord) =>
        batchExtractionMutation.mutateAsync({ contentRefs: [contentRef] });
    const extractSelectedMaterials = () => {
        void batchExtractionMutation.mutateAsync({
            contentRefs: selectedRecords.map((record) => record.source.contentRef)
        });
    };
    const columns: KuzhambuTableProps<GraphMaterialListRecord>["columns"] = [
        {
            title: "标题",
            key: "title",
            render: (_, record) =>
                record.material ? (
                    <a
                        href="#"
                        aria-label={`打开素材 ${record.source.title}`}
                        data-testid={`knowledge-graph-material-open-${record.material.id}-link`}
                        onClick={(event) => {
                            event.preventDefault();
                            setActiveRecord(record);
                        }}
                    >
                        {record.source.title}
                    </a>
                ) : (
                    <Text strong>{record.source.title}</Text>
                )
        },
        {
            title: "发布",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (_, record) =>
                record.material ? (
                    <KuzhambuTag type={STATUS_TYPES[record.material.status]}>
                        {STATUS_LABELS[record.material.status]}
                    </KuzhambuTag>
                ) : (
                    <KuzhambuTag type="neutral">未抽取</KuzhambuTag>
                )
        },
        {
            title: "提取状态",
            key: "taskSummary",
            width: DEFAULT_COLUMN_WIDTHS.taskSummary,
            render: (_, record) => {
                const task = record.latestTask;
                if (!task) {
                    return <KuzhambuTag type="neutral">无任务</KuzhambuTag>;
                }
                return (
                    <KuzhambuSpace orientation="vertical" size={2}>
                        <KuzhambuTag type={TASK_STATUS_TYPES[task.executionStatus]}>
                            {TASK_STATUS_LABELS[task.executionStatus]}
                        </KuzhambuTag>
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
                    key: "view",
                    text: "查看",
                    testId: "knowledge-graph-material-view-button",
                    ariaLabel: `查看素材 ${record.source.title}`,
                    onClick: () => setActiveRecord(record)
                },
                {
                    key: "extract",
                    text: "提取",
                    testId: "knowledge-graph-material-extract-button",
                    ariaLabel: `提取 ${record.source.title}`,
                    disabled: !canExtractMaterial,
                    onClick: () => void extractMaterial(record.source.contentRef)
                }
            ]
        }
    ];

    return (
        <div className="graph-material-list-panel">
            {showPlaceholder ? (
                <div className="graph-material-list-placeholder" aria-label="图谱素材列表占位">
                    请选择左侧目录叶子节点查看素材列表
                </div>
            ) : (
                <KuzhambuTable<GraphMaterialListRecord>
                    ariaLabel="图谱素材列表"
                    batchActionBar={{
                        actions: (
                            <KuzhambuButton
                                disabled={!canExtractMaterial || selectedRecords.length === 0}
                                icon={<RobotOutlined />}
                                loading={batchExtractionMutation.isPending}
                                testId="knowledge-graph-material-batch-extract-button"
                                type="primary"
                                onClick={extractSelectedMaterials}
                            >
                                批量提取
                            </KuzhambuButton>
                        ),
                        selectedCount: selectedRowKeys.length
                    }}
                    columns={columns}
                    dataSource={dataSource}
                    loading={loading}
                    pagination={pagination}
                    rowKey={readRecordKey}
                    rowSelection={{
                        selectedRowKeys,
                        onChange: setSelectedRowKeys
                    }}
                    scroll={{ x: 720 }}
                    locale={{
                        emptyText: loading ? "图谱素材加载中..." : "暂无图谱素材"
                    }}
                />
            )}
            <MaterialDetailDrawer record={activeRecord} onClose={closeMaterialDetailDrawer} />
        </div>
    );
};
