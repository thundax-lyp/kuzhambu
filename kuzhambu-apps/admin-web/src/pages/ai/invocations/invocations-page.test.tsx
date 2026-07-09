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

vi.mock("@/components/kuzhambu-drawer", () => {
    const mockDrawer = ({
        children,
        open,
        title
    }: {
        children: React.ReactNode;
        open?: boolean;
        title?: React.ReactNode;
    }) =>
        open ? (
            <div>
                <h3>{title}</h3>
                {children}
            </div>
        ) : null;

    return {
        KuzhambuDrawer: mockDrawer
    };
});

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

        expect(await screen.findByRole("heading", { name: "AI 调用统计" })).toBeInTheDocument();
        expect(await screen.findByText("summary")).toBeInTheDocument();
        expect(screen.getByText("gpt-4o")).toBeInTheDocument();
        expect(screen.getByText("0.32")).toBeInTheDocument();
    });

    it("opens call detail drawer", async () => {
        renderPage();
        await screen.findByText("gpt-4o");

        fireEvent.click(screen.getByRole("button", { name: /详情/ }));

        expect(await screen.findByRole("heading", { name: "调用详情" })).toBeInTheDocument();
        expect(screen.getByText("req-1")).toBeInTheDocument();
        expect(screen.getByText("trace-1")).toBeInTheDocument();
    });
});
