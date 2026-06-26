import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { GraphExtractionTaskTable } from "./graph-extraction-task-table";

const TASK = {
    aiCandidateId: 7001,
    batchJobId: 1001,
    replaceUnconfirmedOnly: true,
    selectionScopeJson: '{"sourceContentIds":[1001,1002]}',
    sourceContentId: 1001,
    sourceContentType: "SANCAI_ENTRY",
    status: "SUCCEEDED",
    taskId: "8008",
    taskType: "GRAPH" as const,
    triggerSource: "QUALITY_REPORT" as const
};

describe("GraphExtractionTaskTable", () => {
    it("renders batch fields and dispatches regenerate / cancel actions", () => {
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

        expect(screen.getByText("批次号")).toBeInTheDocument();
        expect(screen.getAllByText("1001")).toHaveLength(2);
        expect(screen.getByText("QUALITY_REPORT")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: /查\s*看/u }));
        fireEvent.click(screen.getByRole("button", { name: "重生成" }));
        fireEvent.click(screen.getByRole("button", { name: "取消批任务" }));
        fireEvent.click(screen.getByRole("button", { name: /应\s*用/u }));

        expect(onOpenDetail).toHaveBeenCalledWith(TASK);
        expect(onRegenerate).toHaveBeenCalledWith(TASK);
        expect(onCancelBatch).toHaveBeenCalledWith(TASK);
        expect(onApply).toHaveBeenCalledWith(TASK);
    });
});
