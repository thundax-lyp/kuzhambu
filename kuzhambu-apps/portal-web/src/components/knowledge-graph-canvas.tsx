import dagre from "@dagrejs/dagre";
import { BookOpen, CircleDot, Landmark, MapPin, UserRound } from "lucide-react";
import {
    Background,
    BackgroundVariant,
    BaseEdge,
    Controls,
    Handle,
    MarkerType,
    Position,
    ReactFlow,
    type Edge,
    type EdgeProps,
    type Node,
    type NodeProps
} from "@xyflow/react";
import { useMemo } from "react";
import {
    readKnowledgeGraphNodeTypeLabel,
    readKnowledgeGraphRelationLabel
} from "@/components/knowledge-graph-labels";

import "@xyflow/react/dist/style.css";
import "./knowledge-graph-canvas.css";

export interface KnowledgeGraphCanvasNode {
    id: string;
    name?: string | null;
    nodeType?: string | null;
}

export interface KnowledgeGraphCanvasEdge {
    id: string;
    label?: string | null;
    relationType?: string | null;
    sourceNodeId?: string | null;
    targetNodeId?: string | null;
}

export interface KnowledgeGraphCanvasData {
    edges: KnowledgeGraphCanvasEdge[];
    nodes: KnowledgeGraphCanvasNode[];
}

interface KnowledgeGraphNodeData extends Record<string, unknown> {
    dimmed: boolean;
    label: string;
    matched: boolean;
    nodeType: string;
}

interface KnowledgeGraphEdgeData extends Record<string, unknown> {
    sourceCenter: { x: number; y: number };
    targetCenter: { x: number; y: number };
}

const NODE_SIZE = { height: 28, width: 28 };
const LAYOUT_NODE_SIZE = { height: 64, width: 112 };

const nodeStyle = (nodeType?: string | null) => {
    const styles: Record<string, string> = {
        ANIMAL: "nature",
        BUILDING: "building",
        CELESTIAL_BODY: "celestial",
        DEITY: "person",
        EVENT: "event",
        NATURAL_PHENOMENON: "nature",
        PERSON: "person",
        PLACE: "place",
        PLANT: "nature",
        RITUAL: "work",
        WORK: "work"
    };
    return styles[nodeType || ""] || "default";
};

const nodeIcon = (nodeType: string) => {
    if (nodeType === "BUILDING") return <Landmark aria-hidden="true" size={14} />;
    if (nodeType === "PERSON" || nodeType === "DEITY")
        return <UserRound aria-hidden="true" size={14} />;
    if (nodeType === "PLACE") return <MapPin aria-hidden="true" size={14} />;
    if (nodeType === "WORK" || nodeType === "RITUAL")
        return <BookOpen aria-hidden="true" size={14} />;
    return <CircleDot aria-hidden="true" size={14} />;
};

const KnowledgeGraphNode = ({ data, selected }: NodeProps<Node<KnowledgeGraphNodeData>>) => {
    return (
        <div
            className={[
                "published-graph-node",
                `published-graph-node--${nodeStyle(data.nodeType)}`,
                selected ? "is-selected" : "",
                data.matched ? "is-matched" : "",
                data.dimmed ? "is-dimmed" : ""
            ]
                .filter(Boolean)
                .join(" ")}
            title={`${readKnowledgeGraphNodeTypeLabel(data.nodeType)}：${data.label}`}
        >
            <Handle isConnectable={false} position={Position.Left} type="target" />
            {nodeIcon(data.nodeType)}
            <strong>{data.label}</strong>
            <Handle isConnectable={false} position={Position.Right} type="source" />
        </div>
    );
};

const nodeTypes = { publishedGraph: KnowledgeGraphNode };

