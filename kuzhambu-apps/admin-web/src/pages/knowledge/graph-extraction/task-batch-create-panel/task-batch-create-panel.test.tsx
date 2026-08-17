import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { TaskBatchCreatePanel } from "./task-batch-create-panel";

const serviceMocks = vi.hoisted(() => ({
    createBatchExtraction: vi.fn(async () => ({
        batchId: "batch-001",
        materials: []
    }))
}));

vi.mock("../graph-extraction-service", () => ({
    createBatchExtraction: serviceMocks.createBatchExtraction
}));

const renderPanel = (props?: Partial<React.ComponentProps<typeof TaskBatchCreatePanel>>) => {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

    return render(
        <QueryClientProvider client={queryClient}>
            <AntdApp>
                <TaskBatchCreatePanel
                    canCreate
                    contentRefs={[{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }]}
                    volumeCode="vol-001"
                    volumeTitle="卷一"
                    {...props}
                />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("TaskBatchCreatePanel", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("submits selected materials with contentRefs only", async () => {
        renderPanel();

        fireEvent.click(
            screen.getByTestId("knowledge-graph-extraction-batch-create-selected-button")
        );

        await waitFor(() => {
            expect(serviceMocks.createBatchExtraction).toHaveBeenCalledWith({
                contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }]
            });
        });
        const batchCreateCalls = serviceMocks.createBatchExtraction.mock.calls as unknown as Array<
            [Record<string, unknown>]
        >;
        expect(batchCreateCalls[0]?.[0]).not.toHaveProperty("volumeCode");
    });

    it("submits whole volume with volumeCode only", async () => {
        renderPanel();

        fireEvent.click(
            screen.getByTestId("knowledge-graph-extraction-batch-create-volume-button")
        );

        await waitFor(() => {
            expect(serviceMocks.createBatchExtraction).toHaveBeenCalledWith({
                volumeCode: "vol-001"
            });
        });
        const batchCreateCalls = serviceMocks.createBatchExtraction.mock.calls as unknown as Array<
            [Record<string, unknown>]
        >;
        expect(batchCreateCalls[0]?.[0]).not.toHaveProperty("contentRefs");
    });

    it("disables unavailable batch inputs", () => {
        renderPanel({
            canCreate: false,
            contentRefs: [],
            volumeCode: undefined
        });

        expect(
            screen.getByTestId("knowledge-graph-extraction-batch-create-selected-button")
        ).toBeDisabled();
        expect(
            screen.getByTestId("knowledge-graph-extraction-batch-create-volume-button")
        ).toBeDisabled();
        expect(serviceMocks.createBatchExtraction).not.toHaveBeenCalled();
    });
});
