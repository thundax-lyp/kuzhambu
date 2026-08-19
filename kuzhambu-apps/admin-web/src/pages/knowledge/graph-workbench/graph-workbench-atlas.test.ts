import { describe, expect, it } from "vitest";
import { prioritizeGraphForProjection } from "./hooks/use-graph-workbench-atlas";

describe("prioritizeGraphForProjection", () => {
    it("projects edges attached to the most connected node before peripheral edges", () => {
        const graph = prioritizeGraphForProjection({
            nodes: ["a", "b", "c", "d", "x", "y"].map((id) => ({ id })),
            edges: [
                { id: "edge-b-a", sourceNodeId: "b", targetNodeId: "a" },
                { id: "edge-b-c", sourceNodeId: "b", targetNodeId: "c" },
                { id: "edge-b-d", sourceNodeId: "b", targetNodeId: "d" },
                { id: "edge-x-y", sourceNodeId: "x", targetNodeId: "y" }
            ]
        });

        expect(graph.edges.map((edge) => edge.id)).toEqual([
            "edge-b-a",
            "edge-b-c",
            "edge-b-d",
            "edge-x-y"
        ]);
    });
});
