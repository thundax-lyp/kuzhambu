import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { act } from "react";
import type { ReactNode } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KnowledgeLineagePage } from "./knowledge-lineage-page";

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
        nodes,
        onNodeClick
    }: {
        children: ReactNode;
        nodes: { data: { label?: string }; id: string }[];
        onNodeClick?: (event: unknown, node: { data: { label?: string }; id: string }) => void;
    }) => (
        <div data-testid="lineage-flow">
            {nodes.map((node) => (
                <button key={node.id} type="button" onClick={() => onNodeClick?.({}, node)}>
                    {node.data.label}
                </button>
            ))}
            {children}
        </div>
    )
}));
/* eslint-enable @typescript-eslint/naming-convention */

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

        expect(container.textContent).toContain("世系图浏览");
        expect(container.textContent).toContain("返回知识馆");
        expect(container.textContent).toContain("搜索人物、谱系节点或关系");
        expect(container.textContent).toContain("清除筛选");
        expect(container.textContent).not.toContain("确认");
        expect(container.textContent).not.toContain("删除");

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

        act(() => {
            root.unmount();
        });
    });
});
