import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen, waitFor } from "@testing-library/react";
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
    ReactFlow: ({ children }: { children: React.ReactNode }) => <div>{children}</div>
}));

const response = (data: unknown) =>
    Promise.resolve(new Response(JSON.stringify({ code: "COMMON-00000", data }), { status: 200 }));

afterEach(() => vi.restoreAllMocks());

describe("KnowledgeAtlasPage", () => {
    it("shows the workbench metrics and progressively loaded graph preview", async () => {
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
    });
});
