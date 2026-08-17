import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MemoryRouter, useLocation } from "react-router-dom";
import { replacePermissions } from "@/auth/permission-storage";
import type { Page } from "@/types/page";
import {
    graphMaterialMockDetails,
    graphMaterialMockListRecords
} from "./__mocks__/graph-mock-data";
import { GraphMaterialPage } from "./graph-material-page";
import type { GraphMaterialListRecord } from "./graph-material-types";
import * as service from "./graph-material-service";

vi.mock("@/components/kuzhambu-graph", () => ({
    ["KuzhambuGraph"]: () => <div data-testid="knowledge-graph-material-canvas-mock" />
}));

vi.mock("./graph-material-service", () => ({
    createBatchExtraction: vi.fn(),
    getMaterial: vi.fn(),
    pageMaterials: vi.fn(),
    precheckDeletion: vi.fn(),
    previewBatchPublication: vi.fn(),
    previewBatchWithdrawal: vi.fn(),
    previewPublication: vi.fn(),
    previewWithdrawal: vi.fn(),
    publishBatch: vi.fn(),
    publishMaterial: vi.fn(),
    withdrawMaterial: vi.fn(),
    withdrawBatch: vi.fn()
}));

const toPage = (
    records: GraphMaterialListRecord[],
    pageNo = 1,
    pageSize = 20,
    totalCount = records.length
): Page<GraphMaterialListRecord> => ({
    count: records.length,
    pageNo,
    pageSize,
    records,
    totalCount,
    totalPage: Math.max(1, Math.ceil(totalCount / pageSize))
});

const renderPage = () => {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } }
    });
    const visitedLocations: string[] = [];
    const LocationProbe = () => {
        const location = useLocation();
        visitedLocations.push(`${location.pathname}${location.search}`);
        return null;
    };
    const view = render(
        <MemoryRouter initialEntries={["/knowledge/graph-materials"]}>
            <QueryClientProvider client={queryClient}>
                <GraphMaterialPage />
                <LocationProbe />
            </QueryClientProvider>
        </MemoryRouter>
    );
    return { ...view, visitedLocations };
};

