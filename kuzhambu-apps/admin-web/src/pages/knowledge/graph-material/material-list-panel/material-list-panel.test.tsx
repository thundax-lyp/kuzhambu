import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import type { ReactNode } from "react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { graphMaterialMockListRecords } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import { MaterialListPanel } from "./material-list-panel";

vi.mock("@/pages/knowledge/graph-material/graph-material-service", () => ({
    createBatchExtraction: vi.fn(),
    retryExtraction: vi.fn()
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
});
