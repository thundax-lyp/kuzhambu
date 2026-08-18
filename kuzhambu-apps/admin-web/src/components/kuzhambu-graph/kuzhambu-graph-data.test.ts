import { describe, expect, it } from "vitest";
import { buildKuzhambuGraphData } from "./kuzhambu-graph-data";

describe("buildKuzhambuGraphData", () => {
    it("keeps isolated nodes when relation data is present", () => {
        const graphData = buildKuzhambuGraphData(
            [
                {
                    object: "客体",
                    objectId: "node-2",
                    predicate: "提及",
                    subject: "主体",
                    subjectId: "node-1"
                }
            ],
            [
                { id: "node-1", label: "主体" },
                { id: "node-2", label: "客体" },
                { id: "node-3", label: "孤立节点" }
            ]
        );

        expect(graphData.nodes?.map((node) => node.id)).toContain("node-3");
        expect(graphData.edges).toContainEqual(
            expect.objectContaining({ source: "node-1", target: "node-2" })
        );
    });
});
