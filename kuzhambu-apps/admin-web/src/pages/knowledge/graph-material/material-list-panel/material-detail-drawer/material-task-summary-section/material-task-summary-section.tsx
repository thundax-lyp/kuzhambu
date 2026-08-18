import { App, Empty, Typography } from "antd";
import { RobotOutlined } from "@ant-design/icons";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { hasPermission } from "@/auth/permission-storage";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuDescriptions,
    KuzhambuGraph,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type { KuzhambuGraphSpoItem } from "@/components/kuzhambu-graph";
import type {
    GraphExtractionCandidatePreviewRecord,
    GraphMaterialDetailRecord,
    GraphTaskDisposition,
    GraphTaskExecutionStatus
} from "@/pages/knowledge/graph-material/graph-material-types";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import "./material-task-summary-section.css";

const { Text } = Typography;

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
    PENDING: "warning",
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

const RELATION_TYPE_LABELS: Readonly<Record<string, string>> = {
    ANCESTOR_OF: "祖先/后裔",
    ASSOCIATED_WITH: "相关",
    AUTHORED: "撰著",
    CAUSES: "导致/引起",
    COMPILED: "编纂",
    DEPICTS: "描绘",
    DESCRIBES: "记述",
    HOLDS_OFFICE: "任职",
    LOCATED_IN: "位于",
    MADE_OF: "制成材料",
    MEMBER_OF: "隶属/成员",
    MENTIONS: "提及",
    OCCURS_AT: "发生于",
    PARENT_OF: "父母/子女",
    PARTICIPATED_IN: "参与",
    PART_OF: "构成/隶属",
    PRACTICES: "实行/奉行",
    RELATED_TO: "相关",
    RULES: "统治/管辖",
    SPOUSE_OF: "配偶",
    SUCCEEDS: "继承/取代",
    USES: "使用/采用",
    WORSHIPS: "崇祀",
    OBSERVED_IN: "观测/出现于"
};

interface MaterialTaskSummarySectionProps {
    detail: GraphMaterialDetailRecord | null;
}

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

const renderTaskStatus = (status: GraphTaskExecutionStatus) => (
    <KuzhambuTag type={TASK_STATUS_TYPES[status]}>{TASK_STATUS_LABELS[status]}</KuzhambuTag>
);

const hasActiveExtractionTask = (status?: GraphTaskExecutionStatus) =>
    status === "PENDING" || status === "RUNNING";

const renderDisposition = (disposition?: GraphTaskDisposition | null) => {
    if (!disposition) {
        return <Text type="secondary">-</Text>;
    }
    return (
        <KuzhambuTag type={disposition === "PENDING" ? "warning" : "success"}>
            {DISPOSITION_LABELS[disposition]}
        </KuzhambuTag>
    );
};

const renderCurrentStage = (stage?: string | null) => (
    <KuzhambuTag type="neutral">{stage ?? "-"}</KuzhambuTag>
);

interface ExtractedGraphDocument {
    nodes: Array<{ id: string; name: string }>;
    edges: Array<{ sourceId: string; targetId: string; relationType: string }>;
}

const parseExtractedGraphDocument = (
    candidate?: GraphExtractionCandidatePreviewRecord | null
): ExtractedGraphDocument | null => {
    if (!candidate?.resultSummaryJson) {
        return null;
    }
    try {
        const value: unknown = JSON.parse(candidate.resultSummaryJson);
        if (!value || typeof value !== "object") {
            return null;
        }
        const document = value as {
            nodes?: unknown;
            edges?: unknown;
        };
        const nodes = Array.isArray(document.nodes)
            ? document.nodes.flatMap((node) => {
                  if (!node || typeof node !== "object") {
                      return [];
                  }
                  const value = node as { id?: unknown; name?: unknown };
                  if (typeof value.id !== "string") {
                      return [];
                  }
                  return [
                      { id: value.id, name: typeof value.name === "string" ? value.name : value.id }
                  ];
              })
            : [];
        const edges = Array.isArray(document.edges)
            ? document.edges.flatMap((edge) => {
                  if (!edge || typeof edge !== "object") {
                      return [];
                  }
                  const value = edge as {
                      relationType?: unknown;
                      sourceId?: unknown;
                      targetId?: unknown;
                  };
                  if (typeof value.sourceId !== "string" || typeof value.targetId !== "string") {
                      return [];
                  }
                  return [
                      {
                          relationType:
                              typeof value.relationType === "string" ? value.relationType : "关联",
                          sourceId: value.sourceId,
                          targetId: value.targetId
                      }
                  ];
              })
            : [];
        return { nodes, edges };
    } catch {
        return null;
    }
};