/** 关系线固定连接两个节点的中心点，并以直线呈现。 */
const KnowledgeGraphStraightEdge = ({
    data,
    label,
    markerEnd
}: EdgeProps<Edge<KnowledgeGraphEdgeData>>) => {
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
                    className="published-graph-edge-label"
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

const edgeTypes = { publishedGraphStraight: KnowledgeGraphStraightEdge };

const createDagrePositions = (graph: KnowledgeGraphCanvasData) => {
    const dagreGraph = new dagre.graphlib.Graph();
    dagreGraph.setDefaultEdgeLabel(() => ({}));
    dagreGraph.setGraph({ marginx: 40, marginy: 36, nodesep: 44, rankdir: "LR", ranksep: 82 });
    graph.nodes.forEach((node) =>
        dagreGraph.setNode(node.id, {
            height: LAYOUT_NODE_SIZE.height,
            width: LAYOUT_NODE_SIZE.width
        })
    );
    graph.edges.forEach((edge) => {
        if (edge.sourceNodeId && edge.targetNodeId) {
            dagreGraph.setEdge(edge.sourceNodeId, edge.targetNodeId);
        }
    });
    dagre.layout(dagreGraph);
    return new Map(
        graph.nodes.map((node) => {
            const position = dagreGraph.node(node.id);
            return [
                node.id,
                {
                    x: (position?.x ?? 0) - NODE_SIZE.width / 2,
                    y: (position?.y ?? 0) - NODE_SIZE.height / 2
                }
            ];
        })
    );
};

const createNeighborIds = (edges: KnowledgeGraphCanvasEdge[], selectedNodeId?: string | null) => {
    const ids = new Set<string>();
    if (!selectedNodeId) return ids;
    ids.add(selectedNodeId);
    edges.forEach((edge) => {
        if (edge.sourceNodeId === selectedNodeId && edge.targetNodeId) ids.add(edge.targetNodeId);
        if (edge.targetNodeId === selectedNodeId && edge.sourceNodeId) ids.add(edge.sourceNodeId);
    });
    return ids;
};

const createFlowElements = (
    graph: KnowledgeGraphCanvasData,
    nodePositions: ReadonlyMap<string, { x: number; y: number }> | undefined,
    searchKeyword: string,
    selectedNodeId?: string | null
) => {
    const positions = nodePositions || createDagrePositions(graph);
    const neighbors = createNeighborIds(graph.edges, selectedNodeId);
    const normalizedKeyword = searchKeyword.trim().toLocaleLowerCase("zh-CN");
    const nodes: Node<KnowledgeGraphNodeData>[] = graph.nodes.flatMap((node) => {
        const position = positions.get(node.id);
        if (!position) return [];
        return [
            {
                data: {
                    dimmed: Boolean(selectedNodeId && !neighbors.has(node.id)),
                    label: node.name || "未命名对象",
                    matched: Boolean(
                        normalizedKeyword &&
                        node.name?.toLocaleLowerCase("zh-CN").includes(normalizedKeyword)
                    ),
                    nodeType: node.nodeType || "OBJECT"
                },
                id: node.id,
                position,
                selected: node.id === selectedNodeId,
                type: "publishedGraph"
            }
        ];
    });
    const nodeCenters = new Map(
        nodes.map((node) => [
            node.id,
            {
                x: node.position.x + NODE_SIZE.width / 2,
                y: node.position.y + NODE_SIZE.height / 2
            }
        ])
    );
    const edges: Edge<KnowledgeGraphEdgeData>[] = graph.edges.flatMap((edge) => {
        const sourceNodeId = edge.sourceNodeId || "";
        const targetNodeId = edge.targetNodeId || "";
        const sourceCenter = nodeCenters.get(sourceNodeId);
        const targetCenter = nodeCenters.get(targetNodeId);
        if (!sourceCenter || !targetCenter) return [];
        const connected =
            !selectedNodeId || sourceNodeId === selectedNodeId || targetNodeId === selectedNodeId;
        return [
            {
                className: connected ? "" : "is-dimmed",
                data: { sourceCenter, targetCenter },
                id: edge.id,
                label: edge.label || readKnowledgeGraphRelationLabel(edge.relationType),
                markerEnd: { type: MarkerType.ArrowClosed },
                source: sourceNodeId,
                target: targetNodeId,
                type: "publishedGraphStraight"
            }
        ];
    });
    return { edges, nodes };
};

export const KnowledgeGraphCanvas = ({
    ariaLabel,
    graph,
    nodePositions,
    searchKeyword = "",
    selectedNodeId,
    onNodeClick,
    onNodeDoubleClick
}: {
    ariaLabel: string;
    graph: KnowledgeGraphCanvasData;
    nodePositions?: ReadonlyMap<string, { x: number; y: number }>;
    searchKeyword?: string;
    selectedNodeId?: string | null;
    onNodeClick?: (nodeId: string) => void;
    onNodeDoubleClick?: (nodeId: string) => void;
}) => {
    const elements = useMemo(
        () => createFlowElements(graph, nodePositions, searchKeyword, selectedNodeId),
        [graph, nodePositions, searchKeyword, selectedNodeId]
    );

    return (
        <div className="published-graph-canvas" aria-label={ariaLabel}>
            <ReactFlow
                fitView
                fitViewOptions={{ duration: 520, padding: 0.08 }}
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
                onNodeClick={onNodeClick ? (_, node) => onNodeClick(node.id) : undefined}
                onNodeDoubleClick={
                    onNodeDoubleClick ? (_, node) => onNodeDoubleClick(node.id) : undefined
                }
            >
                <Background color="#d8cdb9" gap={22} variant={BackgroundVariant.Dots} />
                <Controls showInteractive={false} />
            </ReactFlow>
        </div>
    );
};
