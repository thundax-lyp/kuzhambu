import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen } from "@testing-library/react";
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

vi.mock("./components/graph-version-table", () => ({
    GraphVersionTable: (({ versions }: { versions: GraphVersionRecord[] }) => (
        <div aria-label="知识图谱版本表格">
            {versions.map((version) => (
                <div key={version.versionId}>{version.versionId}</div>
            ))}
        </div>
    )) as ComponentType<{ versions: GraphVersionRecord[] }>
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
        replacePermissions(["knowledge:graph:view"]);
    });

    afterEach(() => {
        vi.restoreAllMocks();
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
    }, 10000);
});
