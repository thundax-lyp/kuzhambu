import { ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Input } from "antd";
import { useState } from "react";
import { usePermission } from "@/auth/hooks/use-permission";
import {
    KuzhambuAlert,
    KuzhambuButton,
    KuzhambuListPage,
    KuzhambuPage,
    KuzhambuSelect
} from "@/components";
import type { KuzhambuListPageFilterField } from "@/components";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import * as service from "./graph-extraction-service";
import type {
    GraphExtractionTaskPageQuery,
    GraphExtractionTaskStateCommand
} from "./graph-extraction-service";
import {
    createGraphExtractionTaskColumns,
    graphExtractionTaskRowKey
} from "./graph-extraction-task-columns";
import type {
    GraphContentRefRecord,
    GraphExtractionTaskRecord,
    GraphTaskExecutionStatus
} from "./graph-extraction-types";
import "./graph-extraction-page.css";

const normalizeSearchParam = (value: string | null) => {
    const text = value?.trim();
    return text || undefined;
};

const readPositiveIntegerSearchParam = (value: string | null, fallback: number) => {
    const numberValue = Number(value);
    if (!Number.isInteger(numberValue) || numberValue <= 0) {
        return fallback;
    }
    return numberValue;
};

const parseContentRefsJson = (value: string | null): GraphContentRefRecord[] | undefined => {
    if (!value) {
        return undefined;
    }
    try {
        const parsedValue: unknown = JSON.parse(value);
        if (!Array.isArray(parsedValue)) {
            return undefined;
        }
        const contentRefs = parsedValue.flatMap((item) => {
            if (typeof item !== "object" || item === null) {
                return [];
            }
            const record = item as Partial<GraphContentRefRecord>;
            const contentType = record.contentType?.trim();
            const contentRefId = record.contentRefId?.trim();
            return contentType && contentRefId ? [{ contentRefId, contentType }] : [];
        });
        return contentRefs.length ? contentRefs : undefined;
    } catch {
        return undefined;
    }
};

const readContentRefsFromSearch = (
    params: URLSearchParams
): GraphContentRefRecord[] | undefined => {
    const jsonContentRefs = parseContentRefsJson(params.get("contentRefs"));
    if (jsonContentRefs) {
        return jsonContentRefs;
    }

    const contentType = normalizeSearchParam(
        params.get("contentType") ?? params.get("sourceContentType")
    );
    const contentRefId = normalizeSearchParam(
        params.get("contentRefId") ?? params.get("sourceContentId")
    );
    return contentType && contentRefId ? [{ contentRefId, contentType }] : undefined;
};

const compactTaskQuery = (query: GraphExtractionTaskPageQuery): GraphExtractionTaskPageQuery =>
    Object.fromEntries(
        Object.entries(query).filter(([, value]) => value !== undefined)
    ) as GraphExtractionTaskPageQuery;

const EXECUTION_STATUS_OPTIONS = [
    { label: "待执行", value: "PENDING" },
    { label: "运行中", value: "RUNNING" },
    { label: "已成功", value: "SUCCEEDED" },
    { label: "已失败", value: "FAILED" },
    { label: "已取消", value: "CANCELLED" }
];

const DISPOSITION_OPTIONS = [
    { label: "待采纳", value: "PENDING" },
    { label: "合并采纳", value: "ADOPTED_MERGE" },
    { label: "替换采纳", value: "ADOPTED_REPLACE" },
    { label: "已丢弃", value: "DISCARDED" },
    { label: "已替代", value: "SUPERSEDED" }
];

type GraphExtractionTaskFilters = Pick<
    GraphExtractionTaskPageQuery,
    "disposition" | "executionStatus" | "keyword"
>;

const createTaskStateCommand = (
    task: GraphExtractionTaskRecord,
    expectedExecutionStatus: GraphTaskExecutionStatus
): GraphExtractionTaskStateCommand => ({
    expectedExecutionStatus,
    sourceTaskId: task.taskId || task.id,
    taskId: task.taskId || task.id,
    taskLockVersion: task.lockVersion
});

const readTaskQueryFromSearch = (): GraphExtractionTaskPageQuery => {
    const defaultQuery: GraphExtractionTaskPageQuery = {
        groupBy: "NONE",
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    };
    if (typeof window === "undefined") {
        return defaultQuery;
    }
    const params = new URLSearchParams(window.location.search);
    return compactTaskQuery({
        ...defaultQuery,
        batchId: normalizeSearchParam(params.get("batchId")),
        contentRefs: readContentRefsFromSearch(params),
        executionStatus: normalizeSearchParam(
            params.get("executionStatus")
        ) as GraphTaskExecutionStatus,
        keyword: normalizeSearchParam(params.get("keyword")),
        pageNo: readPositiveIntegerSearchParam(params.get("pageNo"), DEFAULT_PAGE_NO),
        pageSize: readPositiveIntegerSearchParam(params.get("pageSize"), DEFAULT_PAGE_SIZE)
    });
};

