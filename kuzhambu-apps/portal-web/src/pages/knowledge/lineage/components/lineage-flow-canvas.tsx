import {
    Background,
    BackgroundVariant,
    Controls,
    Handle,
    MiniMap,
    Position,
    ReactFlow,
    type Edge,
    type Node,
    type NodeProps
} from "@xyflow/react";
import { useMemo } from "react";
import type { KnowledgeLineageCanvasRecord, KnowledgeLineageNodeRecord } from "../lineage-types";

interface FlowData extends Record<string, unknown> {
    label: string;
    meta: string;
    status?: string | null;
}

interface LineageFlowCanvasProps {
    canvas: KnowledgeLineageCanvasRecord;
    emptyText?: string | null;
    fetching: boolean;
    selectedNodeId?: number | null;
    selectedRelationId?: number | null;
    onSelectNode: (nodeId: number) => void;
    onSelectRelation: (relationId: number) => void;
}

export const LineageFlowCanvas = ({
    canvas,
    emptyText,
    fetching,
    selectedNodeId,
    selectedRelationId,
    onSelectNode,
    onSelectRelation
}: LineageFlowCanvasProps) => {
    const flow = useMemo(
        () => buildFlow(canvas, selectedNodeId, selectedRelationId),
        [canvas, selectedNodeId, selectedRelationId]
    );

    return (
        <div className="knowledge-lineage-flow-frame">
            <ReactFlow
                fitView
                edges={flow.edges}
                edgesFocusable={false}
                elementsSelectable
                maxZoom={1.6}
                minZoom={0.35}
                nodes={flow.nodes}
                nodesConnectable={false}
                nodesDraggable={false}
                nodeTypes={nodeTypes}
                panOnDrag
                proOptions={{ hideAttribution: true }}
                zoomOnDoubleClick={false}
                onEdgeClick={(_, edge) => onSelectRelation(Number(edge.id))}
                onNodeClick={(_, node) => onSelectNode(Number(node.id))}
            >
                <Background color="#d4ddd8" gap={24} variant={BackgroundVariant.Dots} />
                <MiniMap nodeBorderRadius={8} pannable zoomable />
                <Controls showInteractive={false} />
            </ReactFlow>
            {fetching ? <div className="knowledge-lineage-loading">正在读取世系图</div> : null}
            {emptyText ? (
                <div className="knowledge-lineage-empty">
                    <h3>{canvas.empty?.title}</h3>
                    <p>{emptyText}</p>
                </div>
            ) : null}
        </div>
    );
};

const LineageFlowCard = ({ data, selected }: NodeProps<Node<FlowData>>) => {
    return (
        <div className={["knowledge-lineage-flow-card", selected ? "is-selected" : ""].join(" ")}>
            <Handle isConnectable={false} position={Position.Left} type="target" />
            <Handle isConnectable={false} position={Position.Right} type="source" />
            <span>{data.status || "UNCONFIRMED"}</span>
            <strong>{data.label}</strong>
            <small>{data.meta}</small>
        </div>
    );
};

const nodeTypes = {
    lineage: LineageFlowCard
};

const buildFlow = (
    canvas: KnowledgeLineageCanvasRecord,
    selectedNodeId?: number | null,
    selectedRelationId?: number | null
) => {
    const groupedNodes = canvas.nodes.reduce<Map<number, KnowledgeLineageNodeRecord[]>>(
        (groups, node, index) => {
            const generation = node.generation ?? index;
            const current = groups.get(generation) || [];
            groups.set(generation, [...current, node]);
            return groups;
        },
        new Map()
    );
    const generations = Array.from(groupedNodes.keys()).sort((left, right) => left - right);
    const nodes: Node<FlowData>[] = generations.flatMap((generation, columnIndex) => {
        const group = groupedNodes.get(generation) || [];
        return group.map((node, rowIndex) => ({
            data: {
                label: node.name || node.nodeKey || `节点 ${node.nodeId}`,
                meta: `${node.nodeType || "未分类"} / ${node.generation ?? "-"}`,
                status: node.confirmationStatus
            },
            id: String(node.nodeId),
            position: {
                x: node.x ?? columnIndex * 240,
                y: node.y ?? rowIndex * 130
            },
            selected: node.nodeId === selectedNodeId,
            type: "lineage"
        }));
    });
    const edges: Edge[] = canvas.relations
        .filter((relation) => relation.sourceNodeId != null && relation.targetNodeId != null)
        .map((relation) => ({
            id: String(relation.relationId),
            label: relation.relationLabel || relation.relationType || "关系",
            source: String(relation.sourceNodeId),
            target: String(relation.targetNodeId),
            selected: relation.relationId === selectedRelationId,
            type: "default"
        }));

    return { edges, nodes };
};
