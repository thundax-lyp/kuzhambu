import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, waitFor } from "@testing-library/react";
import { act } from "react";
import type { ReactNode } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KnowledgeLineagePage } from "./lineage-page";

const { getKnowledgeLineage } = vi.hoisted(() => ({
    getKnowledgeLineage: vi.fn()
}));

/* eslint-disable @typescript-eslint/naming-convention */
vi.mock("@xyflow/react", () => ({
    Background: () => <div data-testid="lineage-background" />,
    BackgroundVariant: { Dots: "dots" },
    Controls: () => <div data-testid="lineage-controls" />,
    Handle: () => <span data-testid="lineage-handle" />,
    MiniMap: () => <div data-testid="lineage-minimap" />,
    Position: { Left: "left", Right: "right" },
    ReactFlow: ({
        children,
        edges,
        onEdgeClick,
        nodes,
        onNodeClick
    }: {
        edges?: { id: string; label?: string }[];
        children: ReactNode;
        nodes: { data: { label?: string }; id: string }[];
        onEdgeClick?: (event: unknown, edge: { id: string; label?: string }) => void;
        onNodeClick?: (event: unknown, node: { data: { label?: string }; id: string }) => void;
    }) => (
        <div data-testid="lineage-flow">
            {nodes.map((node) => (
                <button key={node.id} type="button" onClick={() => onNodeClick?.({}, node)}>
                    {node.data.label}
                </button>
            ))}
            {(edges ?? []).map((edge) => (
                <button key={edge.id} type="button" onClick={() => onEdgeClick?.({}, edge)}>
                    {edge.label}
                </button>
            ))}
            {children}
        </div>
    )
}));
/* eslint-enable @typescript-eslint/naming-convention */

vi.mock("./lineage-service", () => ({
    getKnowledgeLineage
}));

const renderPage = () => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                gcTime: 0,
                retry: false
            }
        }
    });

    act(() => {
        root.render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter>
                    <KnowledgeLineagePage />
                </MemoryRouter>
            </QueryClientProvider>
        );
    });

    return { container, root };
};

const flushQuery = async () => {
    await act(async () => {
        await Promise.resolve();
        await new Promise((resolve) => {
            window.setTimeout(resolve, 0);
        });
        await Promise.resolve();
        await new Promise((resolve) => {
            window.setTimeout(resolve, 0);
        });
    });
};

const canvas = {
    version: {
        versionId: 71,
        versionNo: 3,
        taskType: "LINEAGE",
        status: "APPLIED",
        sourceCategoryName: "红楼世系"
    },
    summary: {
        nodeCount: 2,
        relationCount: 1,
        confirmedNodeCount: 2,
        confirmedRelationCount: 1
    },
    nodes: [
        {
            id: "lineage-node:1",
            nodeId: 1,
            nodeKey: "person:1",
            name: "贾代善",
            nodeType: "PERSON",
            generation: 1,
            confirmationStatus: "CONFIRMED",
            sourceRefs: []
        },
        {
            id: "lineage-node:2",
            nodeId: 2,
            nodeKey: "person:2",
            name: "贾政",
            nodeType: "PERSON",
            generation: 2,
            confirmationStatus: "CONFIRMED",
            sourceRefs: []
        }
    ],
    relations: [
        {
            id: "lineage-relation:10",
            relationId: 10,
            sourceNodeId: 1,
            sourceNodeName: "贾代善",
            targetNodeId: 2,
            targetNodeName: "贾政",
            relationType: "PARENT_CHILD",
            relationLabel: "父子",
            confirmationStatus: "CONFIRMED",
            sourceRefs: []
        }
    ],
    selectedNode: null,
    selectedRelation: null,
    availableFilters: {
        versions: [
            {
                versionId: 71,
                versionNo: 3,
                sourceCategoryName: "红楼世系"
            }
        ],
        nodeTypes: ["PERSON"],
        relationTypes: ["PARENT_CHILD"],
        confirmationStatuses: ["CONFIRMED"]
    },
    empty: null
};

