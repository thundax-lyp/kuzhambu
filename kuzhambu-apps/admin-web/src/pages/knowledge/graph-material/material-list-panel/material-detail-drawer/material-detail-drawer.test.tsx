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
} from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import { MaterialDetailDrawer } from "./material-detail-drawer";

const drawerActionMocks = {
    onClose: vi.fn()
};

vi.mock("@/pages/knowledge/graph-material/graph-material-service", () => ({
    createExtraction: vi.fn(),
    getMaterial: vi.fn(),
    precheckDeletion: vi.fn(),
    previewPublication: vi.fn(),
    previewWithdrawal: vi.fn(),
    publishMaterial: vi.fn(),
    withdrawMaterial: vi.fn()
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
        vi.mocked(service.precheckDeletion).mockResolvedValue({ executable: true });
        vi.mocked(service.previewPublication).mockResolvedValue({
            edges: [],
            issues: [],
            materialLockVersion: "4",
            materialRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
            nodes: [],
            previewToken: "preview-token",
            publishable: true
        });
        vi.mocked(service.previewWithdrawal).mockResolvedValue({});
        vi.mocked(service.publishMaterial).mockResolvedValue({
            contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
            createdEdgeCount: "0",
            createdNodeCount: "0",
            materialStatus: "PUBLISHED",
            reusedEdgeCount: "1",
            reusedNodeCount: "1",
            success: true
        });
        vi.mocked(service.withdrawMaterial).mockResolvedValue(
            graphMaterialMockDetails[1].material!
        );
    });

    it("renders the four material detail sections", async () => {
        renderDrawer();
        const user = userEvent.setup();

        expect(
            await screen.findByTestId("knowledge-graph-material-detail-overview-section")
        ).toBeInTheDocument();
        expect(await screen.findByText("素材来源")).toBeInTheDocument();

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
        expect(screen.getByText("发布预览")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "发布素材" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "撤回素材" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "删除预检" })).toBeInTheDocument();
        expect(screen.queryByText("发布变更待接入。")).not.toBeInTheDocument();
    });

    it("keeps published material draft graph read-only in the drawer section", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
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

    it("keeps publish and withdraw actions in publication changes", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        renderDrawer();
        const user = userEvent.setup();

        await user.click(screen.getByText("发布变更"));
        await user.click(screen.getByRole("button", { name: "标记冲突已解决" }));
        await user.click(screen.getByRole("button", { name: "发布素材" }));

        await waitFor(() => {
            expect(service.previewPublication).toHaveBeenCalledWith({
                contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
            });
            expect(service.publishMaterial).toHaveBeenCalledWith({
                conflictDecisions: [],
                contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                materialLockVersion: "4",
                previewToken: "preview-token"
            });
        });
        expect(screen.getByText("素材已发布")).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "撤回素材" }));

        await waitFor(() => {
            expect(service.previewWithdrawal).toHaveBeenCalledWith({
                contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
            });
            expect(service.withdrawMaterial).toHaveBeenCalledWith({
                contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                materialLockVersion: "4"
            });
        });
        expect(screen.getByText("素材已撤回")).toBeInTheDocument();
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

    it("keeps delete precheck in publication changes instead of task menus", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        renderDrawer();
        const user = userEvent.setup();

        await user.click(screen.getByText("任务"));

        expect(screen.getByTestId("knowledge-graph-material-detail-tasks-section")).toBeVisible();
        expect(screen.queryByRole("button", { name: "删除变更" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "删除任务" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "删除预检" })).not.toBeInTheDocument();

        await user.click(screen.getByText("发布变更"));
        await user.click(screen.getByRole("button", { name: "删除预检" }));

        await waitFor(() => {
            expect(service.precheckDeletion).toHaveBeenCalledWith({
                contentRef: {
                    contentRefId: "1002",
                    contentType: "SANCAI_ENTRY"
                }
            });
        });
        expect(
            screen.getByText("删除预检已生成，请在当前发布变更段确认影响。")
        ).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "删除变更" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "删除任务" })).not.toBeInTheDocument();
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
