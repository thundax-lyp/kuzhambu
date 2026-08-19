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
        vi.mocked(service.pagePublishedAdjacency).mockResolvedValue({
            ...page,
            records: []
        });
        const user = userEvent.setup();

        renderPage();

        expect(await screen.findByText("李白")).toBeInTheDocument();
        expect(screen.getAllByText("人物")).not.toHaveLength(0);
        expect(screen.getByLabelText("发布节点关系树")).toBeInTheDocument();
        await user.click(screen.getByTestId("knowledge-graph-governance-toggle-node-1"));
        expect(await screen.findByTestId("knowledge-graph-governance-canvas")).toBeInTheDocument();
        expect(screen.getByText(/已加入 1 个节点，并加载各节点的一跳关系/)).toBeInTheDocument();
        expect(screen.queryByText("节点详情")).not.toBeInTheDocument();
        await user.click(screen.getByTestId("knowledge-graph-governance-view-node-1"));
        expect(screen.getByText("节点详情")).toBeInTheDocument();
        await waitFor(() =>
            expect(service.pagePublishedAdjacency).toHaveBeenCalledWith(
                expect.objectContaining({ subjectNodeId: "1" })
            )
        );
    });

    it("submits search filters for the paginated node tree", async () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(service.pagePublishedNodes).mockResolvedValue(page);
        const user = userEvent.setup();

        renderPage();
        await screen.findByText("李白");
        await user.type(screen.getByRole("textbox", { name: "搜索节点" }), "李白");
        await user.click(screen.getByTestId("knowledge-graph-governance-apply-filters"));
        await waitFor(() =>
            expect(vi.mocked(service.pagePublishedNodes).mock.calls.at(-1)?.[0]).toMatchObject({
                keyword: "李白",
                status: "ACTIVE"
            })
        );
        expect(screen.getByLabelText("发布节点关系树")).toBeInTheDocument();
    });

    it("loads relations as child rows and continues their pagination", async () => {
        replacePermissions(["knowledge:graph:view"]);
        vi.mocked(service.pagePublishedNodes).mockResolvedValue(page);
        vi.mocked(service.pagePublishedAdjacency).mockResolvedValue({
            ...page,
            records: [
                {
                    isolated: false,
                    object: { id: "2", name: "唐诗", nodeType: "WORK" },
                    relation: { id: "11", relationType: "AUTHORED" },
                    subject: page.records[0]
                }
            ],
            totalCount: 2,
            totalPage: 2
        });
        const user = userEvent.setup();

        renderPage();

        await screen.findByText("李白");
        const expandButton = screen
            .getByLabelText("发布节点关系树")
            .querySelector<HTMLButtonElement>(".ant-table-row-expand-icon");
        expect(expandButton).not.toBeNull();
        await user.click(expandButton!);

        expect(await screen.findByText("唐诗")).toBeInTheDocument();
        await user.click(screen.getByTestId("knowledge-graph-governance-load-more-relations-1"));
        await waitFor(() =>
            expect(service.pagePublishedAdjacency).toHaveBeenCalledWith(
                expect.objectContaining({ pageNo: 2, subjectNodeId: "1" })
            )
        );
    });

    it("shows the editor action to users with graph-edit permission", async () => {
        replacePermissions(["knowledge:graph:view", "knowledge:graph:edit"]);
        vi.mocked(service.pagePublishedNodes).mockResolvedValue(page);

        renderPage();

        expect(
            await screen.findByTestId("knowledge-graph-governance-edit-node-1")
        ).toHaveTextContent("编辑");
        expect(
            screen.queryByTestId("knowledge-graph-governance-view-node-1")
        ).not.toBeInTheDocument();
    });

    it("renders the access-denied state without loading published objects", () => {
        renderPage();

        expect(screen.getByText("无权查看图谱治理")).toBeInTheDocument();
        expect(vi.mocked(service.pagePublishedNodes)).not.toHaveBeenCalled();
    });
});
