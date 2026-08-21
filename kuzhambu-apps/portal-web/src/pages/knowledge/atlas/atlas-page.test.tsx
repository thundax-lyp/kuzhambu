import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KnowledgeAtlasPage } from "./atlas-page";

/* eslint-disable @typescript-eslint/naming-convention */
vi.mock("@xyflow/react", () => ({
    Background: () => null,
    BackgroundVariant: { Dots: "dots" },
    Controls: () => null,
    Handle: () => null,
    MarkerType: { ArrowClosed: "arrowclosed" },
    MiniMap: () => null,
    Position: { Left: "left", Right: "right" },
    ReactFlow: ({
        children,
        nodes,
        onNodeDoubleClick
    }: {
        children: React.ReactNode;
        nodes: { data: { expanded: boolean; label: string }; id: string }[];
        onNodeDoubleClick: (event: unknown, node: { data: { label: string }; id: string }) => void;
    }) => (
        <div>
            {nodes.map((node) => (
                <button
                    key={node.id}
                    type="button"
                    data-expanded={node.data.expanded}
                    onDoubleClick={(event) => onNodeDoubleClick(event, node)}
                >
                    {node.data.label}
                </button>
            ))}
            {children}
        </div>
    )
}));

const response = (data: unknown) =>
    Promise.resolve(new Response(JSON.stringify({ code: "COMMON-00000", data }), { status: 200 }));

afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
});

