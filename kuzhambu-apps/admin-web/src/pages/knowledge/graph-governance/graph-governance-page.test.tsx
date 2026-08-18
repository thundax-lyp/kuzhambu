import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphGovernancePage } from "./graph-governance-page";
import * as service from "./graph-governance-service";

vi.mock("@/components/kuzhambu-graph", () => ({
    // eslint-disable-next-line @typescript-eslint/naming-convention
    KuzhambuGraph: () => <div data-testid="knowledge-graph-governance-canvas" />
}));

vi.mock("./graph-governance-service", () => ({
    getPublishedNode: vi.fn(),
    getPublishedRelation: vi.fn(),
    pagePublishedAdjacency: vi.fn(),
    pagePublishedNodes: vi.fn(),
    pagePublishedRelations: vi.fn()
}));

const page = {
    count: 2,
    pageNo: 1,
    pageSize: 20,
    records: [
        { id: "1", name: "李白", nodeType: "PERSON", source: "MATERIAL", status: "ACTIVE" },
        { id: "2", name: "杜甫", nodeType: "PERSON", source: "MANUAL", status: "ACTIVE" }
    ],
    totalCount: 2,
    totalPage: 1
};

const createQueryClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } });

const renderPage = () =>
    render(
        <QueryClientProvider client={createQueryClient()}>
            <GraphGovernancePage />
        </QueryClientProvider>
    );

describe("GraphGovernancePage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
        vi.clearAllMocks();
    });

    it("loads a paginated published-node list and focuses its local graph", async () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(service.pagePublishedNodes).mockResolvedValue(page);
        vi.mocked(service.getPublishedNode).mockResolvedValue({
            incidentEdges: [],
            materials: [],
            node: page.records[0],
            operations: [],
            properties: []
        });
        vi.mocked(service.pagePublishedAdjacency).mockResolvedValue({
            ...page,
            records: []
        });
        const user = userEvent.setup();

        renderPage();

        expect(await screen.findByText("李白")).toBeInTheDocument();
        expect(screen.getByLabelText("发布节点分页列表")).toBeInTheDocument();
        await user.click(screen.getByTestId("knowledge-graph-governance-view-node-1"));
        expect(await screen.findByTestId("knowledge-graph-governance-canvas")).toBeInTheDocument();
        expect(screen.getByText(/已以“李白”为焦点加载一跳关系/)).toBeInTheDocument();
        expect(screen.getByText("节点详情")).toBeInTheDocument();
        await waitFor(() =>
            expect(service.pagePublishedAdjacency).toHaveBeenCalledWith(
                expect.objectContaining({ subjectNodeId: "1" })
            )
        );
    });

    it("submits search filters and switches to the paginated relation list", async () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(service.pagePublishedNodes).mockResolvedValue(page);
        vi.mocked(service.pagePublishedRelations).mockResolvedValue({
            ...page,
            records: [
                {
                    id: "11",
                    relationType: "AUTHORED",
                    source: "MATERIAL",
                    status: "ACTIVE"
                }
            ]
        });
        const user = userEvent.setup();

        renderPage();
        await screen.findByText("李白");
        await user.type(screen.getByRole("textbox", { name: "搜索发布对象" }), "李白");
        await user.click(screen.getByTestId("knowledge-graph-governance-apply-filters"));
        await waitFor(() =>
            expect(vi.mocked(service.pagePublishedNodes).mock.calls.at(-1)?.[0]).toMatchObject({
                keyword: "李白",
                status: "ACTIVE"
            })
        );
        await user.click(screen.getByText("关系"));
        expect(await screen.findByText("AUTHORED")).toBeInTheDocument();
        expect(screen.getByLabelText("发布关系分页列表")).toBeInTheDocument();
    });

    it("renders the access-denied state without loading published objects", () => {
        renderPage();

        expect(screen.getByText("无权查看图谱治理")).toBeInTheDocument();
        expect(vi.mocked(service.pagePublishedNodes)).not.toHaveBeenCalled();
    });
});
