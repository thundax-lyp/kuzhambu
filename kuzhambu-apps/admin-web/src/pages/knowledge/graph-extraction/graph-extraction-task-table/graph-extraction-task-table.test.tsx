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
    categoryName: "天文",
    completedAt: "2026-08-18T08:03:00Z",
    materialTitle: "三才图会稿件",
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
    it("renders task material metadata and only dispatches retry for failed tasks", () => {
        const onRetry = vi.fn();

        render(<GraphExtractionTaskTable canRetry tasks={[TASK]} onRetry={onRetry} />);

        expect(screen.getAllByText("任务素材")[0]).toBeInTheDocument();
        expect(screen.getAllByText("运行状态")[0]).toBeInTheDocument();
        expect(screen.getAllByText("采纳状态")[0]).toBeInTheDocument();
        expect(screen.getAllByText("素材分类")[0]).toBeInTheDocument();
        expect(screen.getAllByText("最后执行时间")[0]).toBeInTheDocument();
        expect(screen.getByText("三才图会稿件")).toBeInTheDocument();
        expect(screen.getByText("天文")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "查看任务 8008" })).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: "重试任务 8008" })).toBeDisabled();
        expect(onRetry).not.toHaveBeenCalled();
    });

    it("shows cancel for running tasks and retry for failed tasks", () => {
        const onRetry = vi.fn();

        render(
            <GraphExtractionTaskTable
                canRetry
                tasks={[RUNNING_TASK, FAILED_TASK]}
                onRetry={onRetry}
            />
        );

        expect(screen.getByRole("button", { name: "重试任务 8009" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "重试任务 8010" })).toBeInTheDocument();
        fireEvent.click(screen.getByRole("button", { name: "重试任务 8010" }));

        expect(onRetry).toHaveBeenCalledWith(FAILED_TASK);
    });

    it("uses id when taskId is absent from Knowledge task records", () => {
        const onRetry = vi.fn();
        const taskWithoutLegacyTaskId = {
            ...TASK,
            taskId: null
        };

        render(
            <GraphExtractionTaskTable
                canRetry
                tasks={[taskWithoutLegacyTaskId]}
                onRetry={onRetry}
            />
        );

        expect(screen.queryByRole("button", { name: "查看任务 8008" })).not.toBeInTheDocument();
    });

    it("does not expose a technical content reference when the material title is unavailable", () => {
        const onRetry = vi.fn();

        render(
            <GraphExtractionTaskTable
                tasks={[{ ...TASK, materialTitle: null }]}
                onRetry={onRetry}
            />
        );

        expect(screen.getByText("-")).toBeInTheDocument();
        expect(screen.queryByText("SANCAI_ENTRY / 1001")).not.toBeInTheDocument();
    });
});
