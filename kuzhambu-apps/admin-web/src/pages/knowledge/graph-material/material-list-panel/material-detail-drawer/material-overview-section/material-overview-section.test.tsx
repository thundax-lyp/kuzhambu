import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { graphMaterialMockDetails } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialOverviewSection } from "./material-overview-section";

const renderPanel = (detail: GraphMaterialDetailRecord) => {
    render(<MaterialOverviewSection detail={detail} />);
};

describe("MaterialOverviewSection", () => {
    it("renders source for uninitialized material", () => {
        renderPanel(graphMaterialMockDetails[0]);

        expect(
            screen.getByTestId("knowledge-graph-material-detail-overview-section")
        ).toBeVisible();
        expect(screen.getByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getByText("三才图会")).toBeInTheDocument();
        expect(screen.getByText("未初始化")).toBeInTheDocument();
    });

    it("renders uninitialized material when task summary is missing", () => {
        renderPanel({
            ...graphMaterialMockDetails[0],
            taskSummary: null
        });

        expect(screen.getByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getByText("未初始化")).toBeInTheDocument();
        expect(screen.queryByText("最近活动")).not.toBeInTheDocument();
    });

    it("renders published material node, edge, and task counts", () => {
        renderPanel(graphMaterialMockDetails[1]);

        expect(screen.getByText("已发布")).toBeInTheDocument();
        expect(screen.getByText("节点数")).toBeInTheDocument();
        expect(screen.getByText("边数")).toBeInTheDocument();
        expect(screen.getByText("任务数")).toBeInTheDocument();
        expect(screen.queryByText("最近活动")).not.toBeInTheDocument();
        expect(screen.queryByText("未初始化")).not.toBeInTheDocument();
    });
});