describe("KnowledgeLineagePage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
        getKnowledgeLineage.mockReset();
    });

    it("renders readonly lineage page and selects canvas nodes", async () => {
        getKnowledgeLineage.mockResolvedValue(canvas);
        const { container, root } = renderPage();

        await flushQuery();
        await waitFor(() => {
            expect(container.textContent).toContain("贾政");
        });

        expect(container.textContent).toContain("世系图浏览");
        expect(container.textContent).toContain("返回知识馆");
        expect(
            container.querySelector('input[placeholder="搜索人物、谱系节点或关系"]')
        ).toBeTruthy();
        expect(container.textContent).toContain("清除筛选");
        expect(container.textContent).not.toContain("删除");
        expect(container.textContent).not.toContain("应用候选");

        const nodeButton = Array.from(container.querySelectorAll("button")).find(
            (button) => button.textContent === "贾政"
        );
        expect(nodeButton).toBeTruthy();

        await act(async () => {
            nodeButton?.dispatchEvent(new MouseEvent("click", { bubbles: true }));
        });

        expect(getKnowledgeLineage).toHaveBeenLastCalledWith(
            expect.objectContaining({
                focusNodeId: 2,
                focusRelationId: null
            })
        );
        expect(window.location.pathname).toBe("/");

        act(() => {
            root.unmount();
        });
    });

    it("submits filters, clears them, and selects relations without write actions", async () => {
        getKnowledgeLineage.mockResolvedValue(canvas);
        const { container, root } = renderPage();

        await flushQuery();
        await waitFor(() => {
            expect(container.textContent).toContain("贾代善");
        });

        const searchInput = container.querySelector(
            'input[placeholder="搜索人物、谱系节点或关系"]'
        );
        expect(searchInput).toBeTruthy();
        fireEvent.change(searchInput as HTMLInputElement, { target: { value: "贾政" } });
        const searchButton = Array.from(container.querySelectorAll("button")).find((button) =>
            button.textContent?.includes("搜索")
        );
        await act(async () => {
            searchButton?.dispatchEvent(new MouseEvent("click", { bubbles: true }));
        });

        await waitFor(() =>
            expect(getKnowledgeLineage).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    keyword: "贾政",
                    focusNodeId: null,
                    focusRelationId: null
                })
            )
        );

        fireEvent.change(container.querySelectorAll("select")[1], { target: { value: "PERSON" } });
        await waitFor(() =>
            expect(getKnowledgeLineage).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    nodeType: "PERSON",
                    focusNodeId: null,
                    focusRelationId: null
                })
            )
        );
        await flushQuery();
        fireEvent.change(container.querySelectorAll("select")[2], {
            target: { value: "PARENT_CHILD" }
        });
        await waitFor(() =>
            expect(getKnowledgeLineage).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    relationType: "PARENT_CHILD",
                    focusNodeId: null,
                    focusRelationId: null
                })
            )
        );
        await flushQuery();

        const relationButton = Array.from(container.querySelectorAll("button")).find((button) => {
            return button.textContent === "父子";
        });
        await act(async () => {
            fireEvent.click(relationButton as HTMLButtonElement);
        });
        await waitFor(() =>
            expect(getKnowledgeLineage).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    focusNodeId: null,
                    focusRelationId: 10
                })
            )
        );
        expect(container.textContent).not.toContain("编辑");
        expect(container.textContent).not.toContain("删除");

        const clearButton = Array.from(container.querySelectorAll("button")).find(
            (button) => button.textContent === "清除筛选"
        );
        await act(async () => {
            clearButton?.dispatchEvent(new MouseEvent("click", { bubbles: true }));
        });
        await waitFor(() =>
            expect(getKnowledgeLineage).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    keyword: null,
                    nodeType: null,
                    relationType: null,
                    focusNodeId: null,
                    focusRelationId: null
                })
            )
        );

        act(() => {
            root.unmount();
        });
    });
});
