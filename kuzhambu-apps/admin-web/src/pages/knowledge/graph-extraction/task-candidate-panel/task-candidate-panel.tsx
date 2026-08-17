import { Empty, Typography } from "antd";
import type { ComponentProps, ReactNode } from "react";
import {
    KuzhambuAlert,
    KuzhambuCard,
    KuzhambuDescriptions,
    KuzhambuSpace,
    KuzhambuTable,
    KuzhambuTag
} from "@/components";
import type {
    GraphCandidateDiffChangeType,
    GraphCandidateDiffRecord,
    GraphCandidateEdgeRecord,
    GraphCandidateIssueRecord,
    GraphCandidateNodeRecord,
    GraphCandidatePreviewRecord
} from "@/pages/knowledge/graph-extraction/graph-extraction-types";

const { Text } = Typography;

interface TaskCandidatePanelProps {
    candidate?: GraphCandidatePreviewRecord | null;
}

interface CandidateNodeRow extends GraphCandidateNodeRecord {
    rowKey: string;
}

interface CandidateEdgeRow extends GraphCandidateEdgeRecord {
    rowKey: string;
}

interface CandidateIssueRow extends GraphCandidateIssueRecord {
    rowKey: string;
}

interface CandidateDiffRow extends GraphCandidateDiffRecord {
    rowKey: string;
}

const DIFF_CHANGE_LABELS: Record<GraphCandidateDiffChangeType, string> = {
    ADD: "新增",
    CONFLICT: "冲突",
    REMOVE: "移除",
    UPDATE: "更新"
};

const DIFF_CHANGE_TAG_TYPES: Record<
    GraphCandidateDiffChangeType,
    ComponentProps<typeof KuzhambuTag>["type"]
> = {
    ADD: "success",
    CONFLICT: "danger",
    REMOVE: "warning",
    UPDATE: "info"
};

const ISSUE_SEVERITY_TAG_TYPES: Record<string, ComponentProps<typeof KuzhambuTag>["type"]> = {
    BLOCKING: "danger",
    INFO: "info",
    WARNING: "warning"
};

const formatProperties = (value: Record<string, unknown>) => {
    const entries = Object.entries(value);
    if (!entries.length) {
        return "-";
    }
    return entries.map(([key, propertyValue]) => `${key}: ${String(propertyValue)}`).join("，");
};

const formatList = (values?: string[] | null) => {
    return values?.length ? values.join("，") : "-";
};

const formatDiffIssues = (issues?: GraphCandidateIssueRecord[] | null) => {
    return issues?.length ? issues.map((issue) => issue.message).join("；") : "-";
};

const renderSectionTitle = (title: string, count: number): ReactNode => (
    <KuzhambuSpace size={8} wrap>
        <Text strong>{title}</Text>
        <KuzhambuTag type="neutral">{count}</KuzhambuTag>
    </KuzhambuSpace>
);

