import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { App as AntdApp } from "antd";
import { clearPermissions, replacePermissions } from "@/auth/permission-storage";
import { GraphExtractionPage } from "./graph-extraction-page";

const serviceState = vi.hoisted(() => ({
    executionStatus: "SUCCEEDED"
}));

const serviceMocks = vi.hoisted(() => ({
    pageTasks: vi.fn(async (query?: { groupBy?: string }) => ({
        count: query?.groupBy === "MATERIAL" ? 2 : 1,
        pageNo: 1,
        pageSize: 20,
        records: [
            {
                attemptNo: "1",
                currentStage: "CANDIDATE_READY",
                disposition: "PENDING",
                executionStatus: serviceState.executionStatus,
                id: "8008",
                lockVersion: "1",
                materialRef: {
                    contentRefId: "1001",
                    contentType: "SANCAI_ENTRY"
                },
                materialTitle: "三才稿件",
                progress: 100,
                status: "SUCCEEDED",
                taskId: "8008"
            }
        ],
        totalCount: query?.groupBy === "MATERIAL" ? 2 : 1,
        totalPage: 1
    })),
    retryTask: vi.fn(async () => ({ task: { status: "PENDING", taskId: "8008" } }))
}));

vi.mock("./graph-extraction-service", () => ({
    ...serviceMocks
}));

const createTestQueryClient = () =>
    new QueryClient({
        defaultOptions: {
            queries: {
                retry: false
            }
        }
    });

const renderPage = () => {
    const testQueryClient = createTestQueryClient();
    return render(
        <QueryClientProvider client={testQueryClient}>
            <AntdApp>
                <GraphExtractionPage />
            </AntdApp>
        </QueryClientProvider>
    );
};

describe("GraphExtractionPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        serviceState.executionStatus = "SUCCEEDED";
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
    });

    afterEach(() => {
        window.history.pushState({}, "", "/");
        cleanup();
    });

    it("renders the task queue as the primary page content", async () => {
        renderPage();

        expect(await screen.findByRole("heading", { name: "知识抽取" })).toBeInTheDocument();
        await waitFor(() => {
            expect(serviceMocks.pageTasks).toHaveBeenCalledWith({
                groupBy: "NONE",
                pageNo: 1,
                pageSize: 20
            });
        });
        expect(await screen.findByText("三才稿件")).toBeInTheDocument();
        expect(screen.getByText("素材标题")).toBeInTheDocument();
        expect(screen.getAllByText("运行状态")[0]).toBeInTheDocument();
        expect(screen.getAllByText("采纳状态")[0]).toBeInTheDocument();
        expect(screen.getByText("已成功")).toBeInTheDocument();
        expect(screen.getByText("待采纳")).toBeInTheDocument();
        expect(screen.queryByText("请选择左侧卷目查看稿件")).not.toBeInTheDocument();
    });

    it("refreshes the task list", async () => {
        renderPage();

        await screen.findByText("三才稿件");
        expect(serviceMocks.pageTasks).toHaveBeenCalledTimes(1);

        fireEvent.click(screen.getByTestId("knowledge-graph-extraction-refresh-button"));

        await waitFor(() => {
            expect(serviceMocks.pageTasks).toHaveBeenCalledTimes(2);
        });
    });

    it("only exposes retry for failed tasks", async () => {
        renderPage();

        expect(screen.queryByRole("button", { name: "查看任务 8008" })).not.toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "重试任务 8008" })).not.toBeInTheDocument();
    });

    it("retries a failed task and refreshes the task list", async () => {
        serviceState.executionStatus = "FAILED";
        renderPage();

        fireEvent.click(await screen.findByRole("button", { name: "重试任务 8008" }));

        await waitFor(() => {
            expect(serviceMocks.retryTask).toHaveBeenCalledWith({
                expectedExecutionStatus: "FAILED",
                sourceTaskId: "8008",
                taskId: "8008",
                taskLockVersion: "1"
            });
            expect(serviceMocks.pageTasks).toHaveBeenCalledTimes(2);
        });
    });

    it("does not render an empty action column without edit permission", async () => {
        replacePermissions(["knowledge:graph:view"]);
        renderPage();

        await screen.findByText("三才稿件");

        expect(screen.queryByRole("columnheader", { name: "操作" })).not.toBeInTheDocument();
    });

    it("does not load task data without graph queue permission", async () => {
        clearPermissions();

        renderPage();

        expect(await screen.findByText("无权查看知识抽取任务")).toBeInTheDocument();
        expect(serviceMocks.pageTasks).not.toHaveBeenCalled();
    });

    it("does not load task data with edit permission but without view permission", async () => {
        replacePermissions(["knowledge:graph:edit"]);

        renderPage();

        expect(await screen.findByText("无权查看知识抽取任务")).toBeInTheDocument();
        expect(serviceMocks.pageTasks).not.toHaveBeenCalled();
    });
});
