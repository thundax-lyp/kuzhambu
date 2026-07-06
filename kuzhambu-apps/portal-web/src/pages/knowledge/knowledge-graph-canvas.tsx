import {
    Background,
    BackgroundVariant,
    Controls,
    MiniMap,
    ReactFlow,
    type Node,
    type NodeProps
} from "@xyflow/react";
import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import type { KnowledgeAtlasCanvasView } from "./knowledge-atlas-types";
import { layoutKnowledgeGraph, type KnowledgeGraphNodeData } from "./knowledge-graph-layout";

import "@xyflow/react/dist/style.css";
import "./knowledge-graph-canvas.css";

interface KnowledgeGraphCanvasProps {
    canvasView: KnowledgeAtlasCanvasView | null;
}

const KnowledgeGraphNode = ({ data, selected }: NodeProps<Node<KnowledgeGraphNodeData>>) => {
    const metricText =
        data.metricLabel && data.metricValue !== null
            ? `${data.metricLabel} ${data.metricValue}`
            : data.metricLabel;
    return (
        <div
            className={[
                "knowledge-graph-node",
                `knowledge-graph-node-${data.kind}`,
                selected ? "knowledge-graph-node-selected" : ""
            ]
                .filter(Boolean)
                .join(" ")}
        >
            <div className="knowledge-graph-node-topline">
                <span>{data.kind}</span>
                {data.status ? <strong>{data.status}</strong> : null}
            </div>
            <h3>{data.label}</h3>
            {data.subtitle ? <p>{data.subtitle}</p> : null}
            {metricText ? <small>{metricText}</small> : null}
        </div>
    );
};

const nodeTypes = {
    knowledge: KnowledgeGraphNode
};

export const KnowledgeGraphCanvas = ({ canvasView }: KnowledgeGraphCanvasProps) => {
    const navigate = useNavigate();
    const elements = useMemo(() => layoutKnowledgeGraph(canvasView), [canvasView]);

    if (!canvasView) {
        return null;
    }

    return (
        <section className="knowledge-graph-canvas" aria-label={canvasView.title}>
            <div className="knowledge-graph-canvas-header">
                <div>
                    <p>Graph Canvas</p>
                    <h2>{canvasView.title}</h2>
                    <span>{canvasView.description}</span>
                </div>
                <strong>{canvasView.mode}</strong>
            </div>

            <div className="knowledge-graph-canvas-frame">
                <ReactFlow
                    fitView
                    edges={elements.edges}
                    edgesFocusable={false}
                    elementsSelectable
                    maxZoom={1.6}
                    minZoom={0.35}
                    nodes={elements.nodes}
                    nodesConnectable={false}
                    nodesDraggable={false}
                    nodeTypes={nodeTypes}
                    panOnDrag
                    proOptions={{ hideAttribution: true }}
                    zoomOnDoubleClick={false}
                    onNodeClick={(_, node) => {
                        const href = node.data.href;
                        if (typeof href === "string" && href.length > 0) {
                            navigate(href);
                        }
                    }}
                >
                    <Background color="#d8cdb9" gap={22} variant={BackgroundVariant.Dots} />
                    <MiniMap
                        nodeBorderRadius={8}
                        nodeColor={(node) =>
                            node.data.kind === "category" ? "#2c6a5a" : "#785936"
                        }
                        pannable
                        zoomable
                    />
                    <Controls showInteractive={false} />
                </ReactFlow>
                {canvasView.empty ? (
                    <div className="knowledge-graph-empty">
                        <h3>{canvasView.emptyTitle}</h3>
                        <p>{canvasView.emptyDescription}</p>
                    </div>
                ) : null}
            </div>
        </section>
    );
};
