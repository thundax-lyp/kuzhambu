import { render, screen } from "@testing-library/react";
import type {
    GraphExtractionTaskDetailRecord,
    GraphExtractionTaskRecord
} from "@/pages/knowledge/graph-extraction/graph-extraction-types";
import { TaskDispositionPanel } from "./task-disposition-panel";

const createTask = (
    overrides: Partial<GraphExtractionTaskRecord> = {}
): GraphExtractionTaskRecord => ({
    attemptNo: "1",
    candidateId: "candidate-9001",
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
    status: "SUCCEEDED",
    taskId: "8008",
    taskType: "GRAPH",
    triggerSource: "MANUAL",
    ...overrides
});

const createDetail = (
    overrides: Partial<GraphExtractionTaskRecord> = {}
): GraphExtractionTaskDetailRecord => ({
    candidate: {
        candidateId: "candidate-9001",
        diff: [],
        edges: [],
        issues: [],
        nodes: []
    },
    materialStats: null,
    relatedTasks: [],
    source: {
        contentRef: {
            contentRefId: "1001",
            contentType: "SANCAI_ENTRY"
        },
        contentType: "SANCAI_ENTRY",
        title: "三才稿件"
    },
    stages: [],
    task: createTask(overrides)
});

const queryAction = (name: string) => screen.queryByRole("button", { name });

describe("TaskDispositionPanel", () => {
    it("shows only retry for failed tasks", () => {
        render(
            <TaskDispositionPanel
                detail={createDetail({
                    disposition: null,
                    executionStatus: "FAILED",
                    status: "FAILED"
                })}
                onRetry={vi.fn()}
            />
        );

        expect(queryAction("重试")).toBeInTheDocument();
        expect(queryAction("取消")).not.toBeInTheDocument();
        expect(queryAction("合并")).not.toBeInTheDocument();
        expect(queryAction("覆盖")).not.toBeInTheDocument();
        expect(queryAction("丢弃")).not.toBeInTheDocument();
        expect(queryAction("重新抽取")).not.toBeInTheDocument();
    });

    it("shows only cancel for running tasks", () => {
        render(
            <TaskDispositionPanel
                detail={createDetail({
                    disposition: null,
                    executionStatus: "RUNNING",
                    progress: 40,
                    status: "RUNNING"
                })}
                onCancel={vi.fn()}
            />
        );

        expect(queryAction("取消")).toBeInTheDocument();
        expect(queryAction("重试")).not.toBeInTheDocument();
        expect(queryAction("合并")).not.toBeInTheDocument();
        expect(queryAction("覆盖")).not.toBeInTheDocument();
        expect(queryAction("丢弃")).not.toBeInTheDocument();
        expect(queryAction("重新抽取")).not.toBeInTheDocument();
    });

    it("shows candidate actions and regenerate for succeeded pending review tasks", () => {
        render(
            <TaskDispositionPanel
                detail={createDetail()}
                onDiscard={vi.fn()}
                onMerge={vi.fn()}
                onRegenerate={vi.fn()}
                onReplace={vi.fn()}
            />
        );

        expect(queryAction("合并")).toBeInTheDocument();
        expect(queryAction("覆盖")).toBeInTheDocument();
        expect(queryAction("丢弃")).toBeInTheDocument();
        expect(queryAction("重新抽取")).toBeInTheDocument();
        expect(queryAction("重试")).not.toBeInTheDocument();
        expect(queryAction("取消")).not.toBeInTheDocument();
    });

    it("shows only regenerate for disposed succeeded tasks", () => {
        render(
            <TaskDispositionPanel
                detail={createDetail({
                    disposition: "ADOPTED_MERGE"
                })}
                onRegenerate={vi.fn()}
            />
        );

        expect(queryAction("重新抽取")).toBeInTheDocument();
        expect(queryAction("合并")).not.toBeInTheDocument();
        expect(queryAction("覆盖")).not.toBeInTheDocument();
        expect(queryAction("丢弃")).not.toBeInTheDocument();
        expect(queryAction("重试")).not.toBeInTheDocument();
        expect(queryAction("取消")).not.toBeInTheDocument();
    });
});
