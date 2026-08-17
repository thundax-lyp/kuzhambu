import { render, screen, within } from "@testing-library/react";
import type { GraphExtractionTaskDetailRecord } from "@/pages/knowledge/graph-extraction/graph-extraction-types";
import { TaskExecutionPanel } from "./task-execution-panel";

const createTaskDetail = (): GraphExtractionTaskDetailRecord => ({
    candidate: null,
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
    stages: [
        {
            completedAt: "2026-08-17T08:03:00Z",
            inputSummary: "素材 1 篇，约 320 字",
            outputSummary: "已读取素材引用和版本",
            progress: 100,
            stageCode: "MATERIAL_RESOLVE",
            stageNo: "1",
            startedAt: "2026-08-17T08:00:00Z",
            status: "SUCCEEDED"
        },
        {
            failureReason: "AI 返回结构化 JSON 缺少 edges 字段",
            inputSummary: "使用图谱抽取 schema",
            outputSummary: "解析到候选节点 2 个",
            progress: 45,
            stageCode: "AI_EXECUTION",
            stageNo: "2",
            startedAt: "2026-08-17T08:04:00Z",
            status: "FAILED"
        }
    ],
    task: {
        attemptNo: "2",
        completedAt: "2026-08-17T08:06:00Z",
        currentStage: "AI_EXECUTION",
        disposition: "PENDING",
        executionStatus: "FAILED",
        failureReason: "结构化结果校验失败",
        id: "8008",
        lockVersion: "2",
        materialRef: {
            contentRefId: "1001",
            contentType: "SANCAI_ENTRY"
        },
        progress: 45,
        requestedAt: "2026-08-17T08:00:00Z",
        resultSummary: {
            edgeCount: 0,
            nodeCount: 2,
            warningCount: 1
        },
        status: "FAILED",
        taskId: "8008",
        taskType: "GRAPH",
        triggerSource: "QUALITY_REPORT"
    }
});

describe("TaskExecutionPanel", () => {
    it("renders task and stage progress with summaries and time fields", () => {
        render(<TaskExecutionPanel detail={createTaskDetail()} />);

        const panel = screen.getByTestId(
            "knowledge-graph-extraction-task-detail-execution-section"
        );

        expect(within(panel).getByText("整体进度")).toBeInTheDocument();
        expect(within(panel).getAllByText("45%").length).toBeGreaterThan(0);
        expect(within(panel).getByText("节点 2，关系 0，告警 1")).toBeInTheDocument();
        expect(within(panel).getAllByText("1. 素材准备").length).toBeGreaterThan(0);
        expect(within(panel).getAllByText("2. AI 执行").length).toBeGreaterThan(0);
        expect(within(panel).getByText("素材 1 篇，约 320 字")).toBeInTheDocument();
        expect(within(panel).getByText("解析到候选节点 2 个")).toBeInTheDocument();
        expect(within(panel).getAllByText("开始时间").length).toBeGreaterThan(0);
        expect(within(panel).getAllByText("完成时间").length).toBeGreaterThan(0);
    });

    it("renders failure reasons without exposing full source body or prompt text", () => {
        const detail = {
            ...createTaskDetail(),
            stages: createTaskDetail().stages.map((stage) => ({
                ...stage,
                fullSourceBody: "完整正文：这里是绝不应该出现在执行过程里的正文",
                promptText: "系统提示词：抽取所有人物关系"
            })),
            task: {
                ...createTaskDetail().task,
                fullSourceBody: "完整正文：任务级正文",
                promptText: "系统提示词：任务级提示词"
            }
        } as GraphExtractionTaskDetailRecord;

        render(<TaskExecutionPanel detail={detail} />);

        const panel = screen.getByTestId(
            "knowledge-graph-extraction-task-detail-execution-section"
        );

        expect(within(panel).getByText("结构化结果校验失败")).toBeInTheDocument();
        expect(within(panel).getByText("AI 返回结构化 JSON 缺少 edges 字段")).toBeInTheDocument();
        expect(within(panel).queryByText(/完整正文/)).not.toBeInTheDocument();
        expect(within(panel).queryByText(/系统提示词/)).not.toBeInTheDocument();
    });
});
