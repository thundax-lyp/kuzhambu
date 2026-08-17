import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import type { GraphExtractionTaskRecord } from "../graph-extraction-types";
import { GraphExtractionTaskTable } from "./graph-extraction-task-table";

const TASK: GraphExtractionTaskRecord = {
    attemptNo: "1",
    aiCandidateId: "7001",
    batchJobId: "1001",
    batchId: "1001",
    currentStage: "CANDIDATE_READY",
    disposition: "PENDING",
    executionStatus: "SUCCEEDED",
    id: "8008",
    lockVersion: "1",
    materialRef: {
        contentRefId: "1001",
        contentType: "SANCAI_ENTRY"
    },
    progress: 100,
    replaceUnconfirmedOnly: true,
    resultSummary: {
        edgeCount: 3,
        nodeCount: 2,
        warningCount: 1
    },
    selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
    sourceContentId: "1001",
    sourceContentType: "SANCAI_ENTRY",
    status: "SUCCEEDED",
    taskId: "8008",
    taskType: "GRAPH" as const,
    triggerSource: "QUALITY_REPORT" as const
};

const RUNNING_TASK: GraphExtractionTaskRecord = {
    ...TASK,
    aiCandidateId: null,
    batchJobId: "1002",
    batchId: "1002",
    currentStage: "EXTRACTING",
    disposition: "PENDING",
    executionStatus: "RUNNING",
    id: "8009",
    materialRef: {
        contentRefId: "1002",
        contentType: "SANCAI_ENTRY"
    },
    progress: 45,
    sourceContentId: "1002",
    status: "RUNNING",
    taskId: "8009"
};

const FAILED_TASK: GraphExtractionTaskRecord = {
    ...TASK,
    aiCandidateId: null,
    batchJobId: null,
    batchId: "1003",
    currentStage: "CANDIDATE_BUILD",
    disposition: "PENDING",
    errorMessage: "模型返回空结果",
    executionStatus: "FAILED",
    failureReason: "候选解析失败",
    id: "8010",
    materialRef: {
        contentRefId: "1003",
        contentType: "SANCAI_ENTRY"
    },
    progress: 60,
    resultSummary: null,
    sourceContentId: "1003",
    status: "FAILED",
    taskId: "8010"
};

describe("GraphExtractionTaskTable", () => {
    it("renders material task columns and dispatches available actions", () => {
        const onApply = vi.fn();
        const onCancelBatch = vi.fn();
        const onOpenDetail = vi.fn();
        const onRegenerate = vi.fn();

        render(
            <GraphExtractionTaskTable
                canApply
                canEdit
                tasks={[TASK]}
                onApply={onApply}
                onCancelBatch={onCancelBatch}
                onOpenDetail={onOpenDetail}
                onRegenerate={onRegenerate}
            />
        );

        expect(screen.getAllByText("任务素材")[0]).toBeInTheDocument();
        expect(screen.getAllByText("运行状态")[0]).toBeInTheDocument();
        expect(screen.getAllByText("采纳状态")[0]).toBeInTheDocument();
        expect(screen.getAllByText("阶段")[0]).toBeInTheDocument();
        expect(screen.getAllByText("输入摘要")[0]).toBeInTheDocument();
        expect(screen.getAllByText("结果摘要")[0]).toBeInTheDocument();
        expect(screen.getAllByText("失败原因")[0]).toBeInTheDocument();
        expect(screen.getAllByText("关联任务")[0]).toBeInTheDocument();
        expect(screen.getAllByText("清理时间")[0]).toBeInTheDocument();
        expect(screen.getByText("SANCAI_ENTRY / 1001")).toBeInTheDocument();
        expect(screen.getByText("CANDIDATE_READY / 100%")).toBeInTheDocument();
        expect(screen.getByText("节点 2，关系 3，告警 1")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: "查看任务 8008" }));
        fireEvent.click(screen.getByRole("button", { name: "应用任务 8008" }));

        expect(onOpenDetail).toHaveBeenCalledWith(TASK);
        expect(onApply).toHaveBeenCalledWith(TASK);
        expect(onRegenerate).not.toHaveBeenCalled();
        expect(onCancelBatch).not.toHaveBeenCalled();
    });

    it("shows cancel for running tasks and retry for failed tasks", () => {
        const onApply = vi.fn();
        const onCancelBatch = vi.fn();
        const onOpenDetail = vi.fn();
        const onRegenerate = vi.fn();

        render(
            <GraphExtractionTaskTable
                canApply
                canEdit
                tasks={[RUNNING_TASK, FAILED_TASK]}
                onApply={onApply}
                onCancelBatch={onCancelBatch}
                onOpenDetail={onOpenDetail}
                onRegenerate={onRegenerate}
            />
        );

        expect(screen.getByRole("button", { name: "取消任务 8009" })).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "重试任务 8009" })).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: "重试任务 8010" })).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "取消任务 8010" })).not.toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: "取消任务 8009" }));
        fireEvent.click(screen.getByRole("button", { name: "重试任务 8010" }));

        expect(onCancelBatch).toHaveBeenCalledWith(RUNNING_TASK);
        expect(onRegenerate).toHaveBeenCalledWith(FAILED_TASK);
    });

    it("uses id when taskId is absent from Knowledge task records", () => {
        const onApply = vi.fn();
        const onCancelBatch = vi.fn();
        const onOpenDetail = vi.fn();
        const onRegenerate = vi.fn();
        const taskWithoutLegacyTaskId = {
            ...TASK,
            taskId: null
        };

        render(
            <GraphExtractionTaskTable
                canApply
                canEdit
                applyingTaskId="8008"
                tasks={[taskWithoutLegacyTaskId]}
                onApply={onApply}
                onCancelBatch={onCancelBatch}
                onOpenDetail={onOpenDetail}
                onRegenerate={onRegenerate}
            />
        );

        expect(screen.getByText("任务 8008")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查看任务 8008" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "应用任务 8008" })).toBeDisabled();
    });
});
