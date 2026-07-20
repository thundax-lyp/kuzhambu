import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Card, Empty, Typography } from "antd";
import { useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { GraphExtractionCreate } from "./components/graph-extraction-create";
import { GraphExtractionTaskDetail } from "./components/graph-extraction-task-detail";
import { GraphExtractionTaskTable } from "./components/graph-extraction-task-table";
import * as service from "./graph-extraction-service";
import type {
    GraphExtractionCreateCommand,
    GraphExtractionRegenerateCommand,
    GraphExtractionTriggerSource,
    GraphExtractionTaskPageQuery,
    GraphExtractionTaskRecord
} from "./graph-extraction-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./graph-extraction-page.css";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";

const { Paragraph, Text, Title } = Typography;

const QUALITY_TRIGGER_SOURCE: GraphExtractionTriggerSource = "QUALITY_REPORT";
const MANUAL_TRIGGER_SOURCE: GraphExtractionTriggerSource = "MANUAL";
const REGENERATE_TRIGGER_SOURCE: GraphExtractionTriggerSource = "REGENERATE";
const REFINEMENT_APPLIED_TRIGGER_SOURCE: GraphExtractionTriggerSource = "REFINEMENT_APPLIED";

const readBooleanSearchParam = (value: string | null, fallback: boolean) => {
    if (value === null) {
        return fallback;
    }
    return value === "true" || value === "1";
};

const readRegenerateCommandFromSearch = (): GraphExtractionRegenerateCommand | null => {
    if (typeof window === "undefined") {
        return null;
    }
    const params = new URLSearchParams(window.location.search);
    if (params.get("regenerate") !== "1") {
        return null;
    }
    const sourceTaskId = Number(params.get("sourceTaskId"));
    if (!Number.isFinite(sourceTaskId) || sourceTaskId <= 0) {
        return null;
    }
    return {
        taskType: params.get("taskType") || "GRAPH",
        sourceTaskId,
        triggerSource: params.get("triggerSource") || REFINEMENT_APPLIED_TRIGGER_SOURCE,
        selectionScopeJson: params.get("selectionScopeJson"),
        replaceUnconfirmedOnly: readBooleanSearchParam(params.get("replaceUnconfirmedOnly"), true)
    };
};

export const GraphExtractionPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canViewGraph = hasPermission("knowledge:graph:view");
    const canEditGraph = hasPermission("knowledge:graph:edit");
    const canApplyGraph = hasPermission("knowledge:graph:apply");
    const [latestCreatedTask, setLatestCreatedTask] = useState<GraphExtractionTaskRecord | null>(
        null
    );
    const [createTriggerSource, setCreateTriggerSource] =
        useState<GraphExtractionTriggerSource>(MANUAL_TRIGGER_SOURCE);
    const [handoffRegenerateCommand] = useState<GraphExtractionRegenerateCommand | null>(() =>
        readRegenerateCommandFromSearch()
    );
    const [taskQuery] = useState<GraphExtractionTaskPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [detailTaskId, setDetailTaskId] = useState<number | null>(null);
    const [detailOpen, setDetailOpen] = useState(false);

    const taskPageQuery = useQuery({
        queryKey: ["knowledge", "graph-extraction", "tasks", taskQuery],
        queryFn: () => service.pageTasks(taskQuery),
        enabled: canViewGraph || canEditGraph,
        retry: false
    });
    const detailQuery = useQuery({
        queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId],
        queryFn: () => service.getTaskDetail({ taskId: detailTaskId || 0 }),
        enabled: detailOpen && detailTaskId !== null,
        retry: false
    });
    const createTaskMutation = useMutation({
        mutationFn: (request: GraphExtractionCreateCommand) =>
            service.addTask({
                ...request,
                triggerSource: createTriggerSource
            }),
        onSuccess: async (task) => {
            setLatestCreatedTask(task);
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "graph-extraction", "tasks"]
            });
            messageApi.success("抽取任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "抽取任务创建失败");
        }
    });
    const applyTaskMutation = useMutation({
        mutationFn: (taskId: number) => service.applyTaskCandidate({ taskId }),
        onSuccess: async (task) => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "tasks"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId]
                })
            ]);
            setLatestCreatedTask(task);
            messageApi.success("候选结果已应用");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "候选结果应用失败");
        }
    });
    const regenerateTaskMutation = useMutation({
        mutationFn: service.regenerateTask,
        onSuccess: async (task) => {
            setLatestCreatedTask(task);
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "graph-extraction", "tasks"]
            });
            messageApi.success("重生成任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "重生成任务创建失败");
        }
    });
    const cancelBatchTaskMutation = useMutation({
        mutationFn: (task: GraphExtractionTaskRecord) =>
            service.cancelBatchTask({
                batchJobId: task.batchJobId || 0,
                requestedBy: task.requestedBy
            }),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "tasks"]
                }),
                queryClient.invalidateQueries({
                    queryKey: ["knowledge", "graph-extraction", "task-detail", detailTaskId]
                })
            ]);
            messageApi.success("批任务已取消");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批任务取消失败");
        }
    });

    const tasks = taskPageQuery.data?.records || [];

    const openTaskDetail = (task: GraphExtractionTaskRecord) => {
        const taskId = Number(task.taskId);
        if (Number.isNaN(taskId)) {
            return;
        }
        setDetailTaskId(taskId);
        setDetailOpen(true);
    };

    const applyTask = (task: GraphExtractionTaskRecord) => {
        const taskId = Number(task.taskId);
        if (Number.isNaN(taskId)) {
            return;
        }
        applyTaskMutation.mutate(taskId);
    };

    const regenerateTask = (task: GraphExtractionTaskRecord) => {
        const taskId = Number(task.taskId);
        if (Number.isNaN(taskId)) {
            return;
        }
        regenerateTaskMutation.mutate({
            taskType: task.taskType || "GRAPH",
            sourceTaskId: taskId,
            triggerSource: REGENERATE_TRIGGER_SOURCE,
            selectionScopeJson: task.selectionScopeJson,
            replaceUnconfirmedOnly: task.replaceUnconfirmedOnly ?? true,
            requestedBy: task.requestedBy
        });
    };

    const regenerateFromHandoff = (request: GraphExtractionRegenerateCommand) => {
        regenerateTaskMutation.mutate(request);
    };

    const cancelBatchTask = (task: GraphExtractionTaskRecord) => {
        if (!task.batchJobId) {
            return;
        }
        cancelBatchTaskMutation.mutate(task);
    };

    return (
        <KuzhambuPage
            className="graph-extraction-page knowledge-graph-extraction-page"
            description="统一管理 Knowledge 抽取任务、候选结果和正式应用动作。"
            title="知识抽取任务"
        >
            <KuzhambuSpace
                orientation="vertical"
                size={16}
                className="knowledge-graph-extraction-layout"
            >
                <KuzhambuAlert
                    banner
                    className="knowledge-graph-extraction-banner"
                    title="本页已接通知识抽取任务创建、任务列表、详情查看和候选应用动作。"
                    type="info"
                />

                <section aria-labelledby="graph-extraction-create-section">
                    <div className="knowledge-graph-extraction-section-header">
                        <Title id="graph-extraction-create-section" level={4}>
                            创建抽取任务
                        </Title>
                        <Text type="secondary">三类抽取任务共用统一任务台账和候选应用链路。</Text>
                    </div>
                    <Paragraph className="knowledge-graph-extraction-helper">
                        当前可切换手工触发或质量结果触发。质量模式下，创建请求会统一写入
                        `QUALITY_REPORT`，便于后端任务台账追溯触发来源。
                    </Paragraph>
                    <KuzhambuSpace wrap>
                        <KuzhambuAlert
                            title={
                                createTriggerSource === QUALITY_TRIGGER_SOURCE
                                    ? "当前为质量结果触发模式"
                                    : "当前为手工触发模式"
                            }
                            type={
                                createTriggerSource === QUALITY_TRIGGER_SOURCE ? "warning" : "info"
                            }
                            showIcon
                        />
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-graph-extraction-action-button"
                            onClick={() => setCreateTriggerSource(MANUAL_TRIGGER_SOURCE)}
                        >
                            切换为手工触发
                        </KuzhambuButton>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-graph-extraction-action-button-2"
                            onClick={() => setCreateTriggerSource(QUALITY_TRIGGER_SOURCE)}
                        >
                            切换为质量结果触发
                        </KuzhambuButton>
                    </KuzhambuSpace>
                    <GraphExtractionCreate
                        canEdit={canEditGraph}
                        creatingTaskType={createTaskMutation.variables?.taskType || null}
                        latestCreatedTask={latestCreatedTask}
                        onCreate={createTaskMutation.mutate}
                        onRegenerate={regenerateFromHandoff}
                        regenerateCommand={handoffRegenerateCommand}
                        regenerating={regenerateTaskMutation.isPending}
                    />
                </section>

                <section aria-labelledby="graph-extraction-task-section">
                    <div className="knowledge-graph-extraction-section-header">
                        <Title id="graph-extraction-task-section" level={4}>
                            任务列表
                        </Title>
                        <Text type="secondary">
                            将展示任务状态、AI 候选关联、失败原因和应用时间线。
                        </Text>
                    </div>
                    <Card className="knowledge-graph-extraction-placeholder" variant="borderless">
                        {tasks.length > 0 ? (
                            <GraphExtractionTaskTable
                                applyingTaskId={applyTaskMutation.variables?.toString() || null}
                                canApply={canApplyGraph}
                                canEdit={canEditGraph}
                                cancellingBatchId={
                                    cancelBatchTaskMutation.variables?.batchJobId || null
                                }
                                loading={taskPageQuery.isLoading}
                                regeneratingTaskId={
                                    regenerateTaskMutation.variables?.sourceTaskId?.toString() ||
                                    null
                                }
                                tasks={tasks}
                                onApply={applyTask}
                                onCancelBatch={cancelBatchTask}
                                onOpenDetail={openTaskDetail}
                                onRegenerate={regenerateTask}
                            />
                        ) : (
                            <Empty
                                description="当前还没有抽取任务，可以先从上方创建任务。"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        )}
                    </Card>
                    <GraphExtractionTaskDetail
                        applying={applyTaskMutation.isPending}
                        canApply={canApplyGraph}
                        loading={detailQuery.isLoading}
                        open={detailOpen}
                        task={detailQuery.data || null}
                        onApply={() => {
                            if (detailTaskId !== null) {
                                applyTaskMutation.mutate(detailTaskId);
                            }
                        }}
                        onClose={() => setDetailOpen(false)}
                    />
                </section>
            </KuzhambuSpace>
        </KuzhambuPage>
    );
};
