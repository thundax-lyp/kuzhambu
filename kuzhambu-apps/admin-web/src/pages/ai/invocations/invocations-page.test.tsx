import { QueryClientProvider } from "@tanstack/react-query";
import { App } from "antd";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { queryClient } from "@/query/query-client";
import { InvocationsPage } from "./invocations-page";
import * as service from "./invocations-service";

vi.mock("./invocations-service", () => ({
    getInvocationSummary: vi.fn(),
    listInvocationCapabilities: vi.fn(),
    pageInvocationCalls: vi.fn()
}));

const callRecord = {
    callId: 9001,
    batchId: null,
    scope: "classics",
    capability: "summary",
    contentType: "entry",
    contentId: 101,
    objectId: 102,
    serviceRole: "PRIMARY",
    modelId: 2001,
    modelName: "gpt-4o",
    promptVersionId: 3001,
    requestId: "req-1",
    traceId: "trace-1",
    status: "SUCCEEDED",
    streamUsed: true,
    streamCompleted: true,
    fallbackUsed: false,
    latencyMs: 120,
    inputTokens: 10,
    outputTokens: 20,
    costAmount: "0.02",
    failureStage: null,
    resultFormat: "JSON",
    errorType: null,
    errorMessage: null,
    warningsJson: '["minor"]',
    requestedAt: "2026-07-01T00:00:00.000Z",
    completedAt: "2026-07-01T00:00:01.000Z"
};

const renderPage = () =>
    render(
        <App>
            <QueryClientProvider client={queryClient}>
                <InvocationsPage />
            </QueryClientProvider>
        </App>
    );

describe("InvocationsPage", () => {
    beforeEach(() => {
        queryClient.clear();
        replacePermissions(["ai:invocation:view"]);
        vi.mocked(service.listInvocationCapabilities).mockResolvedValue([
            {
                capability: "summary",
                name: "摘要生成",
                requiredTags: ["chat"],
                outputMode: "JSON",
                enabled: true,
                priority: 1
            }
        ]);
        vi.mocked(service.getInvocationSummary).mockResolvedValue({
            periodStart: "2026-07-01T00:00:00.000Z",
            periodEnd: "2026-07-02T00:00:00.000Z",
            invocationCount: 12,
            succeededInvocationCount: 10,
            failedInvocationCount: 2,
            avgLatencyMs: 120,
            totalCostAmount: "0.32",
            topCapabilities: [{ capability: "summary", invocationCount: 12 }]
        });
        vi.mocked(service.pageInvocationCalls).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [callRecord]
        });
    });

    afterEach(() => {
        cleanup();
        queryClient.clear();
        vi.clearAllMocks();
    });

    it("renders summary metrics and call records", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "调用统计" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "统计概览" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "调用记录" })).toBeInTheDocument();
        expect(await screen.findByText("摘要生成")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("tab", { name: "调用记录" }));

        expect(screen.getByText("entry")).toBeInTheDocument();
    });

    it("expands call detail by clicking call row", async () => {
        renderPage();
        fireEvent.click(await screen.findByRole("tab", { name: "调用记录" }));
        await screen.findByText("entry");

        expect(screen.queryByText("req-1")).not.toBeInTheDocument();
        fireEvent.click(screen.getByText("entry"));

        expect(await screen.findByText("req-1")).toBeInTheDocument();
        expect(screen.getByText("trace-1")).toBeInTheDocument();
    });
});
