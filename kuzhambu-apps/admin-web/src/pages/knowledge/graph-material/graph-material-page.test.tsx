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
import type { GraphMaterialListRecord, GraphMaterialTreeNodeRecord } from "./graph-material-types";
import * as service from "./graph-material-service";

vi.mock("@/components/kuzhambu-graph", () => ({
    ["KuzhambuGraph"]: () => <div data-testid="knowledge-graph-material-canvas-mock" />
}));

vi.mock("./graph-material-service", () => ({
    createBatchExtraction: vi.fn(),
    getMaterial: vi.fn(),
    listMaterialTree: vi.fn(),
    pageMaterials: vi.fn(),
    previewBatchPublication: vi.fn(),
    previewBatchWithdrawal: vi.fn(),
    previewPublication: vi.fn(),
    previewWithdrawal: vi.fn(),
    publishBatch: vi.fn(),
    publishMaterial: vi.fn(),
    retryExtraction: vi.fn(),
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
        <MemoryRouter initialEntries={["/knowledge/graph-material"]}>
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
    vi.mocked(service.listMaterialTree).mockImplementation(async (query) => {
        return materialTreeNodesByParentId[query?.parentId || "root"] || [];
    });
    vi.mocked(service.pageMaterials).mockImplementation(async (query) => {
        return toPage(pageRecords, query?.pageNo ?? 1, query?.pageSize ?? 20, totalCount);
    });
};

const materialTreeNodesByParentId: Record<string, GraphMaterialTreeNodeRecord[]> = {
    root: [
        {
            id: "type:SANCAI_ENTRY",
            leaf: false,
            nodeType: "contentType",
            parentId: "root",
            title: "三才图会"
        },
        {
            id: "type:WANGQI_DOCUMENT",
            leaf: false,
            nodeType: "contentType",
            parentId: "root",
            title: "王祺文献"
        },
        {
            id: "type:MING_CUSTOMS",
            leaf: false,
            nodeType: "contentType",
            parentId: "root",
            title: "明代风俗"
        }
    ],
    "type:SANCAI_ENTRY": [
        {
            id: "type:SANCAI_ENTRY:category:%E5%A4%A9%E6%96%87",
            leaf: false,
            nodeType: "category",
            parentId: "type:SANCAI_ENTRY",
            title: "天文"
        },
        {
            id: "type:SANCAI_ENTRY:category:%E4%BA%BA%E7%89%A9",
            leaf: false,
            nodeType: "category",
            parentId: "type:SANCAI_ENTRY",
            title: "人物"
        }
    ],
    "type:WANGQI_DOCUMENT": [
        {
            id: "type:WANGQI_DOCUMENT:category:%E6%96%B9%E5%BF%97",
            leaf: false,
            nodeType: "category",
            parentId: "type:WANGQI_DOCUMENT",
            title: "方志"
        }
    ],
    "type:MING_CUSTOMS": [
        {
            id: "type:MING_CUSTOMS:category:%E9%A3%8E%E4%BF%97",
            leaf: false,
            nodeType: "category",
            parentId: "type:MING_CUSTOMS",
            title: "风俗"
        }
    ],
    "type:SANCAI_ENTRY:category:%E5%A4%A9%E6%96%87": [
        {
            id: "type:SANCAI_ENTRY:category:%E5%A4%A9%E6%96%87:volume:%E5%8D%B7%E4%B8%80",
            leaf: true,
            nodeType: "volume",
            parentId: "type:SANCAI_ENTRY:category:%E5%A4%A9%E6%96%87",
            title: "卷一"
        }
    ],
    "type:SANCAI_ENTRY:category:%E4%BA%BA%E7%89%A9": [
        {
            id: "type:SANCAI_ENTRY:category:%E4%BA%BA%E7%89%A9:volume:%E5%8D%B7%E4%BA%8C",
            leaf: true,
            nodeType: "volume",
            parentId: "type:SANCAI_ENTRY:category:%E4%BA%BA%E7%89%A9",
            title: "卷二"
        }
    ]
};

const selectCatalogLeaf = async (leafTitle = "卷二") => {
    const user = userEvent.setup();
    if (leafTitle === "卷一") {
        await user.click(await screen.findByText("天文"));
    }
    if (leafTitle === "卷二") {
        await user.click(await screen.findByText("人物"));
    }
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
        vi.mocked(service.listMaterialTree).mockImplementation(async (query) => {
            return materialTreeNodesByParentId[query?.parentId || "root"] || [];
        });
        vi.mocked(service.pageMaterials).mockImplementation(() => {
            return new Promise(() => undefined);
        });
        const { container } = renderPage();

        expect(service.listMaterialTree).toHaveBeenCalledWith({ parentId: "root" });
        expect(screen.getByText("请选择左侧目录叶子节点查看素材列表")).toBeInTheDocument();
        await selectCatalogLeaf();

        await waitFor(() => {
            expect(service.pageMaterials).toHaveBeenLastCalledWith({
                categoryCode: "人物",
                contentType: "SANCAI_ENTRY",
                pageNo: 1,
                pageSize: 20,
                volumeCode: "卷二"
            });
            expect(container.querySelector(".ant-spin-spinning")).toBeInTheDocument();
        });
    });

    it("expands first-level catalog nodes after the initial catalog load", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage();
        renderPage();

        expect(await screen.findByText("人物")).toBeInTheDocument();
        expect(service.listMaterialTree).toHaveBeenCalledWith({ parentId: "root" });
        expect(service.listMaterialTree).toHaveBeenCalledWith({ parentId: "type:SANCAI_ENTRY" });
        expect(service.listMaterialTree).toHaveBeenCalledWith({ parentId: "type:WANGQI_DOCUMENT" });
        expect(service.listMaterialTree).toHaveBeenCalledWith({ parentId: "type:MING_CUSTOMS" });
    });

    it("shows table empty state when selected leaf has no material records", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage([]);
        renderPage();

        await selectCatalogLeaf();

        expect(await screen.findByText("暂无图谱素材")).toBeInTheDocument();
        expect(screen.getByLabelText("图谱素材列表")).toBeInTheDocument();
    });

    it("refreshes the selected material list without reloading the catalog tree", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage();
        renderPage();
        const user = await selectCatalogLeaf();

        await waitFor(() => {
            expect(service.pageMaterials).toHaveBeenCalledTimes(1);
        });
        const catalogRequestCount = vi.mocked(service.listMaterialTree).mock.calls.length;
        await user.click(screen.getByLabelText("刷新图谱素材列表"));

        await waitFor(() => {
            expect(service.pageMaterials).toHaveBeenCalledTimes(2);
        });
        expect(service.listMaterialTree).toHaveBeenCalledTimes(catalogRequestCount);
    });

    it("loads catalog children by parentId when expanding tree nodes", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage([graphMaterialMockListRecords[1]]);
        renderPage();

        await selectCatalogLeaf("卷二");

        expect(await screen.findByText("三才图会 人物一")).toBeInTheDocument();
        expect(service.listMaterialTree).toHaveBeenCalledWith({ parentId: "root" });
        expect(service.listMaterialTree).toHaveBeenCalledWith({ parentId: "type:SANCAI_ENTRY" });
        expect(service.listMaterialTree).toHaveBeenCalledWith({
            parentId: "type:SANCAI_ENTRY:category:%E4%BA%BA%E7%89%A9"
        });
        expect(service.pageMaterials).toHaveBeenLastCalledWith({
            categoryCode: "人物",
            contentType: "SANCAI_ENTRY",
            pageNo: 1,
            pageSize: 20,
            volumeCode: "卷二"
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
                pageNo: 2,
                pageSize: 20,
                volumeCode: "卷二"
            });
        });
    });

    it("recovers from selected material list error by retrying the query", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.listMaterialTree).mockImplementation(async (query) => {
            return materialTreeNodesByParentId[query?.parentId || "root"] || [];
        });
        vi.mocked(service.pageMaterials)
            .mockRejectedValueOnce(new Error("素材服务暂不可用"))
            .mockResolvedValueOnce(toPage(graphMaterialMockListRecords));
        renderPage();

        await selectCatalogLeaf();

        expect(await screen.findByText("素材列表加载失败")).toBeInTheDocument();
        expect(screen.getByText("素材服务暂不可用")).toBeInTheDocument();

        const user = userEvent.setup();
        await user.click(screen.getByRole("button", { name: "重试加载素材" }));

        expect(await screen.findByText("三才图会 人物一")).toBeInTheDocument();
        expect(service.pageMaterials).toHaveBeenCalledTimes(2);
    });

    it("shows uninitialized material rows in the leaf material table", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage();
        renderPage();

        await selectCatalogLeaf("卷一");

        expect(await screen.findByText("三才图会 天文一")).toBeInTheDocument();
        expect(screen.getAllByText("未抽取").length).toBeGreaterThan(0);
        expect(screen.getAllByRole("columnheader")).toHaveLength(5);
        expect(screen.getByRole("button", { name: /查看素材 三才图会 天文一/u })).toBeEnabled();
    });

    it("shows the extraction action for a single material row", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        mockCatalogThenPage();
        renderPage();
        await selectCatalogLeaf();

        await screen.findByText("三才图会 人物一");
        expect(screen.getByRole("button", { name: "提取 三才图会 人物一" })).toBeEnabled();
    });

    it("clears selected material when the detail drawer closes", async () => {
        replacePermissions(["knowledge:graph:view"]);
        mockCatalogThenPage();
        vi.mocked(service.getMaterial).mockResolvedValue(graphMaterialMockDetails[1]);
        renderPage();
        const user = await selectCatalogLeaf();

        await screen.findByText("三才图会 人物一");
        await user.click(screen.getByRole("button", { name: "查看素材 三才图会 人物一" }));

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

        await user.click(screen.getByRole("button", { name: "查看素材 三才图会 人物一" }));

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
        await user.click(screen.getByRole("button", { name: "查看素材 三才图会 人物一" }));

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
