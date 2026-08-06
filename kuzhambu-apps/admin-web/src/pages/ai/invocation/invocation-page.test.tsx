import { AdminQueryProvider } from "@/query/query-client";
import { App } from "antd";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { replacePermissions } from "@/auth/permission-storage";
import { InvocationPage } from "./invocation-page";
import * as service from "./invocation-service";

vi.mock("./invocation-service", () => ({
    getInvocationSummary: vi.fn(),
    listInvocationCapabilities: vi.fn(),
    pageInvocationLogs: vi.fn()
}));

const invocationLog = {
    callId: "9001",
    batchId: null,
    scope: "classics",
    capability: "summary",
    contentType: "entry",
    contentId: "101",
    objectId: "102",
    serviceRole: "PRIMARY",
    modelId: "2001",
    modelName: "gpt-4o",
    promptVersionId: "3001",
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
            <AdminQueryProvider>
                <InvocationPage />
            </AdminQueryProvider>
        </App>
    );

describe("InvocationPage", () => {
    beforeEach(() => {
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
        vi.mocked(service.pageInvocationLogs).mockResolvedValue({
            pageNo: 1,
            pageSize: 20,
            totalPage: 1,
            count: 1,
            totalCount: 1,
            records: [invocationLog]
        });
    });

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("renders summary metrics and call records", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "调用统计" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "统计概览" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "调用记录" })).toBeInTheDocument();
        expect(await screen.findByText("摘要生成")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("tab", { name: "调用记录" }));

        expect(await screen.findByText("entry")).toBeInTheDocument();
    });

    it("opens call detail drawer from the accessible view action", async () => {
        renderPage();
        fireEvent.click(await screen.findByRole("tab", { name: "调用记录" }));
        await screen.findByText("entry");

        expect(screen.queryByText("req-1")).not.toBeInTheDocument();
        fireEvent.click(screen.getByRole("button", { name: "查看调用 9001 详情" }));

        expect(await screen.findByText("调用详情")).toBeInTheDocument();
        expect(await screen.findByText("req-1")).toBeInTheDocument();
        expect(screen.getByText("trace-1")).toBeInTheDocument();
    });

    it("shows a recoverable summary error state", async () => {
        vi.mocked(service.getInvocationSummary).mockRejectedValueOnce(
            new Error("统计服务暂不可用")
        );

        renderPage();

        expect(await screen.findByText("调用统计加载失败")).toBeInTheDocument();
        expect(screen.getByText("统计服务暂不可用")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "重试加载调用统计" })).toBeInTheDocument();
        expect(screen.queryByText("调用次数")).not.toBeInTheDocument();
    });
});