export const TaskCandidatePanel = ({ candidate }: TaskCandidatePanelProps) => {
    if (!candidate) {
        return (
            <Empty
                data-testid="knowledge-graph-extraction-task-detail-candidate-section"
                description="候选不可用"
            />
        );
    }

    const nodeRows: CandidateNodeRow[] = candidate.nodes.map((node) => ({
        ...node,
        rowKey: node.candidateObjectId
    }));
    const edgeRows: CandidateEdgeRow[] = candidate.edges.map((edge) => ({
        ...edge,
        rowKey: edge.candidateObjectId
    }));
    const issueRows: CandidateIssueRow[] = candidate.issues.map((issue, index) => ({
        ...issue,
        rowKey: `${issue.code}-${issue.objectId || "global"}-${index}`
    }));
    const diffRows: CandidateDiffRow[] = candidate.diff.map((diff, index) => ({
        ...diff,
        rowKey: `${diff.candidateObjectId}-${diff.changeType}-${index}`
    }));
    const hasBlockingIssue = issueRows.some((issue) => issue.severity === "BLOCKING");

    return (
        <KuzhambuSpace
            data-testid="knowledge-graph-extraction-task-detail-candidate-section"
            orientation="vertical"
            size={16}
            style={{ width: "100%" }}
        >
            <KuzhambuDescriptions
                column={1}
                size="small"
                variant="detail"
                bordered
                items={[
                    {
                        key: "candidateId",
                        label: "候选 ID",
                        children: candidate.candidateId
                    },
                    {
                        key: "summary",
                        label: "候选摘要",
                        children: `节点 ${nodeRows.length}，关系 ${edgeRows.length}，告警 ${issueRows.length}，Diff ${diffRows.length}`
                    }
                ]}
            />

            {hasBlockingIssue ? (
                <KuzhambuAlert
                    description="存在阻断级告警，应用前需要先处理冲突或数据问题。"
                    showIcon
                    title="候选存在阻断告警"
                    type="error"
                />
            ) : null}

            <KuzhambuCard size="small" title={renderSectionTitle("候选节点", nodeRows.length)}>
                <KuzhambuTable<CandidateNodeRow>
                    ariaLabel="图谱抽取候选节点"
                    dataSource={nodeRows}
                    pagination={false}
                    rowKey="rowKey"
                    size="small"
                    columns={[
                        {
                            dataIndex: "name",
                            key: "name",
                            title: "名称"
                        },
                        {
                            dataIndex: "nodeType",
                            key: "nodeType",
                            title: "类型",
                            width: 140
                        },
                        {
                            key: "properties",
                            render: (_, record) => formatProperties(record.properties),
                            title: "属性"
                        }
                    ]}
                    locale={{ emptyText: "暂无候选节点" }}
                />
            </KuzhambuCard>

            <KuzhambuCard size="small" title={renderSectionTitle("候选关系", edgeRows.length)}>
                <KuzhambuTable<CandidateEdgeRow>
                    ariaLabel="图谱抽取候选关系"
                    dataSource={edgeRows}
                    pagination={false}
                    rowKey="rowKey"
                    size="small"
                    columns={[
                        {
                            dataIndex: "sourceCandidateNodeId",
                            key: "sourceCandidateNodeId",
                            title: "起点"
                        },
                        {
                            dataIndex: "relationType",
                            key: "relationType",
                            title: "关系",
                            width: 160
                        },
                        {
                            dataIndex: "targetCandidateNodeId",
                            key: "targetCandidateNodeId",
                            title: "终点"
                        },
                        {
                            key: "qualifiers",
                            render: (_, record) => formatProperties(record.qualifiers),
                            title: "限定信息"
                        }
                    ]}
                    locale={{ emptyText: "暂无候选关系" }}
                />
            </KuzhambuCard>

            <KuzhambuCard size="small" title={renderSectionTitle("候选告警", issueRows.length)}>
                <KuzhambuTable<CandidateIssueRow>
                    ariaLabel="图谱抽取候选告警"
                    dataSource={issueRows}
                    pagination={false}
                    rowKey="rowKey"
                    size="small"
                    columns={[
                        {
                            key: "severity",
                            render: (_, record) => (
                                <KuzhambuTag
                                    type={ISSUE_SEVERITY_TAG_TYPES[record.severity] ?? "neutral"}
                                >
                                    {record.severity}
                                </KuzhambuTag>
                            ),
                            title: "级别",
                            width: 120
                        },
                        {
                            dataIndex: "code",
                            key: "code",
                            title: "代码",
                            width: 180
                        },
                        {
                            key: "object",
                            render: (_, record) =>
                                [record.objectType, record.objectId, record.field]
                                    .filter(Boolean)
                                    .join(" / ") || "-",
                            title: "对象"
                        },
                        {
                            dataIndex: "message",
                            key: "message",
                            title: "说明"
                        }
                    ]}
                    locale={{ emptyText: "暂无候选告警" }}
                />
            </KuzhambuCard>

            <KuzhambuCard size="small" title={renderSectionTitle("Diff 预览", diffRows.length)}>
                <KuzhambuTable<CandidateDiffRow>
                    ariaLabel="图谱抽取候选 Diff 预览"
                    dataSource={diffRows}
                    pagination={false}
                    rowKey="rowKey"
                    size="small"
                    columns={[
                        {
                            key: "changeType",
                            render: (_, record) => (
                                <KuzhambuTag type={DIFF_CHANGE_TAG_TYPES[record.changeType]}>
                                    {DIFF_CHANGE_LABELS[record.changeType]}
                                </KuzhambuTag>
                            ),
                            title: "变更",
                            width: 100
                        },
                        {
                            dataIndex: "objectType",
                            key: "objectType",
                            title: "对象类型",
                            width: 120
                        },
                        {
                            dataIndex: "candidateObjectId",
                            key: "candidateObjectId",
                            title: "候选对象"
                        },
                        {
                            key: "draftObjectId",
                            render: (_, record) => record.draftObjectId || "-",
                            title: "现有对象"
                        },
                        {
                            key: "changedFields",
                            render: (_, record) => formatList(record.changedFields),
                            title: "变更字段"
                        },
                        {
                            key: "issues",
                            render: (_, record) => formatDiffIssues(record.issues),
                            title: "Diff 告警"
                        }
                    ]}
                    locale={{ emptyText: "暂无 Diff" }}
                />
            </KuzhambuCard>
        </KuzhambuSpace>
    );
};
