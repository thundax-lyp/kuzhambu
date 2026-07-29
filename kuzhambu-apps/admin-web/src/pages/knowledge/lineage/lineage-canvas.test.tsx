import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { LineageCanvas } from "./lineage-canvas";
import type { LineageNodeRecord, LineageRelationRecord } from "./lineage-types";

const nodes: LineageNodeRecord[] = [
    {
        id: "lineage-node:1",
        nodeId: "1",
        nodeKey: "person:1",
        name: "贾代善",
        nodeType: "PERSON",
        generation: 1,
        confirmationStatus: "CONFIRMED",
        sourceRefs: []
    },
    {
        id: "lineage-node:2",
        nodeId: "2",
        nodeKey: "person:2",
        name: "贾政",
        nodeType: "PERSON",
        generation: 2,
        confirmationStatus: "CONFIRMED",
        sourceRefs: []
    }
];

const relations: LineageRelationRecord[] = [
    {
        id: "lineage-relation:10",
        relationId: "10",
        sourceNodeId: "1",
        sourceNodeName: "贾代善",
        targetNodeId: "2",
        targetNodeName: "贾政",
        relationType: "PARENT_CHILD",
        relationLabel: "父子",
        confirmationStatus: "CONFIRMED",
        sourceRefs: []
    }
];

describe("LineageCanvas", () => {
    it("selects nodes and relations from the canvas", () => {
        const onSelectNode = vi.fn();
        const onSelectRelation = vi.fn();

        render(
            <LineageCanvas
                nodes={nodes}
                relations={relations}
                selectedNodeId="1"
                selectedRelationId={null}
                onSelectNode={onSelectNode}
                onSelectRelation={onSelectRelation}
            />
        );

        fireEvent.click(screen.getByText("贾政"));
        expect(onSelectNode).toHaveBeenCalledWith(
            expect.objectContaining({
                name: "贾政",
                nodeId: "2"
            })
        );

        fireEvent.click(screen.getByText("父子"));
        expect(onSelectRelation).toHaveBeenCalledWith(relations[0]);
    });

    it("resets the viewport through fit view control", () => {
        render(
            <LineageCanvas
                nodes={nodes}
                relations={relations}
                selectedNodeId={null}
                selectedRelationId={null}
                onSelectNode={vi.fn()}
                onSelectRelation={vi.fn()}
            />
        );

        fireEvent.click(screen.getByTestId("knowledge-lineage-lineage-canvas-action-button-3"));
        expect(screen.getByRole("img", { name: "世系图画布" })).toBeInTheDocument();
    });
});