export const GraphExtractionPage = () => {
    const { message } = App.useApp();
    const queryClient = useQueryClient();
    const canViewGraph = usePermission("knowledge:graph:view");
    const canEditGraph = usePermission("knowledge:graph:edit");
    const [taskQuery, setTaskQuery] = useState<GraphExtractionTaskPageQuery>(() =>
        readTaskQueryFromSearch()
    );
    const [filters, setFilters] = useState<GraphExtractionTaskFilters>(() => ({
        disposition: taskQuery.disposition,
        executionStatus: taskQuery.executionStatus,
        keyword: taskQuery.keyword
    }));
    const canUseTaskQueue = canViewGraph;

    const taskPageQuery = useQuery({
        enabled: canUseTaskQueue,
        queryFn: () => service.pageTasks(taskQuery),
        queryKey: ["knowledge", "graph-extraction", "tasks", taskQuery],
        retry: false
    });
    const retryTaskMutation = useMutation({
        mutationFn: (task: GraphExtractionTaskRecord) =>
            service.retryTask(createTaskStateCommand(task, "FAILED")),
        onSuccess: async (result) => {
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "graph-extraction", "tasks"]
            });
            if (result.conflict) {
                message.warning(result.conflict.message);
                return;
            }
            message.success("任务已重试");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "任务重试失败");
        }
    });
    const tasks = taskPageQuery.data?.records || [];
    const taskTotalCount = taskPageQuery.data?.totalCount || 0;
    const hasActiveFilters = Boolean(
        taskQuery.keyword || taskQuery.executionStatus || taskQuery.disposition
    );
    const filterFields: KuzhambuListPageFilterField[] = [
        {
            label: "素材标题",
            name: "keyword",
            render: () => (
                <Input
                    allowClear
                    placeholder="请输入素材标题"
                    value={filters.keyword}
                    onChange={(event) =>
                        setFilters((currentFilters) => ({
                            ...currentFilters,
                            keyword: event.target.value || undefined
                        }))
                    }
                />
            )
        },
        {
            label: "运行状态",
            name: "executionStatus",
            render: () => (
                <KuzhambuSelect
                    allowClear
                    options={EXECUTION_STATUS_OPTIONS}
                    value={filters.executionStatus}
                    onChange={(executionStatus) =>
                        setFilters((currentFilters) => ({ ...currentFilters, executionStatus }))
                    }
                />
            )
        },
        {
            label: "采纳状态",
            name: "disposition",
            render: () => (
                <KuzhambuSelect
                    allowClear
                    options={DISPOSITION_OPTIONS}
                    value={filters.disposition}
                    onChange={(disposition) =>
                        setFilters((currentFilters) => ({ ...currentFilters, disposition }))
                    }
                />
            )
        }
    ];
    if (!canUseTaskQueue) {
        return (
            <KuzhambuPage
                className="graph-extraction-page knowledge-graph-extraction-page"
                description="需要知识图谱查看权限。"
                title="知识抽取"
            >
                <KuzhambuAlert title="无权查看知识抽取任务" type="warning" showIcon />
            </KuzhambuPage>
        );
    }

    return (
        <KuzhambuListPage
            description="查看 Knowledge 抽取任务的运行状态和结果。"
            enableFilter
            filterActive={hasActiveFilters}
            filterFields={filterFields}
            pageActions={
                <KuzhambuButton
                    icon={<ReloadOutlined />}
                    loading={taskPageQuery.isFetching}
                    testId="knowledge-graph-extraction-refresh-button"
                    onClick={() => {
                        void taskPageQuery.refetch();
                    }}
                >
                    刷新
                </KuzhambuButton>
            }
            pageClassName="graph-extraction-page knowledge-graph-extraction-page"
            rowKey={graphExtractionTaskRowKey}
            subjectName="知识抽取任务"
            title="知识抽取"
            ariaLabel="知识抽取任务列表"
            columns={createGraphExtractionTaskColumns({
                canRetry: canEditGraph,
                retryingTaskId: retryTaskMutation.variables
                    ? String(retryTaskMutation.variables.taskId)
                    : null,
                onRetry: (task) => retryTaskMutation.mutate(task)
            })}
            dataSource={tasks}
            loading={taskPageQuery.isLoading}
            locale={{
                emptyText: taskPageQuery.isError
                    ? "任务列表加载失败，请确认权限和接口状态。"
                    : "暂无抽取任务"
            }}
            pagination={{
                current: taskQuery.pageNo ?? DEFAULT_PAGE_NO,
                pageSize: taskQuery.pageSize ?? DEFAULT_PAGE_SIZE,
                showTotal: (total) => `共 ${total} 个任务`,
                total: taskTotalCount,
                onChange: (pageNo, pageSize) =>
                    setTaskQuery((currentQuery) => ({ ...currentQuery, pageNo, pageSize }))
            }}
            onFilterApply={() =>
                setTaskQuery((currentQuery) => ({
                    ...currentQuery,
                    ...filters,
                    pageNo: DEFAULT_PAGE_NO
                }))
            }
            onFilterReset={() => {
                setFilters({});
                setTaskQuery({
                    groupBy: "NONE",
                    pageNo: DEFAULT_PAGE_NO,
                    pageSize: taskQuery.pageSize ?? DEFAULT_PAGE_SIZE
                });
            }}
        />
    );
};