const toKuzhambuGraphSpoList = (document: ExtractedGraphDocument): KuzhambuGraphSpoItem[] => {
    const nodeNames = new Map(document.nodes.map((node) => [node.id, node.name]));
    return document.edges.map((edge) => ({
        object: nodeNames.get(edge.targetId) ?? edge.targetId,
        predicate: RELATION_TYPE_LABELS[edge.relationType] ?? edge.relationType,
        subject: nodeNames.get(edge.sourceId) ?? edge.sourceId
    }));
};

export const MaterialTaskSummarySection = ({ detail }: MaterialTaskSummarySectionProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const canExtractMaterial = hasPermission("knowledge:graph:edit");
    const extractionMutation = useMutation({
        mutationFn: service.createExtraction,
        onSuccess: async (task) => {
            await queryClient.refetchQueries({
                queryKey: ["knowledge", "graph-material"],
                type: "active"
            });
            messageApi.success(`抽取任务已创建 #${task?.id ?? "-"}`);
        }
    });
    const taskSummary = detail?.taskSummary;
    const latestTask = detail?.extractionTasks?.[0] ?? taskSummary?.latestTask;
    const retryExtractionMutation = useMutation({
        mutationFn: service.retryExtraction,
        onSuccess: async (task) => {
            await queryClient.refetchQueries({
                queryKey: ["knowledge", "graph-material"],
                type: "active"
            });
            messageApi.success(`抽取任务已重试 #${task?.id ?? "-"}`);
        }
    });
    const extractedGraph = parseExtractedGraphDocument(detail?.latestTaskCandidate);
    const extractedGraphSpoList = extractedGraph ? toKuzhambuGraphSpoList(extractedGraph) : [];
    const canApplyLatestCandidate =
        canExtractMaterial &&
        latestTask?.executionStatus === "SUCCEEDED" &&
        latestTask.disposition === "PENDING" &&
        Boolean(detail?.latestTaskCandidate) &&
        Boolean(detail?.material?.lockVersion);
    const applyCandidateMutation = useMutation({
        mutationFn: (applyMode: "MERGE" | "REPLACE") => {
            if (!latestTask?.id || !detail?.material?.lockVersion) {
                throw new Error("任务或素材版本已变化，请刷新素材详情后重试。");
            }
            return service.applyCandidate({
                applyMode,
                materialLockVersion: detail.material.lockVersion,
                taskId: latestTask.id,
                taskLockVersion: latestTask.lockVersion
            });
        },
        onSuccess: async (_, applyMode) => {
            await queryClient.refetchQueries({
                queryKey: ["knowledge", "graph-material"],
                type: "active"
            });
            messageApi.success(
                applyMode === "MERGE" ? "抽取结果已合并到知识图谱" : "抽取结果已覆盖到知识图谱"
            );
        }
    });

    if (!detail) {
        return (
            <Empty
                data-testid="knowledge-graph-material-detail-tasks-section"
                description="请选择素材查看任务摘要。"
            />
        );
    }

    return (
        <KuzhambuSpace
            className="knowledge-graph-material-task-summary-section"
            data-testid="knowledge-graph-material-detail-tasks-section"
            orientation="vertical"
            size={12}
        >
            <KuzhambuCard
                title="任务摘要"
                size="small"
                extra={
                    <KuzhambuButton
                        ariaLabel={`抽取素材 ${detail.source.title}`}
                        disabled={
                            !canExtractMaterial ||
                            extractionMutation.isPending ||
                            retryExtractionMutation.isPending ||
                            hasActiveExtractionTask(latestTask?.executionStatus)
                        }
                        icon={<RobotOutlined />}
                        loading={extractionMutation.isPending || retryExtractionMutation.isPending}
                        testId="knowledge-graph-material-detail-extract-button"
                        type="primary"
                        onClick={() => {
                            if (latestTask?.executionStatus === "FAILED") {
                                retryExtractionMutation.mutate({
                                    expectedExecutionStatus: "FAILED",
                                    taskId: latestTask.id,
                                    taskLockVersion: latestTask.lockVersion
                                });
                                return;
                            }
                            extractionMutation.mutate({ contentRef: detail.source.contentRef });
                        }}
                    >
                        {latestTask?.executionStatus === "FAILED" ? "重试" : "抽取"}
                    </KuzhambuButton>
                }
            >
                <KuzhambuDescriptions
                    ariaLabel="素材任务摘要"
                    column={3}
                    items={[
                        {
                            label: "运行中任务",
                            children: taskSummary?.activeTaskCount ?? "0"
                        },
                        {
                            label: "待处置候选",
                            children: taskSummary?.pendingReviewTaskCount ?? "0"
                        },
                        {
                            label: "失败任务",
                            children: taskSummary?.failedTaskCount ?? "0"
                        }
                    ]}
                    size="small"
                    bordered
                />
                {extractionMutation.error ? (
                    <KuzhambuAlert
                        title={
                            extractionMutation.error instanceof Error
                                ? extractionMutation.error.message
                                : "抽取任务创建失败"
                        }
                        type="error"
                        showIcon
                    />
                ) : null}
            </KuzhambuCard>

            <KuzhambuCard title="最近任务" size="small">
                {latestTask ? (
                    <KuzhambuSpace
                        className="knowledge-graph-material-task-summary-section-latest"
                        orientation="vertical"
                        size={12}
                    >
                        <KuzhambuDescriptions
                            ariaLabel="最近任务"
                            column={2}
                            items={[
                                { label: "任务编号", children: latestTask.id },
                                {
                                    label: "执行状态",
                                    children: renderTaskStatus(latestTask.executionStatus)
                                },
                                {
                                    label: "处置状态",
                                    children: renderDisposition(latestTask.disposition)
                                },
                                {
                                    label: "当前阶段",
                                    children: renderCurrentStage(latestTask.currentStage)
                                },
                                { label: "尝试次数", children: latestTask.attemptNo },
                                { label: "批次号", children: latestTask.batchId ?? "-" },
                                {
                                    label: "请求时间",
                                    children: formatTimestamp(latestTask.requestedAt)
                                },
                                {
                                    label: "完成时间",
                                    children: formatTimestamp(latestTask.completedAt)
                                },
                                {
                                    label: "抽取节点",
                                    children: String(extractedGraph?.nodes.length ?? 0)
                                },
                                {
                                    label: "抽取边",
                                    children: String(extractedGraph?.edges.length ?? 0)
                                }
                            ]}
                            size="small"
                            bordered
                        />
                        {latestTask.failureReason ? (
                            <Text type="danger">{latestTask.failureReason}</Text>
                        ) : null}
                    </KuzhambuSpace>
                ) : (
                    <Text type="secondary">暂无任务记录</Text>
                )}
            </KuzhambuCard>

            {latestTask && extractedGraph ? (
                <KuzhambuCard
                    title="抽取结果预览"
                    size="small"
                    extra={
                        latestTask.disposition === "ADOPTED_MERGE" ||
                        latestTask.disposition === "ADOPTED_REPLACE" ? (
                            <KuzhambuTag type="success">
                                {latestTask.disposition === "ADOPTED_MERGE" ? "已合并" : "已覆盖"}
                            </KuzhambuTag>
                        ) : (
                            <KuzhambuSpace size={8}>
                                <KuzhambuButton
                                    disabled={!canApplyLatestCandidate}
                                    loading={applyCandidateMutation.isPending}
                                    testId="knowledge-graph-material-detail-merge-candidate-button"
                                    type="primary"
                                    onClick={() => applyCandidateMutation.mutate("MERGE")}
                                >
                                    合并到知识图谱
                                </KuzhambuButton>
                                <KuzhambuButton
                                    danger
                                    disabled={!canApplyLatestCandidate}
                                    loading={applyCandidateMutation.isPending}
                                    testId="knowledge-graph-material-detail-replace-candidate-button"
                                    onClick={() =>
                                        confirm.danger({
                                            description:
                                                "覆盖会替换当前素材知识图谱中的节点和边，且不能自动恢复。",
                                            message: "确认以本次抽取结果覆盖知识图谱吗？",
                                            okText: "确认覆盖",
                                            title: "覆盖知识图谱",
                                            onConfirm: () =>
                                                applyCandidateMutation.mutateAsync("REPLACE")
                                        })
                                    }
                                >
                                    覆盖到知识图谱
                                </KuzhambuButton>
                            </KuzhambuSpace>
                        )
                    }
                >
                    {extractedGraphSpoList.length > 0 ? (
                        <KuzhambuGraph height={300} spoList={extractedGraphSpoList} />
                    ) : (
                        <Empty
                            description="本次抽取未生成可绘制的关系。"
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                        />
                    )}
                    {applyCandidateMutation.error ? (
                        <KuzhambuAlert
                            title={
                                applyCandidateMutation.error instanceof Error
                                    ? applyCandidateMutation.error.message
                                    : "抽取结果合并失败"
                            }
                            type="error"
                            showIcon
                        />
                    ) : null}
                </KuzhambuCard>
            ) : null}
        </KuzhambuSpace>
    );
};
