import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import type { ReactNode } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { graphMaterialMockListRecords } from "@/test/fixtures/graph-material-fixtures";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import { MaterialListPanel } from "./material-list-panel";

vi.mock("@/pages/knowledge/graph-material/graph-material-service", () => ({
    createBatchExtraction: vi.fn(),
    previewBatchPublication: vi.fn(),
    previewPublication: vi.fn(),
    previewWithdrawal: vi.fn(),
    publishBatch: vi.fn(),
    publishMaterial: vi.fn(),
    retryExtraction: vi.fn(),
    withdrawMaterial: vi.fn()
}));

vi.mock("./material-detail-drawer", () => ({
    ["MaterialDetailDrawer"]: ({ record }: { record: { source: { title: string } } | null }) =>
        record ? <div data-testid="material-detail-drawer-mock">{record.source.title}</div> : null
}));

const renderPanel = (children: ReactNode) => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    return render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>{children}</AntdApp>
        </QueryClientProvider>
    );
};

describe("MaterialListPanel", () => {
    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("creates batch extraction for selected materials", async () => {
        vi.mocked(service.createBatchExtraction).mockResolvedValue({
            materials: [],
            batchId: "batch-1"
        });
        const onRefreshMaterials = vi.fn(async () => undefined);
        renderPanel(
            <MaterialListPanel
                dataSource={[graphMaterialMockListRecords[0], graphMaterialMockListRecords[1]]}
                onRefreshMaterials={onRefreshMaterials}
            />
        );
        const user = userEvent.setup();

        const checkboxes = screen.getAllByRole("checkbox");
        await user.click(checkboxes[1]);
        await user.click(checkboxes[2]);
        await user.click(screen.getByTestId("knowledge-graph-material-batch-extract-button"));

        await waitFor(() => {
            expect(vi.mocked(service.createBatchExtraction).mock.calls[0]?.[0]).toEqual({
                contentRefs: [
                    { contentRefId: "1001", contentType: "SANCAI_ENTRY" },
                    { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
                ]
            });
        });
        expect(onRefreshMaterials).toHaveBeenCalledTimes(1);
    });

    it("keeps row view and extraction actions", async () => {
        vi.mocked(service.createBatchExtraction).mockResolvedValue({
            materials: [],
            batchId: "batch-1"
        });
        renderPanel(
            <MaterialListPanel
                dataSource={[graphMaterialMockListRecords[1]]}
                onRefreshMaterials={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole("button", { name: /查看素材/u }));
        expect(screen.getByTestId("material-detail-drawer-mock")).toHaveTextContent(
            "三才图会 人物一"
        );

        fireEvent.click(screen.getByRole("button", { name: "提取 三才图会 人物一" }));
        await waitFor(() => {
            expect(vi.mocked(service.createBatchExtraction).mock.calls[0]?.[0]).toEqual({
                contentRefs: [{ contentRefId: "1002", contentType: "SANCAI_ENTRY" }]
            });
        });
    });

    it("retries a failed extraction task instead of creating another task", async () => {
        const failedRecord = graphMaterialMockListRecords[2];
        const failedTask = failedRecord.latestTask!;
        vi.mocked(service.retryExtraction).mockResolvedValue(failedTask);
        renderPanel(<MaterialListPanel dataSource={[failedRecord]} onRefreshMaterials={vi.fn()} />);

        fireEvent.click(screen.getByRole("button", { name: `重试 ${failedRecord.source.title}` }));

        await waitFor(() => {
            expect(vi.mocked(service.retryExtraction)).toHaveBeenCalledWith(
                {
                    expectedExecutionStatus: "FAILED",
                    taskId: failedTask.id,
                    taskLockVersion: failedTask.lockVersion
                },
                expect.anything()
            );
        });
        expect(service.createBatchExtraction).not.toHaveBeenCalled();
    });

    it("opens an uninitialized material through its title link", () => {
        renderPanel(
            <MaterialListPanel
                dataSource={[graphMaterialMockListRecords[0]]}
                onRefreshMaterials={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole("link", { name: "打开素材 三才图会 天文一" }));

        expect(screen.getByTestId("material-detail-drawer-mock")).toHaveTextContent(
            "三才图会 天文一"
        );
    });

    it("uses distinct semantic colors for publication and extraction statuses", () => {
        renderPanel(
            <MaterialListPanel
                dataSource={[graphMaterialMockListRecords[1], graphMaterialMockListRecords[2]]}
                onRefreshMaterials={vi.fn()}
            />
        );

        expect(screen.getByText("已发布")).toHaveClass("kuzhambu-tag-success");
        expect(screen.getByText("失败")).toHaveClass("kuzhambu-tag-danger");
        expect(screen.getByText("已成功")).toHaveClass("kuzhambu-tag-success");
        expect(screen.getByText("已失败")).toHaveClass("kuzhambu-tag-danger");
    });

    it("prevents duplicate extraction for an active task", () => {
        renderPanel(
            <MaterialListPanel
                dataSource={[graphMaterialMockListRecords[3]]}
                onRefreshMaterials={vi.fn()}
            />
        );

        expect(screen.getByRole("button", { name: "提取 明代风俗 婚礼" })).toBeDisabled();
    });

    it("keeps batch publication available but disabled until the selected statuses agree", async () => {
        renderPanel(
            <MaterialListPanel
                dataSource={[graphMaterialMockListRecords[1], graphMaterialMockListRecords[3]]}
                onRefreshMaterials={vi.fn()}
            />
        );
        const user = userEvent.setup();

        const batchPublication = screen.getByTestId(
            "knowledge-graph-material-batch-publication-button"
        );
        expect(batchPublication).toBeDisabled();
        expect(batchPublication).toHaveAttribute("title", "请先选择素材");

        const checkboxes = screen.getAllByRole("checkbox");
        await user.click(checkboxes[1]);
        await user.click(checkboxes[2]);

        expect(batchPublication).toBeDisabled();
        expect(batchPublication).toHaveAttribute(
            "title",
            "仅可对状态一致且可发布或可撤回的素材执行批量操作"
        );
    });

    it("publishes a batch from direct preview records and reuses same-key matches", async () => {
        const record = graphMaterialMockListRecords[3];
        const material = record.material!;
        vi.mocked(service.previewBatchPublication).mockResolvedValue({
            materials: [
                {
                    edges: [],
                    issues: [],
                    materialLockVersion: material.lockVersion!,
                    materialRef: material.contentRef,
                    nodes: [
                        {
                            matchType: "CONFLICT",
                            matchedObjectId: "2001",
                            materialObjectId: "1001"
                        }
                    ],
                    previewToken: "preview-1004",
                    publishable: true
                }
            ]
        });
        vi.mocked(service.publishBatch).mockResolvedValue({
            materials: [
                {
                    contentRef: material.contentRef,
                    result: {
                        contentRef: material.contentRef,
                        createdEdgeCount: "0",
                        createdNodeCount: "0",
                        materialStatus: "PUBLISHED",
                        reusedEdgeCount: "0",
                        reusedNodeCount: "1",
                        success: true
                    },
                    success: true
                }
            ]
        });
        const onRefreshMaterials = vi.fn(async () => undefined);
        renderPanel(
            <MaterialListPanel dataSource={[record]} onRefreshMaterials={onRefreshMaterials} />
        );
        const user = userEvent.setup();

        await user.click(screen.getAllByRole("checkbox")[1]);
        await user.click(screen.getByTestId("knowledge-graph-material-batch-publication-button"));

        await waitFor(() => {
            expect(vi.mocked(service.publishBatch)).toHaveBeenCalledWith({
                materials: [
                    {
                        conflictDecisions: [
                            {
                                action: "REUSE_MATCH",
                                matchedObjectId: "2001",
                                materialObjectId: "1001",
                                objectType: "NODE"
                            }
                        ],
                        contentRef: material.contentRef,
                        materialLockVersion: material.lockVersion,
                        previewToken: "preview-1004"
                    }
                ]
            });
        });
        expect(onRefreshMaterials).toHaveBeenCalledTimes(1);
    });

    it("withdraws a published material after previewing it", async () => {
        const record = graphMaterialMockListRecords[1];
        vi.mocked(service.previewWithdrawal).mockResolvedValue({});
        vi.mocked(service.withdrawMaterial).mockResolvedValue(record.material!);
        const onRefreshMaterials = vi.fn(async () => undefined);
        renderPanel(
            <MaterialListPanel dataSource={[record]} onRefreshMaterials={onRefreshMaterials} />
        );

        fireEvent.click(screen.getByRole("button", { name: `撤回素材 ${record.source.title}` }));

        await waitFor(() => {
            expect(vi.mocked(service.previewWithdrawal)).toHaveBeenCalledWith({
                contentRef: record.material!.contentRef
            });
            expect(vi.mocked(service.withdrawMaterial)).toHaveBeenCalledWith({
                contentRef: record.material!.contentRef,
                materialLockVersion: record.material!.lockVersion
            });
        });
        expect(onRefreshMaterials).toHaveBeenCalledTimes(1);
    });
});
