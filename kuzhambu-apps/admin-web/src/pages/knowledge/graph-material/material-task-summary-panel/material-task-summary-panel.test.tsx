import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes, useLocation } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { graphMaterialMockDetails } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialTaskSummaryPanel } from "./material-task-summary-panel";

const LocationProbe = () => {
    const location = useLocation();
    return <span data-testid="location-probe">{location.pathname + location.search}</span>;
};

const renderPanel = (detail: GraphMaterialDetailRecord | null) => {
    render(
        <MemoryRouter initialEntries={["/knowledge/graph-material"]}>
            <Routes>
                <Route
                    path="*"
                    element={
                        <>
                            <MaterialTaskSummaryPanel detail={detail} />
                            <LocationProbe />
                        </>
                    }
                />
            </Routes>
        </MemoryRouter>
    );
};

describe("MaterialTaskSummaryPanel", () => {
    it("renders task summary without draft editing controls", () => {
        renderPanel(graphMaterialMockDetails[3]);

        expect(screen.getByTestId("knowledge-graph-material-detail-tasks-section")).toBeVisible();
        expect(screen.getByText("任务摘要")).toBeInTheDocument();
        expect(screen.getByText("运行中任务")).toBeInTheDocument();
        expect(screen.getByText("最近任务")).toBeInTheDocument();
        expect(screen.getByText("7002")).toBeInTheDocument();
        expect(screen.getByText("运行中")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "新增对象" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "抽取草稿" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "导入草稿" })).not.toBeInTheDocument();
    });

    it("navigates to extraction tasks with material content reference", () => {
        renderPanel(graphMaterialMockDetails[1]);

        fireEvent.click(screen.getByRole("button", { name: /查看任务/u }));

        const url = screen.getByTestId("location-probe").textContent ?? "";
        expect(url).toMatch(/^\/knowledge\/graph-extraction\?/u);
        const params = new URLSearchParams(url.split("?")[1]);
        expect(JSON.parse(params.get("contentRefs") || "[]")).toEqual([
            { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
        ]);
    });
});
