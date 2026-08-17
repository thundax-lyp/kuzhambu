import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { graphMaterialMockDetails } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import type { GraphMaterialDrawerSection } from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialDetailDrawer } from "./material-detail-drawer";

const renderDrawer = () => {
    const Wrapper = () => {
        const [activeSection, setActiveSection] = useState<GraphMaterialDrawerSection>("OVERVIEW");
        const detail = graphMaterialMockDetails[1];
        return (
            <MaterialDetailDrawer
                activeSection={activeSection}
                detail={detail}
                material={detail.material ?? null}
                open
                onClose={vi.fn()}
                onRetry={vi.fn()}
                onSectionChange={setActiveSection}
            />
        );
    };
    return render(
        <MemoryRouter>
            <Wrapper />
        </MemoryRouter>
    );
};

describe("MaterialDetailDrawer", () => {
    it("renders the four material detail sections", async () => {
        renderDrawer();
        const user = userEvent.setup();

        expect(
            screen.getByTestId("knowledge-graph-material-detail-overview-section")
        ).toBeInTheDocument();
        expect(screen.getByText("素材来源")).toBeInTheDocument();

        await user.click(screen.getByText("草稿图谱"));
        expect(
            screen.getByTestId("knowledge-graph-material-detail-draft-graph-section")
        ).toBeInTheDocument();

        await user.click(screen.getByText("任务"));
        expect(
            screen.getByTestId("knowledge-graph-material-detail-tasks-section")
        ).toBeInTheDocument();
        expect(screen.getByText("任务摘要")).toBeInTheDocument();
        expect(screen.queryByText("任务摘要待接入。")).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "新增对象" })).not.toBeInTheDocument();

        await user.click(screen.getByText("发布变更"));
        expect(
            screen.getByTestId("knowledge-graph-material-detail-publication-changes-section")
        ).toBeInTheDocument();
    });
});
