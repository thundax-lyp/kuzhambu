import { describe, expect, it } from "vitest";
import { AtlasIncrementalLayout } from "./atlas-force-layout";
import type { AtlasGraphRecord } from "./atlas-workbench-types";

const nodes = ["a1", "a2", "b1", "b2", "c1", "c2"].map((id) => ({
    id,
    name: id,
    nodeType: "CONCEPT"
}));

const initialGraph: AtlasGraphRecord = {
    nodes,
    edges: [
        { id: "a", relationType: "ASSOCIATED_WITH", sourceNodeId: "a1", targetNodeId: "a2" },
        { id: "b", relationType: "ASSOCIATED_WITH", sourceNodeId: "b1", targetNodeId: "b2" },
        { id: "c", relationType: "ASSOCIATED_WITH", sourceNodeId: "c1", targetNodeId: "c2" }
    ]
};

const positionsById = (layout: ReturnType<AtlasIncrementalLayout["update"]>) =>
    new Map(layout.nodes.map((node) => [node.id, node.position]));

describe("AtlasIncrementalLayout", () => {
    it("merges only the clusters joined by a new edge", () => {
        const engine = new AtlasIncrementalLayout();
        const initial = engine.update(initialGraph);
        const untouchedBefore = positionsById(initial);

        expect(engine.getClusterId("a1")).not.toBe(engine.getClusterId("b1"));
        expect(engine.getHiddenGapCount()).toBe(3);

        const mergedGraph: AtlasGraphRecord = {
            nodes,
            edges: [
                ...initialGraph.edges,
                {
                    id: "a-b",
                    relationType: "ASSOCIATED_WITH",
                    sourceNodeId: "a1",
                    targetNodeId: "b1"
                }
            ]
        };
        const merged = engine.update(mergedGraph);
        const untouchedAfter = positionsById(merged);

        expect(engine.getClusterId("a1")).toBe(engine.getClusterId("b1"));
        expect(engine.getHiddenGapCount()).toBe(2);
        expect(untouchedAfter.get("c1")).toEqual(untouchedBefore.get("c1"));
        expect(untouchedAfter.get("c2")).toEqual(untouchedBefore.get("c2"));

        const beforeInternalEdge = positionsById(merged);
        const withInternalEdge = engine.update({
            nodes,
            edges: [
                ...mergedGraph.edges,
                {
                    id: "internal",
                    relationType: "ASSOCIATED_WITH",
                    sourceNodeId: "a2",
                    targetNodeId: "b2"
                }
            ]
        });
        const afterInternalEdge = positionsById(withInternalEdge);
        expect(afterInternalEdge.get("c1")).toEqual(beforeInternalEdge.get("c1"));
        expect(afterInternalEdge.get("c2")).toEqual(beforeInternalEdge.get("c2"));
        expect([afterInternalEdge.get("a2"), afterInternalEdge.get("b2")]).not.toEqual([
            beforeInternalEdge.get("a2"),
            beforeInternalEdge.get("b2")
        ]);

        const newNode = { id: "d1", name: "d1", nodeType: "CONCEPT" };
        const emitted = engine.update({
            nodes: [...nodes, newNode],
            edges: [
                ...mergedGraph.edges,
                {
                    id: "internal",
                    relationType: "ASSOCIATED_WITH",
                    sourceNodeId: "a2",
                    targetNodeId: "b2"
                },
                {
                    id: "emit",
                    relationType: "ASSOCIATED_WITH",
                    sourceNodeId: "a1",
                    targetNodeId: "d1"
                }
            ]
        });
        const emittedNode = emitted.nodes.find((node) => node.id === "d1");
        expect(emittedNode?.entryOffset).toBeTruthy();
        expect(emittedNode?.entryOffset).not.toEqual({ x: 0, y: 0 });
        expect(positionsById(emitted).get("c1")).toEqual(afterInternalEdge.get("c1"));
    });
});
