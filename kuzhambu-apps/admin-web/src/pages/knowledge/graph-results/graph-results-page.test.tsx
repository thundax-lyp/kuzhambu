import { cleanup, render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { App as AntdApp } from "antd";
import type { ComponentType } from "react";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphResultsPage } from "./graph-results-page";
import type { GraphVersionRecord } from "./graph-results-types";

vi.mock("./graph-results-service", () => ({
    pageVersions: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 1,
        totalPage: 1,
        count: 1,
        records: [
            {
                versionId: 71,
                taskId: "31",
                taskType: "GRAPH",
                status: "APPLIED",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: 1001,
                versionNo: 2
            }
        ]
    })),
    getVersionDetail: vi.fn(async () => ({
        versionId: 71,
        taskId: "31",
        taskType: "GRAPH",
        status: "APPLIED",
        sourceContentType: "SANCAI_ENTRY",
        sourceContentId: 1001,
        versionNo: 2
    })),
    pageEntities: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getEntityDetail: vi.fn(async () => null),
    pageRelations: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getRelationDetail: vi.fn(async () => null),
    pageLineageNodes: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getLineageNodeDetail: vi.fn(async () => null),
    pageLineageRelations: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 0,
        totalPage: 0,
        count: 0,
        records: []
    })),
    getLineageRelationDetail: vi.fn(async () => null)
}));

vi.mock("./components/graph-version-table", () => ({
    GraphVersionTable: (({
        versions,
        onOpenDetail
    }: {
        versions: GraphVersionRecord[];
        onOpenDetail: (version: GraphVersionRecord) => void;
    }) => (
        <div aria-label="知识图谱版本表格">
            {versions.map((version) => (
                <div key={version.versionId}>
                    <span>{version.versionId}</span>
                    <button type="button" onClick={() => onOpenDetail(version)}>
                        查看详情
                    </button>
                </div>
            ))}
        </div>
    )) as ComponentType<{
        versions: GraphVersionRecord[];
        onOpenDetail: (version: GraphVersionRecord) => void;
    }>
}));

vi.mock("./components/graph-version-detail", () => ({
    GraphVersionDetail: (() => null) as ComponentType
}));

vi.mock("./components/graph-entity-table", () => ({
    GraphEntityTable: (() => null) as ComponentType
}));

vi.mock("./components/graph-entity-detail", () => ({
    GraphEntityDetail: (() => null) as ComponentType
}));

vi.mock("./components/graph-relation-table", () => ({
    GraphRelationTable: (() => null) as ComponentType
}));

vi.mock("./components/graph-relation-detail", () => ({
    GraphRelationDetail: (() => null) as ComponentType
}));

vi.mock("./components/graph-lineage-node-table", () => ({
    GraphLineageNodeTable: (() => null) as ComponentType
}));

vi.mock("./components/graph-lineage-node-detail", () => ({
    GraphLineageNodeDetail: (() => null) as ComponentType
}));

vi.mock("./components/graph-lineage-relation-table", () => ({
    GraphLineageRelationTable: (() => null) as ComponentType
}));

vi.mock("./components/graph-lineage-relation-detail", () => ({
    GraphLineageRelationDetail: (() => null) as ComponentType
}));

describe("GraphResultsPage", () => {
    beforeEach(() => {
        const originalGetComputedStyle = window.getComputedStyle.bind(window);
        vi.spyOn(window, "getComputedStyle").mockImplementation(((
            element: Element,
            pseudoElt?: string
        ) =>
            originalGetComputedStyle(
                element,
                pseudoElt === undefined ? undefined : null
            )) as typeof window.getComputedStyle);
        replacePermissions(["knowledge:graph:view"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        cleanup();
    });

    it("renders graph version list entry", async () => {
        const queryClient = new QueryClient({
            defaultOptions: {
                queries: {
                    gcTime: Infinity,
                    refetchOnWindowFocus: false,
                    retry: false
                }
            }
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <GraphResultsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(screen.getByRole("heading", { level: 2, name: "正式结果读取" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { level: 4, name: "结果入口" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "图谱版本" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式实体" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式关系" })).toBeInTheDocument();
        expect(screen.getByRole("tab", { name: "正式世系" })).toBeInTheDocument();
        expect(await screen.findByLabelText("知识图谱版本表格")).toBeInTheDocument();
        expect(screen.getByText("71")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "查看详情" })).toBeInTheDocument();
        expect(
            screen.getByText(
                "当前页已以图谱版本作为主入口，管理员可以从版本详情下钻审阅实体、关系和世系正式结果。"
            )
        ).toBeInTheDocument();
    });
});
