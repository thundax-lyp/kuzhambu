import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Button, Card, Empty, Typography } from "antd";
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
    GraphExtractionTriggerSource,
    GraphExtractionTaskPageQuery,
    GraphExtractionTaskRecord
} from "./graph-extraction-types";
import "./graph-extraction-page.css";

const { Paragraph, Text, Title } = Typography;

const QUALITY_TRIGGER_SOURCE: GraphExtractionTriggerSource = "QUALITY_REPORT";
const MANUAL_TRIGGER_SOURCE: GraphExtractionTriggerSource = "MANUAL";

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

    return (
        <KuzhambuPage
            className="graph-extraction-page knowledge-graph-extraction-page"
            description="统一管理 Knowledge 抽取任务、候选结果和正式应用动作。"
            eyebrow="Knowledge / Graph Extraction"
            title="知识抽取任务"
        >
            <KuzhambuSpace
                orientation="vertical"
                size={16}
                className="knowledge-graph-extraction-layout"
            >
                <Alert
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
                        <Alert
                            message={
                                createTriggerSource === QUALITY_TRIGGER_SOURCE
                                    ? "当前为质量结果触发模式"
                                    : "当前为手工触发模式"
                            }
                            type={
                                createTriggerSource === QUALITY_TRIGGER_SOURCE ? "warning" : "info"
                            }
                            showIcon
                        />
                        <Button onClick={() => setCreateTriggerSource(MANUAL_TRIGGER_SOURCE)}>
                            切换为手工触发
                        </Button>
                        <Button onClick={() => setCreateTriggerSource(QUALITY_TRIGGER_SOURCE)}>
                            切换为质量结果触发
                        </Button>
                    </KuzhambuSpace>
                    <GraphExtractionCreate
                        canEdit={canEditGraph}
                        creatingTaskType={createTaskMutation.variables?.taskType || null}
                        latestCreatedTask={latestCreatedTask}
                        onCreate={createTaskMutation.mutate}
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
                                loading={taskPageQuery.isLoading}
                                tasks={tasks}
                                onApply={applyTask}
                                onOpenDetail={openTaskDetail}
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
