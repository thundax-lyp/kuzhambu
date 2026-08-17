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

const mockCatalogThenPage = (
    pageRecords: GraphMaterialListRecord[] = graphMaterialMockListRecords,
    totalCount = pageRecords.length
) => {
    vi.mocked(service.pageMaterials).mockImplementation(async (query) => {
        if (query?.pageSize === 500) {
            return toPage(
                graphMaterialMockListRecords,
                1,
                500,
                graphMaterialMockListRecords.length
            );
        }
        return toPage(pageRecords, query?.pageNo ?? 1, query?.pageSize ?? 20, totalCount);
    });
};

const selectCatalogLeaf = async (leafTitle = "卷二") => {
    const user = userEvent.setup();
    await user.click(await screen.findByText(leafTitle));
    return user;
};

describe("GraphMaterialPage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
        vi.clearAllMocks();
    });

    it("shows the material list loading state after selecting a catalog leaf", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials).mockImplementation((query) => {
            if (query?.pageSize === 500) {
                return Promise.resolve(
                    toPage(
                        graphMaterialMockListRecords,
                        1,
                        500,
                        graphMaterialMockListRecords.length
                    )
                );
            }
            return new Promise(() => undefined);
        });
        const { container } = renderPage();

        expect(service.pageMaterials).toHaveBeenCalledWith({ pageNo: 1, pageSize: 500 });
        expect(screen.getByText("请选择左侧目录叶子节点查看素材列表")).toBeInTheDocument();
        await selectCatalogLeaf();

        await waitFor(() => {
            expect(service.pageMaterials).toHaveBeenLastCalledWith({
                categoryCode: "人物",
                contentType: "SANCAI_ENTRY",
                keyword: undefined,
                pageNo: 1,
                pageSize: 20,
                volumeCode: "卷二"
            });
            expect(container.querySelector(".ant-spin-spinning")).toBeInTheDocument();
        });
    });

    it("shows table empty state when selected leaf has no material records", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage([]);
        renderPage();

        await selectCatalogLeaf();

        expect(await screen.findByText("暂无图谱素材")).toBeInTheDocument();
        expect(screen.getByLabelText("图谱素材列表")).toBeInTheDocument();
    });

    it("queries pageMaterials with selected catalog leaf and search keyword", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage();
        renderPage();
        const user = await selectCatalogLeaf();

        await user.type(screen.getByLabelText("搜索图谱素材"), "人物");

        await waitFor(() => {
            expect(service.pageMaterials).toHaveBeenLastCalledWith({
                categoryCode: "人物",
                contentType: "SANCAI_ENTRY",
                keyword: "人物",
                pageNo: 1,
                pageSize: 20,
                volumeCode: "卷二"
            });
        });
    });

    it("queries pageMaterials with pagination values inside the selected leaf", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage(graphMaterialMockListRecords, 50);
        renderPage();
        const user = await selectCatalogLeaf();

        await screen.findByText("三才图会 人物一");
        await user.click(screen.getByRole("listitem", { name: "2" }));

        await waitFor(() => {
            expect(service.pageMaterials).toHaveBeenLastCalledWith({
                categoryCode: "人物",
                contentType: "SANCAI_ENTRY",
                keyword: undefined,
                pageNo: 2,
                pageSize: 20,
                volumeCode: "卷二"
            });
        });
    });

    it("recovers from selected material list error by retrying the query", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pageMaterials)
            .mockResolvedValueOnce(
                toPage(graphMaterialMockListRecords, 1, 500, graphMaterialMockListRecords.length)
            )
            .mockRejectedValueOnce(new Error("素材服务暂不可用"))
            .mockResolvedValueOnce(toPage(graphMaterialMockListRecords));
        renderPage();

        await selectCatalogLeaf();

        expect(await screen.findByText("素材列表加载失败")).toBeInTheDocument();
        expect(screen.getByText("素材服务暂不可用")).toBeInTheDocument();

        const user = userEvent.setup();
        await user.click(screen.getByRole("button", { name: "重试加载素材" }));

        expect(await screen.findByText("三才图会 人物一")).toBeInTheDocument();
        expect(service.pageMaterials).toHaveBeenCalledTimes(3);
    });

    it("shows uninitialized material rows in the leaf material table", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage();
        renderPage();

        await selectCatalogLeaf("卷一");

        expect(await screen.findByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getAllByText("未初始化/未抽取").length).toBeGreaterThan(0);
        expect(screen.getAllByRole("columnheader")).toHaveLength(4);
        expect(screen.getByRole("button", { name: /打开素材 三才图会 天文一/u })).toBeDisabled();
    });

    it("creates extraction task for a single material row", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage();
        vi.mocked(service.createBatchExtraction).mockResolvedValue({
            batchId: "batch-001",
            materials: []
        });
        renderPage();
        const user = await selectCatalogLeaf();

        await screen.findByText("三才图会 人物一");
        await user.click(screen.getByRole("button", { name: "提取 三才图会 人物一" }));

        await waitFor(() => {
            expect(vi.mocked(service.createBatchExtraction).mock.calls[0]?.[0]).toEqual({
                contentRefs: [{ contentRefId: "1002", contentType: "SANCAI_ENTRY" }]
            });
        });
    });

    it("navigates to extraction tasks from a single material row", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage();
        const { visitedLocations } = renderPage();
        const user = await selectCatalogLeaf();

        await screen.findByText("三才图会 人物一");
        await user.click(screen.getByRole("button", { name: "查看任务 三才图会 人物一" }));

        await waitFor(() => {
            expect(visitedLocations.at(-1)).toMatch(/^\/knowledge\/graph-extraction\?/u);
        });
        const lastLocation = visitedLocations.at(-1) ?? "";
        const params = new URLSearchParams(lastLocation.split("?")[1]);
        expect(JSON.parse(params.get("contentRefs") || "[]")).toEqual([
            { contentRefId: "1002", contentType: "SANCAI_ENTRY" }
        ]);
    });

    it("clears selected material when the detail drawer closes", async () => {
        replacePermissions(["knowledge:graph:view"]);
        mockCatalogThenPage();
        vi.mocked(service.getMaterial).mockResolvedValue(graphMaterialMockDetails[1]);
        renderPage();
        const user = await selectCatalogLeaf();

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
        mockCatalogThenPage();
        vi.mocked(service.getMaterial)
            .mockRejectedValueOnce(new Error("素材详情服务暂不可用"))
            .mockResolvedValueOnce(graphMaterialMockDetails[1]);
        renderPage();
        const user = await selectCatalogLeaf();

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
