import { BookOpen, CircleDot, Landmark, MapPin, UserRound } from "lucide-react";
import {
    Background,
    BackgroundVariant,
    BaseEdge,
    Controls,
    MarkerType,
    Position,
    ReactFlow,
    Handle,
    type Edge,
    type EdgeProps,
    type Node,
    type NodeProps
} from "@xyflow/react";
import { useMemo } from "react";
import { ATLAS_NODE_SIZE, layoutAtlasForceGraph } from "./atlas-force-layout";
import type { AtlasGraphRecord } from "./atlas-workbench-types";

import "@xyflow/react/dist/style.css";
import "./atlas-workbench-canvas.css";

interface AtlasNodeData extends Record<string, unknown> {
    label: string;
    nodeType: string;
}

interface AtlasEdgeData extends Record<string, unknown> {
    sourceCenter: { x: number; y: number };
    targetCenter: { x: number; y: number };
}

const nodeStyle = (nodeType?: string | null) => {
    const styles: Record<string, string> = {
        BUILDING: "building",
        EVENT: "event",
        PERSON: "person",
        PLACE: "place",
        WORK: "work"
    };
    return styles[nodeType || ""] || "default";
};

const nodeIcon = (nodeType: string) => {
    if (nodeType === "BUILDING") return <Landmark aria-hidden="true" size={14} />;
    if (nodeType === "PERSON") return <UserRound aria-hidden="true" size={14} />;
    if (nodeType === "PLACE") return <MapPin aria-hidden="true" size={14} />;
    if (nodeType === "WORK") return <BookOpen aria-hidden="true" size={14} />;
    return <CircleDot aria-hidden="true" size={14} />;
};

const AtlasNode = ({ data }: NodeProps<Node<AtlasNodeData>>) => {
    return (
        <div className={`atlas-workbench-node atlas-workbench-node--${nodeStyle(data.nodeType)}`}>
            <Handle isConnectable={false} position={Position.Left} type="target" />
            {nodeIcon(data.nodeType)}
            <strong className="atlas-workbench-node-label">{data.label}</strong>
            <Handle isConnectable={false} position={Position.Right} type="source" />
        </div>
    );
};

const nodeTypes = { atlas: AtlasNode };

/** 关系线固定连接两个节点的中心点，并以直线呈现。 */
const AtlasStraightEdge = ({ data, label, markerEnd }: EdgeProps<Edge<AtlasEdgeData>>) => {
    if (!data) return null;
    const { sourceCenter, targetCenter } = data;
    const path = `M ${sourceCenter.x},${sourceCenter.y} L ${targetCenter.x},${targetCenter.y}`;
    const labelX = (sourceCenter.x + targetCenter.x) / 2;
    const labelY = (sourceCenter.y + targetCenter.y) / 2;
    return (
        <>
            <BaseEdge markerEnd={markerEnd} path={path} />
            {label ? (
                <text
                    className="atlas-workbench-edge-label"
                    textAnchor="middle"
                    x={labelX}
                    y={labelY}
                >
                    {String(label)}
                </text>
            ) : null}
        </>
    );
};

const edgeTypes = { atlasStraight: AtlasStraightEdge };

const toFlowElements = (graph: AtlasGraphRecord) => {
    const layout = layoutAtlasForceGraph(graph);
    const nodes: Node<AtlasNodeData>[] = layout.nodes.map(({ id, node, position }) => ({
        data: { label: node.name || "未命名对象", nodeType: node.nodeType || "OBJECT" },
        id,
        position,
        type: "atlas"
    }));
    const nodeCenters = new Map(
        layout.nodes.map(({ id, position }) => [
            id,
            {
                x: position.x + ATLAS_NODE_SIZE.width / 2,
                y: position.y + ATLAS_NODE_SIZE.height / 2
            }
        ])
    );
    const edges: Edge<AtlasEdgeData>[] = layout.edges.flatMap((edge) => {
        const sourceCenter = nodeCenters.get(edge.sourceNodeId || "");
        const targetCenter = nodeCenters.get(edge.targetNodeId || "");
        if (!sourceCenter || !targetCenter) return [];
        return [
            {
                data: { sourceCenter, targetCenter },
                id: edge.id,
                label: edge.relationLabel,
                markerEnd: { type: MarkerType.ArrowClosed },
                source: edge.sourceNodeId || "",
                target: edge.targetNodeId || "",
                type: "atlasStraight"
            }
        ];
    });
    return { edges, nodes };
};

export const AtlasWorkbenchCanvas = ({
    graph,
    loading
}: {
    graph: AtlasGraphRecord;
    loading: boolean;
}) => {
    const elements = useMemo(() => toFlowElements(graph), [graph]);
    if (graph.edges.length === 0 && !loading)
        return <p className="atlas-workbench-empty">暂无可展示的关系图谱。</p>;
    return (
        <section aria-label="三才图会总谱预览" className="atlas-workbench-canvas">
            <ReactFlow
                fitView
                fitViewOptions={{ duration: 520, padding: 0.06 }}
                edges={elements.edges}
                edgesFocusable={false}
                edgeTypes={edgeTypes}
                maxZoom={1.6}
                minZoom={0.3}
                nodes={elements.nodes}
                nodesConnectable={false}
                nodesDraggable={false}
                nodeTypes={nodeTypes}
                panOnDrag
                proOptions={{ hideAttribution: true }}
                zoomOnDoubleClick={false}
            >
                <Background color="#d8cdb9" gap={22} variant={BackgroundVariant.Dots} />
                <Controls showInteractive={false} />
            </ReactFlow>
            {loading ? <p className="atlas-workbench-loading">正在扩展总谱关系…</p> : null}
        </section>
    );
};
