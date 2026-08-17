import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import {
    graphBatchExtractionResult,
    graphMaterialMockData,
    graphMaterialMockListRecords
} from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import { MaterialBatchActions } from "./material-batch-actions";

describe("MaterialBatchActions", () => {
    it("hides batch actions when no material is selected", () => {
        render(
            <MaterialBatchActions
                canApplyGraph
                selectedRecords={[]}
                onExtract={vi.fn()}
                onPublish={vi.fn()}
                onViewTasks={vi.fn()}
                onWithdraw={vi.fn()}
            />
        );

        expect(
            screen.queryByTestId("knowledge-graph-material-batch-actions")
        ).not.toBeInTheDocument();
    });

    it("shows partial failures after batch extraction", async () => {
        const onExtract = vi.fn().mockResolvedValue(graphBatchExtractionResult);
        render(
            <MaterialBatchActions
                canApplyGraph
                selectedRecords={[graphMaterialMockListRecords[0], graphMaterialMockListRecords[2]]}
                onExtract={onExtract}
                onPublish={vi.fn()}
                onViewTasks={vi.fn()}
                onWithdraw={vi.fn()}
            />
        );
        const user = userEvent.setup();

        await user.click(screen.getByRole("button", { name: /批量提取/u }));

        await waitFor(() => {
            expect(
                screen.getByText("部分素材处理失败，其余逐素材结果已保留。")
            ).toBeInTheDocument();
        });
        expect(screen.getByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getByText("任务已创建 #7001")).toBeInTheDocument();
        expect(screen.getByText("王祺札记 山川")).toBeInTheDocument();
        expect(screen.getByText("素材已有活动任务。")).toBeInTheDocument();
    });

    it("shows one result row for each selected material after batch publication", async () => {
        const onPublish = vi.fn().mockResolvedValue(graphMaterialMockData.batchPublicationResults);
        render(
            <MaterialBatchActions
                canApplyGraph
                selectedRecords={[
                    graphMaterialMockListRecords[0],
                    graphMaterialMockListRecords[2],
                    graphMaterialMockListRecords[3]
                ]}
                onExtract={vi.fn()}
                onPublish={onPublish}
                onViewTasks={vi.fn()}
                onWithdraw={vi.fn()}
            />
        );
        const user = userEvent.setup();

        await user.click(screen.getByRole("button", { name: /批量发布/u }));

        expect(await screen.findByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getByText("素材尚未初始化，无法发布。")).toBeInTheDocument();
        expect(screen.getByText("王祺札记 山川")).toBeInTheDocument();
        expect(screen.getByText("发布预览存在未解决冲突。")).toBeInTheDocument();
        expect(screen.getByText("明代风俗 婚礼")).toBeInTheDocument();
        expect(screen.getByText("已发布")).toBeInTheDocument();
        expect(onPublish).toHaveBeenCalledWith([
            graphMaterialMockListRecords[0],
            graphMaterialMockListRecords[2],
            graphMaterialMockListRecords[3]
        ]);
    });
});
