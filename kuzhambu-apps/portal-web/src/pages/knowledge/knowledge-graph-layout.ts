import dagre from "@dagrejs/dagre";
import { MarkerType, type Edge, type Node } from "@xyflow/react";
import type {
    KnowledgeAtlasCanvasEdge,
    KnowledgeAtlasCanvasNode,
    KnowledgeAtlasCanvasView
} from "./knowledge-atlas-types";

export interface KnowledgeGraphNodeData extends Record<string, unknown> {
    label: string;
    subtitle: string | null;
    metricLabel: string | null;
    metricValue: number | null;
    status: string | null;
    href: string | null;
    kind: string;
}

export interface KnowledgeGraphElements {
    nodes: Node<KnowledgeGraphNodeData>[];
    edges: Edge[];
}

const NODE_WIDTH = 188;
const NODE_HEIGHT = 86;
const OVERVIEW_OFFSET_X = 360;
const OVERVIEW_OFFSET_Y = 320;

const toNodeData = (node: KnowledgeAtlasCanvasNode): KnowledgeGraphNodeData => ({
    href: node.href,
    kind: node.kind,
    label: node.label,
    metricLabel: node.metricLabel,
    metricValue: node.metricValue,
    status: node.status,
    subtitle: node.subtitle
});

const toReactFlowEdge = (edge: KnowledgeAtlasCanvasEdge): Edge => ({
    animated: false,
    id: edge.id,
    label: edge.label ?? undefined,
    markerEnd: {
        type: MarkerType.ArrowClosed
    },
    source: edge.source,
    style: edge.dashed
        ? {
              strokeDasharray: "6 5"
          }
        : undefined,
    target: edge.target,
    type: "smoothstep"
});

const layoutOverviewNodes = (
    canvasView: KnowledgeAtlasCanvasView
): Node<KnowledgeGraphNodeData>[] =>
    canvasView.nodes.map((node) => ({
        data: toNodeData(node),
        id: node.id,
        position: {
            x: (node.x ?? 0) + OVERVIEW_OFFSET_X,
            y: (node.y ?? 0) + OVERVIEW_OFFSET_Y
        },
        type: "knowledge"
    }));

const layoutDagreNodes = (canvasView: KnowledgeAtlasCanvasView): Node<KnowledgeGraphNodeData>[] => {
    const graph = new dagre.graphlib.Graph();
    graph.setDefaultEdgeLabel(() => ({}));
    graph.setGraph({
        marginx: 40,
        marginy: 28,
        nodesep: 46,
        rankdir: "LR",
        ranksep: 88
    });

    canvasView.nodes.forEach((node) => {
        graph.setNode(node.id, {
            height: NODE_HEIGHT,
            width: NODE_WIDTH
        });
    });
    canvasView.edges.forEach((edge) => {
        graph.setEdge(edge.source, edge.target);
    });

    dagre.layout(graph);

    return canvasView.nodes.map((node) => {
        const layoutNode = graph.node(node.id);
        return {
            data: toNodeData(node),
            id: node.id,
            position: {
                x: (layoutNode?.x ?? 0) - NODE_WIDTH / 2,
                y: (layoutNode?.y ?? 0) - NODE_HEIGHT / 2
            },
            type: "knowledge"
        };
    });
};

export const layoutKnowledgeGraph = (
    canvasView: KnowledgeAtlasCanvasView | null
): KnowledgeGraphElements => {
    if (!canvasView) {
        return {
            edges: [],
            nodes: []
        };
    }

    return {
        edges: canvasView.edges.map(toReactFlowEdge),
        nodes:
            canvasView.mode === "overview"
                ? layoutOverviewNodes(canvasView)
                : layoutDagreNodes(canvasView)
    };
};
