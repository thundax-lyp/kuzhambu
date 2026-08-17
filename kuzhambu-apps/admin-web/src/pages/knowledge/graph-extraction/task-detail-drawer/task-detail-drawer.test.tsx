import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import type {
    GraphExtractionTaskDetailRecord,
    GraphExtractionTaskDrawerSection
} from "../graph-extraction-types";
import { TaskDetailDrawer } from "./task-detail-drawer";

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
    stages: [],
    task: {
        attemptNo: "1",
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
        selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
        status: "SUCCEEDED",
        taskId: "8008",
        taskType: "GRAPH",
        triggerSource: "QUALITY_REPORT"
    }
});

const TaskDetailDrawerHarness = () => {
    const [activeSection, setActiveSection] =
        useState<GraphExtractionTaskDrawerSection>("OVERVIEW");

    return (
        <TaskDetailDrawer
            activeSection={activeSection}
            detail={createTaskDetail()}
            open
            onClose={vi.fn()}
            onSectionChange={setActiveSection}
        />
    );
};

describe("TaskDetailDrawer", () => {
    it("makes all four task detail sections accessible", async () => {
        const user = userEvent.setup();

        render(<TaskDetailDrawerHarness />);

        expect(
            screen.getByTestId("knowledge-graph-extraction-task-detail-overview-section")
        ).toBeInTheDocument();
        expect(screen.getByText('{"sourceContentIds":[1001,1002]}')).toBeInTheDocument();

        await user.click(screen.getByText("执行过程"));
        expect(
            screen.getByTestId("knowledge-graph-extraction-task-detail-execution-section")
        ).toBeInTheDocument();

        await user.click(screen.getByText("候选预览"));
        expect(
            screen.getByTestId("knowledge-graph-extraction-task-detail-candidate-section")
        ).toBeInTheDocument();
        expect(screen.getByText("候选不可用")).toBeInTheDocument();

        await user.click(screen.getByText("候选处置"));
        expect(
            screen.getByTestId("knowledge-graph-extraction-task-detail-disposition-section")
        ).toBeInTheDocument();
    });
});
