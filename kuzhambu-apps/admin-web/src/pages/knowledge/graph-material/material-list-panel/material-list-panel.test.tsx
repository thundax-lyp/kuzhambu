import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";
import { graphMaterialMockListRecords } from "@/pages/knowledge/graph-material/__mocks__/graph-mock-data";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import { MaterialListPanel } from "./material-list-panel";

vi.mock("@/pages/knowledge/graph-material/graph-material-service", () => ({
    createBatchExtraction: vi.fn()
}));

vi.mock("./material-detail-drawer", () => ({
    ["MaterialDetailDrawer"]: ({ record }: { record: { source: { title: string } } | null }) =>
        record ? <div data-testid="material-detail-drawer-mock">{record.source.title}</div> : null
}));

const renderPanel = (children: ReactNode) => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    return render(<QueryClientProvider client={queryClient}>{children}</QueryClientProvider>);
};

describe("MaterialListPanel", () => {
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
        await user.click(screen.getByRole("button", { name: /批量抽取/u }));

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

        fireEvent.click(screen.getByRole("button", { name: /提取/u }));
        await waitFor(() => {
            expect(vi.mocked(service.createBatchExtraction).mock.calls[0]?.[0]).toEqual({
                contentRefs: [{ contentRefId: "1002", contentType: "SANCAI_ENTRY" }]
            });
        });
    });
});
