import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { graphMaterialMockListRecords } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import { MaterialTable } from "./material-table";

describe("MaterialTable", () => {
    it("renders uninitialized materials without enabling material open", () => {
        const onOpenMaterial = vi.fn();
        const onViewTasks = vi.fn();
        render(
            <MaterialTable
                dataSource={[graphMaterialMockListRecords[0]]}
                onOpenMaterial={onOpenMaterial}
                onViewTasks={onViewTasks}
            />
        );

        expect(screen.getByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getAllByText("未初始化/未抽取").length).toBeGreaterThan(0);
        expect(screen.getByRole("button", { name: /打开素材/u })).toBeDisabled();
    });

    it("marks statistics as refreshing while material task is running", () => {
        render(
            <MaterialTable
                dataSource={[graphMaterialMockListRecords[3]]}
                onOpenMaterial={vi.fn()}
                onViewTasks={vi.fn()}
            />
        );

        expect(screen.getByText("明代风俗 婚礼")).toBeInTheDocument();
        expect(screen.getByText("统计更新中")).toBeInTheDocument();
        expect(screen.getByText("运行中")).toBeInTheDocument();
    });

    it("disables task navigation when task permission is missing", () => {
        const onViewTasks = vi.fn();
        render(
            <MaterialTable
                canViewTasks={false}
                dataSource={[graphMaterialMockListRecords[1]]}
                onOpenMaterial={vi.fn()}
                onViewTasks={onViewTasks}
            />
        );

        const viewTaskButton = screen.getByRole("button", { name: /查看任务/u });
        expect(viewTaskButton).toBeDisabled();
        fireEvent.click(viewTaskButton);
        expect(onViewTasks).not.toHaveBeenCalled();
    });

    it("navigates to task list with contentRefs when viewing material tasks", () => {
        const onViewTasks = vi.fn();
        render(
            <MaterialTable
                dataSource={[graphMaterialMockListRecords[1]]}
                onOpenMaterial={vi.fn()}
                onViewTasks={onViewTasks}
            />
        );

        fireEvent.click(screen.getByRole("button", { name: /查看任务/u }));

        expect(onViewTasks).toHaveBeenCalledTimes(1);
        const [url] = onViewTasks.mock.calls[0] as [string];
        expect(url).toMatch(/^\/knowledge\/graph-extraction\?/u);
        const params = new URLSearchParams(url.split("?")[1]);
        expect(JSON.parse(params.get("contentRefs") || "[]")).toEqual([
            { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
        ]);
    });
});
