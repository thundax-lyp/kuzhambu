import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { graphMaterialMockDetails } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialOverviewPanel } from "./material-overview-panel";

const renderPanel = (detail: GraphMaterialDetailRecord) => {
    render(<MaterialOverviewPanel detail={detail} />);
};

describe("MaterialOverviewPanel", () => {
    it("renders source and risk for uninitialized material", () => {
        renderPanel(graphMaterialMockDetails[0]);

        expect(
            screen.getByTestId("knowledge-graph-material-detail-overview-section")
        ).toBeVisible();
        expect(screen.getByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getByText("SANCAI_ENTRY:1001")).toBeInTheDocument();
        expect(screen.getByText("未初始化")).toBeInTheDocument();
        expect(screen.getByText("素材尚未初始化")).toBeInTheDocument();
    });

    it("marks statistics as outdated when material lock version differs", () => {
        const detail: GraphMaterialDetailRecord = {
            ...graphMaterialMockDetails[1],
            material: {
                ...graphMaterialMockDetails[1].material!,
                lockVersion: "5"
            }
        };

        renderPanel(detail);

        expect(screen.getByText("统计已过期")).toBeInTheDocument();
        expect(screen.getByText("统计版本")).toBeInTheDocument();
        expect(screen.getByText("4")).toBeInTheDocument();
    });

    it("renders published material statistics and recent activity", () => {
        renderPanel(graphMaterialMockDetails[1]);

        expect(screen.getByText("已发布")).toBeInTheDocument();
        expect(screen.getByText("发布贡献")).toBeInTheDocument();
        expect(screen.getByText("162")).toBeInTheDocument();
        expect(screen.getByText(/最近发布于/u)).toBeInTheDocument();
        expect(screen.getByText("暂无风险")).toBeInTheDocument();
        expect(screen.queryByText("素材尚未初始化")).not.toBeInTheDocument();
    });
});
