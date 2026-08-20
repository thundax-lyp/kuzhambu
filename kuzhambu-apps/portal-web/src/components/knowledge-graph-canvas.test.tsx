import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { KnowledgeGraphCanvas } from "./knowledge-graph-canvas";

/* eslint-disable @typescript-eslint/naming-convention */
vi.mock("@xyflow/react", () => ({
    Background: () => null,
    BackgroundVariant: { Dots: "dots" },
    BaseEdge: () => null,
    Controls: () => null,
    Handle: () => null,
    MarkerType: { ArrowClosed: "arrowclosed" },
    Position: { Left: "left", Right: "right" },
    ReactFlow: ({
        children,
        nodes
    }: {
        children: React.ReactNode;
        nodes: {
            data: { entryOffset?: { x: number; y: number } };
            id: string;
            position: { x: number; y: number };
        }[];
    }) => (
        <div>
            {nodes.map((node) => (
                <span
                    key={node.id}
                    data-entry-offset={
                        node.data.entryOffset
                            ? `${node.data.entryOffset.x},${node.data.entryOffset.y}`
                            : undefined
                    }
                    data-testid={`shared-graph-node-${node.id}`}
                    data-position={`${node.position.x},${node.position.y}`}
                />
            ))}
            {children}
        </div>
    )
}));

describe("KnowledgeGraphCanvas", () => {
    it("calculates an independent position for every automatically laid out node", () => {
        render(
            <KnowledgeGraphCanvas
                ariaLabel="测试图谱"
                graph={{
                    nodes: [
                        { id: "1", name: "天", nodeType: "CONCEPT" },
                        { id: "2", name: "日", nodeType: "CELESTIAL_BODY" },
                        { id: "3", name: "阳气", nodeType: "NATURAL_PHENOMENON" }
                    ],
                    edges: [
                        { id: "11", sourceNodeId: "2", targetNodeId: "1" },
                        { id: "12", sourceNodeId: "2", targetNodeId: "3" }
                    ]
                }}
            />
        );

        const positions = ["1", "2", "3"].map((id) =>
            screen.getByTestId(`shared-graph-node-${id}`).getAttribute("data-position")
        );
        expect(new Set(positions).size).toBe(3);
    });

    it("passes a new node entry offset to the renderer", () => {
        render(
            <KnowledgeGraphCanvas
                ariaLabel="发射动画图谱"
                graph={{
                    nodes: [
                        { id: "1", name: "已有节点" },
                        { id: "2", entryOffset: { x: -120, y: 24 }, name: "新节点" }
                    ],
                    edges: [{ id: "11", sourceNodeId: "1", targetNodeId: "2" }]
                }}
            />
        );

        expect(screen.getByTestId("shared-graph-node-2").dataset.entryOffset).toBe("-120,24");
    });
});
