import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { MemoryRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import {
    graphMaterialMockDetails,
    graphMaterialMockListRecords
} from "@/test/fixtures/graph-material-fixtures";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import { MaterialDetailDrawer } from "./material-detail-drawer";

const drawerActionMocks = {
    onClose: vi.fn()
};

vi.mock("@/pages/knowledge/graph-material/graph-material-service", () => ({
    createExtraction: vi.fn(),
    retryExtraction: vi.fn(),
    getMaterial: vi.fn()
}));

vi.mock("@/components/kuzhambu-graph", () => ({
    ["KuzhambuGraph"]: ({ spoList }: { spoList: unknown[] }) => (
        <div data-testid="knowledge-graph-material-canvas-mock">{spoList.length} 条关系</div>
    )
}));

const renderDrawer = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    const Wrapper = () => {
        return (
            <MaterialDetailDrawer
                record={graphMaterialMockListRecords[1]}
                onClose={drawerActionMocks.onClose}
            />
        );
    };
    return render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <MemoryRouter>
                    <Wrapper />
                </MemoryRouter>
            </AntdApp>
        </QueryClientProvider>
    );
};

const renderSwitchableDrawer = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    const Wrapper = () => {
        const [record, setRecord] = useState(graphMaterialMockListRecords[3]);
        const [open, setOpen] = useState(true);
        const closeMaterialDetailDrawer = () => {
            setOpen(false);
        };
        return (
            <>
                <button
                    type="button"
                    onClick={() => {
                        setRecord(graphMaterialMockListRecords[1]);
                        setOpen(true);
                    }}
                >
                    打开已发布素材
                </button>
                <MaterialDetailDrawer
                    record={open ? record : null}
                    onClose={closeMaterialDetailDrawer}
                />
            </>
        );
    };
    return render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <MemoryRouter>
                    <Wrapper />
                </MemoryRouter>
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("MaterialDetailDrawer", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
        vi.clearAllMocks();
    });

    beforeEach(() => {
        vi.mocked(service.getMaterial).mockImplementation(async ({ contentRef }) => {
            const detail = graphMaterialMockDetails.find(
                (item) =>
                    item.source.contentRef.contentType === contentRef.contentType &&
                    item.source.contentRef.contentRefId === contentRef.contentRefId
            );
            if (!detail) {
                throw new Error("素材详情不存在");
            }
            return detail;
        });
    });

    it("renders the three material detail sections", async () => {
        renderDrawer();
        const user = userEvent.setup();

        expect(
            await screen.findByTestId("knowledge-graph-material-detail-overview-section")
        ).toBeInTheDocument();
        expect(await screen.findByText("素材来源")).toBeInTheDocument();

        await user.click(screen.getByText("知识图谱"));
        expect(
            await screen.findByTestId("knowledge-graph-material-detail-draft-graph-section")
        ).toBeInTheDocument();
        expect(screen.getAllByText("图谱信息").length).toBeGreaterThan(0);
        expect(screen.getByTestId("knowledge-graph-material-canvas-mock")).toHaveTextContent(
            "1 条关系"
        );

        await user.click(screen.getByText("任务"));
        expect(
            await screen.findByTestId("knowledge-graph-material-detail-tasks-section")
        ).toBeInTheDocument();
        expect(screen.getByText("任务摘要")).toBeInTheDocument();
        expect(screen.queryByText("任务摘要待接入。")).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "新增对象" })).not.toBeInTheDocument();

        expect(screen.queryByText("发布变更")).not.toBeInTheDocument();
    });

    it("disables graph mutations for published material", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        renderDrawer();
        const user = userEvent.setup();

        await user.click(screen.getByText("知识图谱"));

        expect(screen.getByTestId("knowledge-graph-material-canvas-mock")).toHaveTextContent(
            "1 条关系"
        );
        expect(screen.getByTestId("knowledge-graph-material-draft-create-button")).toBeDisabled();
        expect(screen.getByTestId("knowledge-graph-material-draft-merge-button")).toBeDisabled();
        expect(
            screen.getByTestId("knowledge-graph-material-draft-batch-delete-button")
        ).toBeDisabled();
        expect(screen.queryByText("发布预览")).not.toBeInTheDocument();
    });

    it("refreshes material detail after creating an extraction task", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.createExtraction).mockResolvedValue({
            attemptNo: "1",
            currentStage: "已提交",
            disposition: "PENDING",
            executionStatus: "PENDING",
            id: "7101",
            lockVersion: "1",
            materialRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
            progress: 0
        });
        renderDrawer();
        const user = userEvent.setup();

        await user.click(screen.getByText("任务"));
        await waitFor(() => {
            expect(service.getMaterial).toHaveBeenCalledTimes(1);
        });
        await user.click(screen.getByTestId("knowledge-graph-material-detail-extract-button"));

        await waitFor(() => {
            expect(service.getMaterial).toHaveBeenCalledTimes(2);
        });
    });

    it("refreshes all drawer detail data from the overview", async () => {
        renderDrawer();
        const user = userEvent.setup();

        await screen.findByTestId("knowledge-graph-material-detail-refresh-button");
        await user.click(screen.getByTestId("knowledge-graph-material-detail-refresh-button"));

        await waitFor(() => {
            expect(service.getMaterial).toHaveBeenCalledTimes(2);
        });
    });

    it("does not expose deletion actions in the material detail drawer", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        renderDrawer();
        const user = userEvent.setup();

        await user.click(screen.getByText("任务"));

        expect(
            await screen.findByTestId("knowledge-graph-material-detail-tasks-section")
        ).toBeVisible();
        expect(screen.queryByRole("button", { name: "删除变更" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "删除任务" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "删除预检" })).not.toBeInTheDocument();

        expect(screen.queryByText("发布变更")).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "删除变更" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "删除任务" })).not.toBeInTheDocument();
    });

    it("does not keep the merge dialog state after the drawer closes", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        renderSwitchableDrawer();
        const user = userEvent.setup();

        await user.click(screen.getByText("知识图谱"));
        await user.click(screen.getByTestId("knowledge-graph-material-draft-merge-button"));
        expect(
            screen.getByTestId("knowledge-graph-material-draft-merge-modal")
        ).toBeInTheDocument();

        await user.click(screen.getByTestId("knowledge-graph-material-detail-close-button"));
        await waitFor(() => {
            expect(
                screen.queryByTestId("knowledge-graph-material-detail-drawer")
            ).not.toBeInTheDocument();
        });

        await user.click(screen.getByRole("button", { name: "打开已发布素材" }));
        await user.click(screen.getByText("知识图谱"));

        expect(
            screen.queryByTestId("knowledge-graph-material-draft-merge-modal")
        ).not.toBeInTheDocument();
    });
});