describe("GraphMaterialPage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
        vi.clearAllMocks();
    });

    it("shows the material list loading state while querying pageMaterials", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockReturnValue(new Promise(() => undefined));
        const { container } = renderPage();

        expect(service.pageMaterials).toHaveBeenCalledWith({ pageNo: 1, pageSize: 20 });
        expect(screen.getByText("素材列表")).toBeInTheDocument();
        await waitFor(() => {
            expect(container.querySelector(".ant-spin-spinning")).toBeInTheDocument();
        });
    });

    it("shows table empty state when pageMaterials returns no records", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockResolvedValue(toPage([]));
        renderPage();

        expect(await screen.findByText("暂无图谱素材")).toBeInTheDocument();
        expect(screen.getByLabelText("图谱素材复合表格")).toBeInTheDocument();
    });

    it("queries pageMaterials with material filter values", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockResolvedValue(toPage(graphMaterialMockListRecords));
        renderPage();
        const user = userEvent.setup();

        await screen.findByText("三才图会 人物一");
        await user.type(screen.getByLabelText("关键字"), "人物");
        await user.type(screen.getByLabelText("分类"), "person");
        await user.type(screen.getByLabelText("卷目"), "volume-2");
        await user.click(screen.getByTestId("knowledge-graph-material-filter-submit-button"));

        await waitFor(() => {
            expect(service.pageMaterials).toHaveBeenLastCalledWith({
                categoryCode: "person",
                keyword: "人物",
                pageNo: 1,
                pageSize: 20,
                volumeCode: "volume-2"
            });
        });
    });

    it("queries pageMaterials with pagination values", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockImplementation(async (query) =>
            toPage(graphMaterialMockListRecords, query?.pageNo ?? 1, query?.pageSize ?? 20, 50)
        );
        renderPage();
        const user = userEvent.setup();

        await screen.findByText("三才图会 人物一");
        await user.click(screen.getByRole("listitem", { name: "2" }));

        await waitFor(() => {
            expect(service.pageMaterials).toHaveBeenLastCalledWith({ pageNo: 2, pageSize: 20 });
        });
    });

    it("recovers from pageMaterials error by retrying the query", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials)
            .mockRejectedValueOnce(new Error("素材服务暂不可用"))
            .mockResolvedValueOnce(toPage(graphMaterialMockListRecords));
        renderPage();

        expect(await screen.findByText("素材列表加载失败")).toBeInTheDocument();
        expect(screen.getByText("素材服务暂不可用")).toBeInTheDocument();

        const user = userEvent.setup();
        await user.click(screen.getByRole("button", { name: "重试加载素材列表" }));

        expect(await screen.findByText("三才图会 人物一")).toBeInTheDocument();
        expect(service.pageMaterials).toHaveBeenCalledTimes(2);
    });

    it("shows uninitialized material rows in the composite table", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockResolvedValue(toPage(graphMaterialMockListRecords));
        renderPage();

        expect(await screen.findByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getAllByText("未初始化/未抽取").length).toBeGreaterThan(0);
        expect(screen.getAllByRole("columnheader")).toHaveLength(11);
        expect(screen.getByRole("button", { name: /打开素材 三才图会 天文一/u })).toBeDisabled();
    });

    it("shows batch actions for selected material rows", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockResolvedValue(toPage(graphMaterialMockListRecords));
        renderPage();
        const user = userEvent.setup();

        await screen.findByText("三才图会 人物一");
        const checkboxes = screen.getAllByRole("checkbox");
        await user.click(checkboxes[2]);
        await user.click(checkboxes[3]);

        expect(screen.getByText("批量动作（2）")).toBeInTheDocument();
    });

    it("publishes selected materials through Knowledge publication preview and publish APIs", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockResolvedValue(toPage(graphMaterialMockListRecords));
        vi.mocked(service.previewBatchPublication).mockResolvedValue({
            materials: [
                {
                    contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                    result: {
                        edges: [],
                        issues: [],
                        materialLockVersion: "4",
                        materialRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                        nodes: [],
                        previewToken: "preview-1002",
                        publishable: true
                    },
                    success: true
                },
                {
                    contentRef: { contentRefId: "1003", contentType: "WANGQI_DOCUMENT" },
                    failureMessage: "发布预检存在阻塞项。",
                    success: false
                }
            ]
        });
        vi.mocked(service.publishBatch).mockResolvedValue({
            materials: [
                {
                    contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                    result: {
                        contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                        createdEdgeCount: "0",
                        createdNodeCount: "0",
                        materialStatus: "PUBLISHED",
                        reusedEdgeCount: "98",
                        reusedNodeCount: "64",
                        success: true
                    },
                    success: true
                }
            ]
        });
        renderPage();
        const user = userEvent.setup();

        await screen.findByText("三才图会 人物一");
        const checkboxes = screen.getAllByRole("checkbox");
        await user.click(checkboxes[2]);
        await user.click(checkboxes[3]);
        await user.click(screen.getByTestId("knowledge-graph-material-batch-publish-button"));

        await waitFor(() => {
            expect(service.previewBatchPublication).toHaveBeenCalledWith({
                contentRefs: [
                    { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                    { contentRefId: "1003", contentType: "WANGQI_DOCUMENT" }
                ]
            });
        });
        expect(service.publishBatch).toHaveBeenCalledWith({
            materials: [
                {
                    conflictDecisions: [],
                    contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" },
                    materialLockVersion: "4",
                    previewToken: "preview-1002"
                }
            ]
        });
        await waitFor(() => {
            expect(
                screen.getByTestId("knowledge-graph-material-batch-result-SANCAI_ENTRY:1002")
            ).toHaveTextContent("已发布");
        });
        expect(
            screen.getByTestId("knowledge-graph-material-batch-result-WANGQI_DOCUMENT:1003")
        ).toHaveTextContent("发布预检存在阻塞项。");
    });

    it("navigates to extraction tasks with selected contentRefs", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockResolvedValue(toPage(graphMaterialMockListRecords));
        const { visitedLocations } = renderPage();
        const user = userEvent.setup();

        await screen.findByText("三才图会 人物一");
        const checkboxes = screen.getAllByRole("checkbox");
        await user.click(checkboxes[1]);
        await user.click(checkboxes[2]);
        await user.click(screen.getByTestId("knowledge-graph-material-batch-view-tasks-button"));

        await waitFor(() => {
            expect(visitedLocations.at(-1)).toMatch(/^\/knowledge\/graph-extraction\?/u);
        });
        const lastLocation = visitedLocations.at(-1) ?? "";
        const params = new URLSearchParams(lastLocation.split("?")[1]);
        expect(JSON.parse(params.get("contentRefs") || "[]")).toEqual([
            { contentRefId: "1001", contentType: "SANCAI_ENTRY" },
            { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
        ]);
    });

    it("clears selected material when the detail drawer closes", async () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(service.pageMaterials).mockResolvedValue(toPage(graphMaterialMockListRecords));
        vi.mocked(service.getMaterial).mockResolvedValue(graphMaterialMockDetails[1]);
        renderPage();
        const user = userEvent.setup();

        await screen.findByText("三才图会 人物一");
        await user.click(screen.getByTestId("knowledge-graph-material-open-2002-link"));

        expect(await screen.findByTestId("knowledge-graph-material-detail-drawer")).toBeVisible();
        expect(service.getMaterial).toHaveBeenCalledWith({
            contentRef: { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
        });

        await user.click(screen.getByTestId("knowledge-graph-material-detail-close-button"));

        await waitFor(() => {
            expect(
                screen.queryByTestId("knowledge-graph-material-detail-drawer")
            ).not.toBeInTheDocument();
        });

        await user.click(screen.getByTestId("knowledge-graph-material-open-2002-link"));

        await waitFor(() => {
            expect(service.getMaterial).toHaveBeenCalledTimes(2);
        });
    });

    it("recovers from material detail loading error by retrying the query", async () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(service.pageMaterials).mockResolvedValue(toPage(graphMaterialMockListRecords));
        vi.mocked(service.getMaterial)
            .mockRejectedValueOnce(new Error("素材详情服务暂不可用"))
            .mockResolvedValueOnce(graphMaterialMockDetails[1]);
        renderPage();
        const user = userEvent.setup();

        await screen.findByText("三才图会 人物一");
        await user.click(screen.getByTestId("knowledge-graph-material-open-2002-link"));

        expect(await screen.findByText("素材详情加载失败")).toBeInTheDocument();
        expect(screen.getByText("素材详情服务暂不可用")).toBeInTheDocument();

        await user.click(screen.getByTestId("knowledge-graph-material-detail-retry-button"));

        await waitFor(() => {
            expect(screen.queryByText("素材详情加载失败")).not.toBeInTheDocument();
        });
        expect(service.getMaterial).toHaveBeenCalledTimes(2);
    });

    it("does not query materials when graph view permission is missing", () => {
        replacePermissions([]);
        renderPage();

        expect(screen.getByText("无权查看图谱素材库")).toBeInTheDocument();
        expect(service.pageMaterials).not.toHaveBeenCalled();
        expect(service.getMaterial).not.toHaveBeenCalled();
    });
});
