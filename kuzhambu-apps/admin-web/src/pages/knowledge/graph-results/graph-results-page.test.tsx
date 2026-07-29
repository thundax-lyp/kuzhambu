import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { App as AntdApp } from "antd";
import type { ComponentType } from "react";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphResultsPage } from "./graph-results-page";
import type { GraphVersionRecord } from "./graph-results-types";

const serviceMocks = vi.hoisted(() => ({
    pageVersions: vi.fn(async () => ({
        pageNo: 1,
        pageSize: 20,
        totalCount: 1,
        totalPage: 1,
        count: 1,
        records: [
            {
                versionId: "71",
                taskId: "31",
                taskType: "GRAPH",
                status: "APPLIED",
                sourceContentType: "SANCAI_ENTRY",
                sourceContentId: "1001",
                versionNo: 2,
                refinementApplied: true,
                lastRefinementTaskId: "31",
                lastRefinementAppliedAt: 1760000000000
            }
        ]
    })),
    getVersionDetail: vi.fn(async () => null),
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

vi.mock("./graph-results-service", () => ({
    ...serviceMocks
}));

vi.mock("./graph-version-table", () => ({
    GraphVersionTable: (({
        versions,
        onOpenResults
    }: {
        versions: GraphVersionRecord[];
        onOpenResults: (version: GraphVersionRecord) => void;
    }) => (
        <div aria-label="知识图谱版本表格">
            {versions.map((version) => (
                <button
                    key={version.versionId}
                    type="button"
                    onClick={() => onOpenResults(version)}
                >
                    {version.versionId}
                </button>
            ))}
        </div>
    )) as ComponentType<{
        versions: GraphVersionRecord[];
        onOpenResults: (version: GraphVersionRecord) => void;
    }>
}));

vi.mock("./graph-version-detail", () => ({
    GraphVersionDetail: (() => null) as ComponentType
}));
vi.mock("./graph-entity-table", () => ({
    GraphEntityTable: (() => null) as ComponentType
}));
vi.mock("./graph-entity-detail", () => ({
    GraphEntityDetail: (() => null) as ComponentType
}));
vi.mock("./graph-relation-table", () => ({
    GraphRelationTable: (() => null) as ComponentType
}));
vi.mock("./graph-relation-detail", () => ({
    GraphRelationDetail: (() => null) as ComponentType
}));
vi.mock("./graph-lineage-node-table", () => ({
    GraphLineageNodeTable: (() => null) as ComponentType
}));
vi.mock("./graph-lineage-node-detail", () => ({
    GraphLineageNodeDetail: (() => null) as ComponentType
}));
vi.mock("./graph-lineage-relation-table", () => ({
    GraphLineageRelationTable: (() => null) as ComponentType
}));
vi.mock("./graph-lineage-relation-detail", () => ({
    GraphLineageRelationDetail: (() => null) as ComponentType
}));

describe("GraphResultsPage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        window.history.pushState({}, "", "/");
        replacePermissions(["knowledge:graph:view"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
        window.history.pushState({}, "", "/");
        cleanup();
    });

    it("renders page shell", async () => {
        const queryClient = new QueryClient({
            defaultOptions: {
                queries: { gcTime: Infinity, refetchOnWindowFocus: false, retry: false }
            }
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <GraphResultsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(screen.getByRole("heading", { name: "正式结果读取" })).toBeInTheDocument();
        expect(await screen.findByLabelText("知识图谱版本表格")).toBeInTheDocument();
        expect(screen.getByText("71")).toBeInTheDocument();
    }, 30000);

    it("focuses graph version from search params and refreshes result tabs", async () => {
        window.history.pushState({}, "", "/knowledge/graph-results?graphVersionId=71");
        const queryClient = new QueryClient({
            defaultOptions: {
                queries: { gcTime: Infinity, refetchOnWindowFocus: false, retry: false }
            }
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <GraphResultsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        await waitFor(() => {
            expect(serviceMocks.pageEntities).toHaveBeenCalledWith(
                expect.objectContaining({ versionId: "71" })
            );
        });

        fireEvent.click(screen.getByRole("tab", { name: "正式关系" }));
        await waitFor(() => {
            expect(serviceMocks.pageRelations).toHaveBeenCalledWith(
                expect.objectContaining({ versionId: "71" })
            );
        });

        fireEvent.click(screen.getByRole("tab", { name: "正式世系" }));
        await waitFor(() => {
            expect(serviceMocks.pageLineageNodes).toHaveBeenCalledWith(
                expect.objectContaining({ versionId: "71" })
            );
            expect(serviceMocks.pageLineageRelations).toHaveBeenCalledWith(
                expect.objectContaining({ versionId: "71" })
            );
        });
    }, 30000);

    it("ignores invalid graph version search param", async () => {
        window.history.pushState({}, "", "/knowledge/graph-results?graphVersionId=abc");
        const queryClient = new QueryClient({
            defaultOptions: {
                queries: { gcTime: Infinity, refetchOnWindowFocus: false, retry: false }
            }
        });

        render(
            <QueryClientProvider client={queryClient}>
                <AntdApp>
                    <GraphResultsPage />
                </AntdApp>
            </QueryClientProvider>
        );

        expect(await screen.findByLabelText("知识图谱版本表格")).toBeInTheDocument();
        await waitFor(() => {
            expect(serviceMocks.pageEntities).not.toHaveBeenCalled();
            expect(serviceMocks.pageRelations).not.toHaveBeenCalled();
            expect(serviceMocks.pageLineageNodes).not.toHaveBeenCalled();
            expect(serviceMocks.pageLineageRelations).not.toHaveBeenCalled();
        });
    }, 30000);
});