describe("KnowledgeAtlasPage", () => {
    it("shows the workbench metrics and progressively loaded graph preview", async () => {
        let oneHopCallCount = 0;
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.includes("overview/get")) {
                return response({
                    publishedNodeCount: "12",
                    publishedEdgeCount: "18",
                    coveredMaterialCount: "6",
                    isolatedNodeCount: "2"
                });
            }
            if (url.includes("recent-edges/list")) {
                return response({
                    nodes: [
                        { id: "1", name: "黄帝", nodeType: "PERSON" },
                        { id: "2", name: "华夏", nodeType: "PLACE" }
                    ],
                    edges: [
                        { id: "11", sourceNodeId: "1", targetNodeId: "2", relationType: "活动于" }
                    ]
                });
            }
            oneHopCallCount += 1;
            if (oneHopCallCount === 2) {
                return response({
                    nodes: [{ id: "3", name: "轩辕", nodeType: "PERSON" }],
                    edges: [
                        {
                            id: "12",
                            sourceNodeId: "1",
                            targetNodeId: "3",
                            relationType: "PARENT_OF"
                        }
                    ],
                    nextCursor: null,
                    truncated: false
                });
            }
            return response({ nodes: [], edges: [], nextCursor: null, truncated: false });
        });
        const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
        render(
            <QueryClientProvider client={client}>
                <MemoryRouter>
                    <KnowledgeAtlasPage />
                </MemoryRouter>
            </QueryClientProvider>
        );

        expect(await screen.findByRole("heading", { name: "三才图会总谱" })).toBeTruthy();
        await waitFor(() => expect(screen.getByText("12")).toBeTruthy());
        expect(screen.getByLabelText("三才图会总谱预览")).toBeTruthy();
        fireEvent.doubleClick(screen.getByRole("button", { name: "黄帝" }));
        expect(await screen.findByRole("button", { name: "轩辕" })).toBeTruthy();
        expect(screen.getByRole("button", { name: "华夏" })).toBeTruthy();
    });

    it("automatically expands core nodes by degree until the 100-node limit", async () => {
        vi.useFakeTimers();
        const relatedNodes = Array.from({ length: 120 }, (_, index) => ({
            id: String(index + 2),
            name: `节点 ${index + 1}`,
            nodeType: "CONCEPT"
        }));
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            const url = String(input);
            if (url.includes("overview/get")) {
                return response({
                    publishedNodeCount: "121",
                    publishedEdgeCount: "120",
                    coveredMaterialCount: "1",
                    isolatedNodeCount: "0"
                });
            }
            if (url.includes("recent-edges/list")) {
                return response({
                    nodes: [{ id: "1", name: "中心节点", nodeType: "CONCEPT" }, ...relatedNodes],
                    edges: relatedNodes.map((node, index) => ({
                        id: String(index + 101),
                        sourceNodeId: "1",
                        targetNodeId: node.id,
                        relationType: "关联"
                    }))
                });
            }
            return response({ nodes: [], edges: [], nextCursor: null, truncated: false });
        });
        const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
        render(
            <QueryClientProvider client={client}>
                <MemoryRouter>
                    <KnowledgeAtlasPage />
                </MemoryRouter>
            </QueryClientProvider>
        );

        await act(async () => {
            await vi.advanceTimersByTimeAsync(0);
        });
        expect(screen.getByRole("button", { name: "节点 40" })).toBeTruthy();
        expect(screen.getByRole("button", { name: "中心节点" }).dataset.expanded).toBe("false");
        expect(screen.queryByRole("button", { name: "节点 41" })).toBeNull();

        await act(async () => {
            await vi.advanceTimersByTimeAsync(1_200);
        });
        expect(screen.getByRole("button", { name: "节点 41" })).toBeTruthy();
        expect(screen.getByRole("button", { name: "节点 42" })).toBeTruthy();
        expect(screen.getByRole("button", { name: "中心节点" }).dataset.expanded).toBe("true");

        await act(async () => {
            await vi.advanceTimersByTimeAsync(10_800);
        });
        expect(screen.getByRole("button", { name: "节点 99" })).toBeTruthy();
        expect(screen.queryByRole("button", { name: "节点 100" })).toBeNull();
        expect(screen.getAllByRole("button")).toHaveLength(100);
    });

    it("selects the visible node with the highest source degree first", async () => {
        vi.useFakeTimers();
        const fillerEdges = Array.from({ length: 38 }, (_, index) => ({
            id: `filler-${index}`,
            relationType: "关联",
            sourceNodeId: `filler-source-${index}`,
            targetNodeId: `filler-target-${index}`
        }));
        const nodes = [
            { id: "low", name: "低度节点", nodeType: "CONCEPT" },
            { id: "low-visible", name: "低度已显示", nodeType: "CONCEPT" },
            { id: "high", name: "核心节点", nodeType: "CONCEPT" },
            { id: "high-visible", name: "核心已显示", nodeType: "CONCEPT" },
            ...fillerEdges.flatMap((edge, index) => [
                { id: edge.sourceNodeId, name: `填充源 ${index}`, nodeType: "CONCEPT" },
                { id: edge.targetNodeId, name: `填充目标 ${index}`, nodeType: "CONCEPT" }
            ]),
            { id: "low-new", name: "低度新节点", nodeType: "CONCEPT" },
            ...Array.from({ length: 3 }, (_, index) => ({
                id: `high-new-${index}`,
                name: `核心新节点 ${index}`,
                nodeType: "CONCEPT"
            }))
        ];
        const edges = [
            {
                id: "low-visible",
                sourceNodeId: "low",
                targetNodeId: "low-visible",
                relationType: "关联"
            },
            {
                id: "high-visible",
                sourceNodeId: "high",
                targetNodeId: "high-visible",
                relationType: "关联"
            },
            ...fillerEdges,
            {
                id: "low-hidden",
                sourceNodeId: "low",
                targetNodeId: "low-new",
                relationType: "关联"
            },
            ...Array.from({ length: 3 }, (_, index) => ({
                id: `high-hidden-${index}`,
                sourceNodeId: "high",
                targetNodeId: `high-new-${index}`,
                relationType: "关联"
            }))
        ];
        vi.spyOn(globalThis, "fetch").mockImplementation((input) => {
            if (String(input).includes("overview/get")) {
                return response({
                    coveredMaterialCount: "1",
                    isolatedNodeCount: "0",
                    publishedEdgeCount: String(edges.length),
                    publishedNodeCount: String(nodes.length)
                });
            }
            if (String(input).includes("recent-edges/list")) return response({ edges, nodes });
            return response({ nodes: [], edges: [], nextCursor: null, truncated: false });
        });
        const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
        render(
            <QueryClientProvider client={client}>
                <MemoryRouter>
                    <KnowledgeAtlasPage />
                </MemoryRouter>
            </QueryClientProvider>
        );

        await act(async () => {
            await vi.advanceTimersByTimeAsync(0);
        });
        await act(async () => {
            await vi.advanceTimersByTimeAsync(1_200);
        });
        expect(screen.getByRole("button", { name: "核心新节点 0" })).toBeTruthy();
        expect(screen.queryByRole("button", { name: "低度新节点" })).toBeNull();
    });
});
