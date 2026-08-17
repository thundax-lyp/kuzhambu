import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { graphMaterialMockDetails } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import type { GraphMaterialDetailRecord } from "@/pages/knowledge/graph-material/graph-material-types";
import type { GraphMaterialDrawerSection } from "@/pages/knowledge/graph-material/graph-material-types";
import { MaterialDetailDrawer } from "./material-detail-drawer";

vi.mock("@/components/kuzhambu-graph", () => ({
    ["KuzhambuGraph"]: ({ spoList }: { spoList: unknown[] }) => (
        <div data-testid="knowledge-graph-material-canvas-mock">{spoList.length} 条关系</div>
    )
}));

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

const renderSwitchableDrawer = () => {
    const Wrapper = () => {
        const [activeSection, setActiveSection] = useState<GraphMaterialDrawerSection>("OVERVIEW");
        const [detail, setDetail] = useState<GraphMaterialDetailRecord>(
            graphMaterialMockDetails[3]
        );
        const [open, setOpen] = useState(true);
        const closeMaterialDetailDrawer = () => {
            setActiveSection("OVERVIEW");
            setOpen(false);
        };
        return (
            <>
                <button
                    type="button"
                    onClick={() => {
                        setDetail(graphMaterialMockDetails[1]);
                        setOpen(true);
                    }}
                >
                    打开已发布素材
                </button>
                <MaterialDetailDrawer
                    activeSection={activeSection}
                    detail={detail}
                    material={detail.material ?? null}
                    open={open}
                    onClose={closeMaterialDetailDrawer}
                    onRetry={vi.fn()}
                    onSectionChange={setActiveSection}
                />
            </>
        );
    };
    return render(
        <MemoryRouter>
            <Wrapper />
        </MemoryRouter>
    );
};

describe("MaterialDetailDrawer", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
    });

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
        expect(screen.getByText("草稿图谱：三才图会 人物一")).toBeInTheDocument();

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

    it("keeps published material draft graph read-only in the drawer section", async () => {
        replacePermissions([
            "knowledge:graph:view",
            "knowledge:graph:edit",
            "knowledge:graph:apply"
        ]);
        renderDrawer();
        const user = userEvent.setup();

        await user.click(screen.getByText("草稿图谱"));

        expect(screen.getByText("草稿图谱：三才图会 人物一")).toBeInTheDocument();
        expect(screen.getByText("只读")).toBeInTheDocument();
        expect(screen.getByTestId("knowledge-graph-material-canvas-mock")).toHaveTextContent(
            "1 条关系"
        );
        expect(screen.queryByRole("button", { name: "新增对象" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "抽取草稿" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "导入草稿" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "撤回素材" })).not.toBeInTheDocument();
        expect(screen.queryByText("发布预览")).not.toBeInTheDocument();
    });

    it("does not keep the selected draft object state after the drawer closes", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        renderSwitchableDrawer();
        const user = userEvent.setup();

        await user.click(screen.getByText("草稿图谱"));
        await user.click(
            screen.getByTestId("knowledge-graph-material-open-object-node-1004-1-button")
        );
        expect(
            screen.getByTestId("knowledge-graph-material-draft-object-detail")
        ).toBeInTheDocument();

        await user.click(screen.getByTestId("knowledge-graph-material-detail-close-button"));
        await waitFor(() => {
            expect(
                screen.queryByTestId("knowledge-graph-material-detail-drawer")
            ).not.toBeInTheDocument();
        });

        await user.click(screen.getByRole("button", { name: "打开已发布素材" }));
        await user.click(screen.getByText("草稿图谱"));

        expect(screen.getByText("草稿图谱：三才图会 人物一")).toBeInTheDocument();
        expect(
            screen.queryByTestId("knowledge-graph-material-draft-object-detail")
        ).not.toBeInTheDocument();
    });
});
